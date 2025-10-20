package io.charlie.web.oj.modular.task.similarity.handle;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import io.charlie.web.oj.modular.data.library.entity.DataLibrary;
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
import io.charlie.web.oj.modular.task.similarity.dto.SimilaritySubmitDto;
import io.charlie.web.oj.modular.task.similarity.enums.CloneLevelEnum;
import io.charlie.web.oj.modular.task.similarity.enums.ReportTypeEnum;
import io.charlie.web.oj.modular.task.similarity.mq.CommonSimilarityQueue;
import io.charlie.web.oj.modular.task.similarity.utils.CodeSimilarityCalculator;
import io.charlie.web.oj.modular.task.similarity.utils.DynamicCloneLevelDetector;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class SimilarityHandleMessage {

    private final RabbitTemplate rabbitTemplate;
    private final DataSubmitMapper dataSubmitMapper;
    private final DataProblemMapper dataProblemMapper;
    private final DataLibraryService dataLibraryService;
    private final SysConfigMapper sysConfigMapper;
    private final TaskSimilarityMapper taskSimilarityMapper;
    private final TaskReportsMapper taskReportsMapper;
    private final CodeSimilarityCalculator codeSimilarityCalculator;

    public void sendSimilarity(SimilaritySubmitDto dto) {
        log.info("代码克隆检测 -> 发送检测消息：{}", JSONUtil.toJsonStr(dto));
        rabbitTemplate.convertAndSend(
                CommonSimilarityQueue.EXCHANGE,
                CommonSimilarityQueue.ROUTING_KEY,
                dto
        );
    }

    @Transactional
    @RabbitListener(queues = CommonSimilarityQueue.QUEUE, concurrency = "5-10")
    public void receiveSimilarity(SimilaritySubmitDto similaritySubmitDto) {
        log.info("代码克隆检测 -> 接收检测消息：{}", JSONUtil.toJsonStr(similaritySubmitDto));

        Config config = loadConfig();
        if (Config.shouldSkip(config)) {
            skipDetection(similaritySubmitDto);
            return;
        }

        log.info("代码克隆检测 -> 获取配置：{}", JSONUtil.toJsonStr(config));
        dataLibraryService.processCodeLibrariesInBatches(
                similaritySubmitDto.getIsSet(),
                similaritySubmitDto.getLanguage(),
                List.of(similaritySubmitDto.getProblemId()),
                List.of(similaritySubmitDto.getSetId()),
                null,
                0, // 不批次
                libraries -> {
                    if (libraries.isEmpty()) {
                        skipDetection(similaritySubmitDto);
                        return;
                    }

                    calculateTaskSimilarities(similaritySubmitDto, libraries, config.getMinMatchLength());
                }
        );
    }

    private void calculateTaskSimilarities(SimilaritySubmitDto similaritySubmitDto, List<DataLibrary> samples, int minMatchLength) {
        log.info("代码克隆检测 -> 计算相似度");
        List<TaskSimilarity> details = new ArrayList<>();

        // 使用 stream 过滤掉用户本身的样本
        List<DataLibrary> filteredSamples = samples.stream()
                .filter(sample -> !sample.getUserId().equals(similaritySubmitDto.getUserId()))
                .toList();

        for (DataLibrary dataLibrary : filteredSamples) {
            CodeSimilarityCalculator.SimilarityDetail similarity = codeSimilarityCalculator.getSimilarityDetail(
                    similaritySubmitDto.getLanguage().toLowerCase(),
                    dataLibrary.getCode(),
                    similaritySubmitDto.getCode(),
                    minMatchLength
            );

            TaskSimilarity detail = new TaskSimilarity();
            // 基础信息
            detail.setTaskId(similaritySubmitDto.getTaskId());
            detail.setTaskType(similaritySubmitDto.getTaskType());
            detail.setProblemId(similaritySubmitDto.getProblemId());
            detail.setSetId(similaritySubmitDto.getSetId());
            detail.setIsSet(similaritySubmitDto.getIsSet());
            detail.setLanguage(dataLibrary.getLanguage());
            detail.setSimilarity(BigDecimal.valueOf(similarity.getSimilarity()));

            // 提交信息
            detail.setSubmitUser(similaritySubmitDto.getUserId());
            detail.setSubmitCode(similaritySubmitDto.getCode());
            detail.setSubmitCodeLength(similaritySubmitDto.getCode().length());
            detail.setSubmitId(similaritySubmitDto.getId());
            detail.setSubmitTime(similaritySubmitDto.getCreateTime());
            detail.setSubmitTokenName(similarity.getTokenNames1());
            detail.setSubmitTokenTexts(similarity.getTokenTexts1());

            // 样本信息
            detail.setOriginUser(dataLibrary.getUserId());
            detail.setOriginCode(dataLibrary.getCode());
            detail.setOriginCodeLength(dataLibrary.getCodeLength());
            detail.setOriginId(dataLibrary.getSubmitId());
            detail.setOriginTime(dataLibrary.getCreateTime());
            detail.setOriginTokenName(similarity.getTokenNames2());
            detail.setOriginTokenTexts(similarity.getTokenTexts2());

            // 增加
            details.add(detail);
        }

        saveSimilarityReportData(similaritySubmitDto, details);
    }

    private void saveSimilarityReportData(SimilaritySubmitDto similaritySubmitDto, List<TaskSimilarity> similarities) {
        taskSimilarityMapper.insert(similarities);

        DataProblem dataProblem = dataProblemMapper.selectById(similaritySubmitDto.getProblemId());

        // 阈值分布选择器
        DynamicCloneLevelDetector detector = new DynamicCloneLevelDetector(dataProblem.getThreshold());

        TaskReports taskReports = BeanUtil.toBean(similaritySubmitDto, TaskReports.class);
        if (similaritySubmitDto.getIsSet()) {
            log.info("代码克隆检测 -> 单题集报告");
            taskReports.setReportType(ReportTypeEnum.SET_SINGLE_SUBMIT.getValue());
        } else {
            log.info("代码克隆检测 -> 单题目报告");
            taskReports.setReportType(ReportTypeEnum.PROBLEM_SINGLE_SUBMIT.getValue());
        }
        taskReports.setSampleCount(similarities.size());
        taskReports.setThreshold(dataProblem.getThreshold());

        // 按相似度聚合统计个数
        Map<BigDecimal, Long> similarityGroupCount = similarities.stream()
                .collect(Collectors.groupingBy(TaskSimilarity::getSimilarity, Collectors.counting()));
        taskReports.setSimilarityGroupCount(similarityGroupCount.size());

        BigDecimal avgSimilarity = BigDecimal.valueOf(similarities.stream()
                .mapToDouble(d -> d.getSimilarity().doubleValue())
                .average()
                .orElse(0.0)).setScale(4, RoundingMode.HALF_UP);
        taskReports.setAvgSimilarity(avgSimilarity);

        // 取最大
        BigDecimal maxSimilarity = similarities.stream().max(Comparator.comparing(TaskSimilarity::getSimilarity)).get().getSimilarity();
        taskReports.setMaxSimilarity(maxSimilarity);

        // 相似度分布
        taskReports.setSimilarityDistribution(detector.similarityDistribution(similarities));
        // 程度统计
        taskReports.setDegreeStatistics(detector.similarityDegreeStatistics(similarities));

        taskReportsMapper.insert(taskReports);

        log.info("更新提交 {} 最大相似度 {} 平均相似度 {}", similaritySubmitDto.getId(), maxSimilarity, avgSimilarity);
        updateDataSubmit(
                similaritySubmitDto.getId(),
                maxSimilarity,
                detector.detect(maxSimilarity).getValue(),
                taskReports.getId()
        );
    }

    private void updateDataSubmit(String submitId, BigDecimal similarity, String category, String reportId) {
        log.info("更新提交 {} 相似度 {} 相似等级分类 {}", submitId, similarity, category);
        dataSubmitMapper.update(new LambdaUpdateWrapper<DataSubmit>()
                .eq(DataSubmit::getId, submitId)
                .set(DataSubmit::getSimilarity, similarity)
                .set(DataSubmit::getSimilarityCategory, category)
                .set(DataSubmit::getIsFinish, Boolean.TRUE)
                .set(StrUtil.isNotEmpty(reportId), DataSubmit::getReportId, reportId)
        );
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
        updateDataSubmit(
                dto.getId(),
                BigDecimal.ZERO,
                CloneLevelEnum.NOT_DETECTED.getValue(),
                null
        );
    }

    private Config loadConfig() {
        Config config = new Config();
        config.setMinSampleSize(getConfigValue("APP_DEFAULT_SAMPLE_SIZE_MIN", 0));
        config.setRecentSampleSize(getConfigValue("APP_DEFAULT_SAMPLE_SIZE_RECENT", 0));
        config.setMinMatchLength(getConfigValue("APP_DEFAULT_MATCH_LENGTH", 0));
        return config;
    }
}