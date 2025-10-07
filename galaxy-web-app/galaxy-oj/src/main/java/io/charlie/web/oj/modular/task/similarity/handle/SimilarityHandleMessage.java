package io.charlie.web.oj.modular.task.similarity.handle;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import io.charlie.web.oj.modular.data.library.entity.DataLibrary;
import io.charlie.web.oj.modular.data.library.mapper.DataLibraryMapper;
import io.charlie.web.oj.modular.data.library.service.DataLibraryService;
import io.charlie.web.oj.modular.data.problem.entity.DataProblem;
import io.charlie.web.oj.modular.data.problem.mapper.DataProblemMapper;
import io.charlie.web.oj.modular.data.reports.entity.TaskReports;
import io.charlie.web.oj.modular.data.reports.mapper.TaskReportsMapper;
import io.charlie.web.oj.modular.data.similarity.entity.TaskSimilarity;
import io.charlie.web.oj.modular.data.similarity.mapper.TaskSimilarityMapper;
import io.charlie.web.oj.modular.data.submit.entity.DataSubmit;
import io.charlie.web.oj.modular.data.submit.mapper.DataSubmitMapper;
import io.charlie.web.oj.modular.sys.config.entity.SysConfig;
import io.charlie.web.oj.modular.sys.config.mapper.SysConfigMapper;
import io.charlie.web.oj.modular.task.similarity.data.Config;
import io.charlie.web.oj.modular.task.similarity.data.SimilarityResult;
import io.charlie.web.oj.modular.task.similarity.dto.SimilaritySubmitDto;
import io.charlie.web.oj.modular.task.similarity.enums.CloneLevelEnum;
import io.charlie.web.oj.modular.task.similarity.enums.ReportTypeEnum;
import io.charlie.web.oj.modular.task.similarity.mq.CommonSimilarityQueue;
import io.charlie.web.oj.modular.task.similarity.utils.CodeSimilarityCalculator;
import io.charlie.web.oj.modular.task.similarity.utils.DynamicCloneLevelDetector;
import io.charlie.web.oj.modular.websocket.config.WebSocketConfig;
import io.charlie.web.oj.modular.websocket.data.WebSocketMessage;
import io.charlie.web.oj.modular.websocket.utils.WebSocketUtil;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.dromara.trans.service.impl.TransService;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Slf4j
@Component
@RequiredArgsConstructor
public class SimilarityHandleMessage {

    private final RabbitTemplate rabbitTemplate;
    private final WebSocketUtil webSocketUtil;
    private final DataSubmitMapper dataSubmitMapper;
    private final DataProblemMapper dataProblemMapper;
    private final DataLibraryService dataLibraryService;
    private final SysConfigMapper sysConfigMapper;
    private final TaskSimilarityMapper taskSimilarityMapper;
    private final TaskReportsMapper taskReportsMapper;
    private final CodeSimilarityCalculator codeSimilarityCalculator;
    private final TransService transService;

    public void sendSimilarity(SimilaritySubmitDto dto) {
        log.info("代码克隆检测 -> 发送检测消息：{}", JSONUtil.toJsonStr(dto));
        rabbitTemplate.convertAndSend(CommonSimilarityQueue.EXCHANGE, CommonSimilarityQueue.ROUTING_KEY, dto);
    }

    @Transactional
    @RabbitListener(queues = CommonSimilarityQueue.QUEUE, concurrency = "5-10")
    public void receiveSimilarity(SimilaritySubmitDto dto) {
        log.info("代码克隆检测 -> 接收检测消息：{}", JSONUtil.toJsonStr(dto));

        Config config = loadConfig();
        if (Config.shouldSkip(config)) {
            skipDetection(dto);
            return;
        }

        int batchSize = 0; // 不批次
        dataLibraryService.processCodeLibrariesInBatches(
                dto.getIsSet(),
                dto.getLanguage(),
                List.of(dto.getProblemId()),
                List.of(dto.getSetId()),
                null,
                batchSize,
                libraries -> {
                    if (libraries.isEmpty()) {
                        skipDetection(dto);
                        return;
                    }

                    SimilarityResult result = calculateSimilarities(dto, libraries, config.getMinMatchLength());
                    String reportId = saveResults(dto, result);
                    updateSubmitAndNotify(dto, result, reportId);
                }
        );
    }

    private Config loadConfig() {
        Config config = new Config();
        config.setMinSampleSize(getConfigValue("APP_DEFAULT_SAMPLE_SIZE_MIN", 0));
        config.setRecentSampleSize(getConfigValue("APP_DEFAULT_SAMPLE_SIZE_RECENT", 0));
        config.setMinMatchLength(getConfigValue("APP_DEFAULT_MATCH_LENGTH", 0));
        return config;
    }

    private int getConfigValue(String code, int defaultValue) {
        SysConfig sysConfig = sysConfigMapper.selectOne(new LambdaQueryWrapper<SysConfig>()
                .eq(SysConfig::getCode, code));

        if (sysConfig != null && sysConfig.getValue() != null) {
            return Integer.parseInt(sysConfig.getValue());
        } else {
            return defaultValue;
        }
    }

    private void skipDetection(SimilaritySubmitDto dto) {
        log.info("代码克隆检测 -> 忽略空数据");
        updateSubmitSimilarity(dto.getId(), BigDecimal.ZERO, CloneLevelEnum.NOT_DETECTED.getValue(), null);
    }

    private void updateSubmitSimilarity(String submitId, BigDecimal similarity, String category, String reportId) {
        log.info("更新提交 {} 相似度 {} 相似等级分类 {}", submitId, similarity, category);
        dataSubmitMapper.update(new LambdaUpdateWrapper<DataSubmit>()
                .eq(DataSubmit::getId, submitId)
                .set(DataSubmit::getSimilarity, similarity)
                .set(DataSubmit::getSimilarityCategory, category)
                .set(StrUtil.isNotEmpty(reportId), DataSubmit::getReportId, reportId)
        );
    }

    private SimilarityResult calculateSimilarities(SimilaritySubmitDto dto, List<DataLibrary> samples, int minMatchLength) {
        List<TaskSimilarity> details = new ArrayList<>();
        TaskSimilarity maxDetail = null;

        for (DataLibrary sample : samples) {
            CodeSimilarityCalculator.SimilarityDetail similarity = codeSimilarityCalculator.getSimilarityDetail(
                    dto.getLanguage().toLowerCase(),
                    sample.getCode(),
                    dto.getCode(),
                    minMatchLength
            );

            TaskSimilarity detail = buildTaskSimilarity(dto, sample, similarity);
            details.add(detail);

            if (maxDetail == null || similarity.getSimilarity() > maxDetail.getSimilarity().doubleValue()) {
                maxDetail = detail;
            }
        }

        return new SimilarityResult(details, maxDetail);
    }

    private TaskSimilarity buildTaskSimilarity(SimilaritySubmitDto dto, DataLibrary sample,
                                               CodeSimilarityCalculator.SimilarityDetail similarity) {
        TaskSimilarity detail = new TaskSimilarity();
        // 基础信息
        detail.setTaskId(dto.getTaskId());
        detail.setTaskType(dto.getTaskType());
        detail.setProblemId(dto.getProblemId());
        detail.setSetId(dto.getSetId());
        detail.setIsSet(dto.getIsSet());
        detail.setLanguage(sample.getLanguage());
        detail.setSimilarity(BigDecimal.valueOf(similarity.getSimilarity()));

        // 提交信息
        detail.setSubmitUser(dto.getUserId());
        detail.setSubmitCode(dto.getCode());
        detail.setSubmitCodeLength(dto.getCode().length());
        detail.setSubmitId(dto.getId());
        detail.setSubmitTime(dto.getCreateTime());
        detail.setSubmitTokenName(similarity.getTokenNames1());
        detail.setSubmitTokenTexts(similarity.getTokenTexts1());

        // 样本信息
        detail.setOriginUser(sample.getUserId());
        detail.setOriginCode(sample.getCode());
        detail.setOriginCodeLength(sample.getCodeLength());
        detail.setOriginId(sample.getSubmitId());
        detail.setOriginTime(sample.getCreateTime());
        detail.setOriginTokenName(similarity.getTokenNames2());
        detail.setOriginTokenTexts(similarity.getTokenTexts2());

        return detail;
    }

    private String saveResults(SimilaritySubmitDto dto, SimilarityResult result) {
        taskSimilarityMapper.insert(result.getDetails());

        DataProblem problem = dataProblemMapper.selectById(dto.getProblemId());
        if (problem == null) return "";

        TaskReports report = buildTaskReport(dto, result, problem);
        taskReportsMapper.insert(report);
        return report.getId();
    }

    private TaskReports buildTaskReport(SimilaritySubmitDto dto, SimilarityResult result, DataProblem problem) {
        TaskReports report = new TaskReports();
        if (dto.getIsSet()) {
            report.setReportType(ReportTypeEnum.SET_SINGLE_SUBMIT.getValue());
        }
        report.setReportType(ReportTypeEnum.PROBLEM_SINGLE_SUBMIT.getValue());
        report.setTaskId(dto.getTaskId());
        report.setProblemId(dto.getProblemId());
        report.setSampleCount(result.getDetails().size());
//        report.setSimilarityGroupCount(result.getDetails().size());
        report.setAvgSimilarity(calculateAverageSimilarity(result.getDetails()));
        report.setMaxSimilarity(result.getMaxDetail().getSimilarity());
        report.setThreshold(problem.getThreshold());
        report.setSimilarityDistribution(calculateSimilarityDistribution(result.getDetails()));

        DynamicCloneLevelDetector detector = new DynamicCloneLevelDetector(problem.getThreshold());
        report.setDegreeStatistics(calculateSimilarityDegreeStatistics(result.getDetails(), detector.getLevels()));

        return report;
    }

    private void updateSubmitAndNotify(SimilaritySubmitDto dto, SimilarityResult result, String reportId) {
        DataProblem problem = dataProblemMapper.selectById(dto.getProblemId());
        if (problem == null) return;

        DynamicCloneLevelDetector detector = new DynamicCloneLevelDetector(problem.getThreshold());
        CloneLevelEnum level = detector.detect(result.getMaxDetail().getSimilarity());

        updateSubmitSimilarity(dto.getId(), result.getMaxDetail().getSimilarity(), level.getValue(), reportId);

        DataSubmit submit = dataSubmitMapper.selectById(dto.getId());
        transService.transOne(submit);

        WebSocketMessage<DataSubmit> message = new WebSocketMessage<>();
        message.setData(submit);

        webSocketUtil.sendToTopic(WebSocketConfig.TOPIC_JUDGE_STATUS, dto.getJudgeTaskId(), message);
        webSocketUtil.sendToTopicClose(WebSocketConfig.TOPIC_JUDGE_STATUS, dto.getJudgeTaskId());
    }

    // 简化工具方法
    private List<Integer> calculateSimilarityDistribution(List<TaskSimilarity> details) {
        int[] distribution = new int[10];
        details.forEach(detail -> {
            int index = Math.min((int) (detail.getSimilarity().doubleValue() * 10), 9);
            distribution[index]++;
        });
        return Arrays.stream(distribution).boxed().collect(Collectors.toList());
    }

    private BigDecimal calculateAverageSimilarity(List<TaskSimilarity> details) {
        if (details.isEmpty()) return BigDecimal.ZERO;

        double avg = details.stream()
                .mapToDouble(d -> d.getSimilarity().doubleValue())
                .average()
                .orElse(0.0);

        return BigDecimal.valueOf(avg).setScale(4, RoundingMode.HALF_UP);
    }

    private List<DynamicCloneLevelDetector.CloneLevel> calculateSimilarityDegreeStatistics(
            List<TaskSimilarity> details,
            List<DynamicCloneLevelDetector.CloneLevel> levels
    ) {

        if (details.isEmpty() || levels.isEmpty()) return levels;

        // 按相似度阈值从高到低排序，确保正确的范围匹配
        List<DynamicCloneLevelDetector.CloneLevel> sortedLevels = levels.stream()
                .sorted(Comparator.comparing(DynamicCloneLevelDetector.CloneLevel::getSimilarity).reversed())
                .collect(Collectors.toList());

        int[] counts = new int[sortedLevels.size()];
        details.forEach(detail -> {
            BigDecimal similarity = detail.getSimilarity();
            int levelIndex = findMatchingLevelIndex(similarity, sortedLevels);
            if (levelIndex >= 0) counts[levelIndex]++;
        });

        updateLevelStatistics(sortedLevels, counts, details.size());
        return sortedLevels;
    }

    private int findMatchingLevelIndex(BigDecimal similarity, List<DynamicCloneLevelDetector.CloneLevel> levels) {
        for (int i = 0; i < levels.size(); i++) {
            DynamicCloneLevelDetector.CloneLevel level = levels.get(i);
            if (i == 0 && similarity.compareTo(level.getSimilarity()) >= 0) return i;
            if (i > 0 && similarity.compareTo(level.getSimilarity()) >= 0 &&
                    similarity.compareTo(levels.get(i - 1).getSimilarity()) < 0) return i;
        }
        return levels.size() - 1; // 默认最低级别
    }

    private void updateLevelStatistics(List<DynamicCloneLevelDetector.CloneLevel> levels, int[] counts, int total) {
        IntStream.range(0, levels.size()).forEach(i -> {
            DynamicCloneLevelDetector.CloneLevel level = levels.get(i);
            level.setCount(counts[i]);
            BigDecimal percentage = total > 0 ?
                    new BigDecimal(counts[i]).divide(new BigDecimal(total), 4, RoundingMode.HALF_UP).multiply(new BigDecimal(100)) :
                    BigDecimal.ZERO;
            level.setPercentage(percentage);
        });
    }


}