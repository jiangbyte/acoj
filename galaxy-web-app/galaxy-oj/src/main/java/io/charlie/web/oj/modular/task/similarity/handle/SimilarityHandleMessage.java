package io.charlie.web.oj.modular.task.similarity.handle;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import io.charlie.galaxy.utils.ranking.RankingUtil;
import io.charlie.web.oj.modular.data.library.entity.DataLibrary;
import io.charlie.web.oj.modular.data.library.mapper.DataLibraryMapper;
import io.charlie.web.oj.modular.data.problem.entity.DataProblem;
import io.charlie.web.oj.modular.data.problem.mapper.DataProblemMapper;
import io.charlie.web.oj.modular.data.reports.entity.TaskReports;
import io.charlie.web.oj.modular.data.reports.mapper.TaskReportsMapper;
import io.charlie.web.oj.modular.data.similarity.entity.TaskSimilarity;
import io.charlie.web.oj.modular.data.similarity.mapper.TaskSimilarityMapper;
import io.charlie.web.oj.modular.data.solved.mapper.DataSolvedMapper;
import io.charlie.web.oj.modular.data.submit.entity.DataSubmit;
import io.charlie.web.oj.modular.data.submit.mapper.DataSubmitMapper;
import io.charlie.web.oj.modular.sys.config.entity.SysConfig;
import io.charlie.web.oj.modular.sys.config.mapper.SysConfigMapper;
import io.charlie.web.oj.modular.task.similarity.dto.SimilaritySubmitDto;
import io.charlie.web.oj.modular.task.similarity.enums.CloneLevelEnum;
import io.charlie.web.oj.modular.task.similarity.enums.ReportTypeEnum;
import io.charlie.web.oj.modular.task.similarity.mq.CommonSimilarityQueue;
import io.charlie.web.oj.modular.task.similarity.utils.CodeSimilarityCalculator;
import io.charlie.web.oj.modular.task.similarity.utils.DynamicCloneLevelDetector;
import io.charlie.web.oj.modular.websocket.utils.WebSocketUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

/**
 * @author ZhangJiangHu
 * @version v1.0
 * @date 20/09/2025
 * @description TODO
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class SimilarityHandleMessage {
    private final RabbitTemplate rabbitTemplate;
    private final WebSocketUtil webSocketUtil;
    private final DataSubmitMapper dataSubmitMapper;
    private final DataSolvedMapper dataSolvedMapper;
    private final DataProblemMapper dataProblemMapper;
    private final RankingUtil rankingUtil;
    private final DataLibraryMapper dataLibraryMapper;
    private final SysConfigMapper sysConfigMapper;
    private final TaskSimilarityMapper taskSimilarityMapper;
    private final TaskReportsMapper taskReportsMapper;
    private final CodeSimilarityCalculator codeSimilarityCalculator;

    public void sendSimilarity(SimilaritySubmitDto similaritySubmitDto) {

        rabbitTemplate.convertAndSend(
                CommonSimilarityQueue.EXCHANGE,
                CommonSimilarityQueue.ROUTING_KEY,
                similaritySubmitDto
        );
    }

    @Transactional
    @RabbitListener(queues = CommonSimilarityQueue.QUEUE, concurrency = "5-10")
    public void receiveSimilarity(SimilaritySubmitDto similaritySubmitDto) {
        log.info("代码克隆检测 -> 接收检测消息：{}", JSONUtil.toJsonStr(similaritySubmitDto));

        int MIN_SAMPLE_SIZE = 0;
        SysConfig sysConfig = sysConfigMapper.selectOne(new LambdaQueryWrapper<SysConfig>().eq(SysConfig::getCode, "APP_DEFAULT_SAMPLE_SIZE_MIN"));
        if (ObjectUtil.isNotEmpty(sysConfig)) {
            MIN_SAMPLE_SIZE = Integer.parseInt(sysConfig.getValue());
        }

        int RECENT_SAMPLE_SIZE = 0;
        sysConfig = sysConfigMapper.selectOne(new LambdaQueryWrapper<SysConfig>().eq(SysConfig::getCode, "APP_DEFAULT_SAMPLE_SIZE_RECENT"));
        if (ObjectUtil.isNotEmpty(sysConfig)) {
            RECENT_SAMPLE_SIZE = Integer.parseInt(sysConfig.getValue());
        }

        int MIN_MATCH_LENGTH;
        sysConfig = sysConfigMapper.selectOne(new LambdaQueryWrapper<SysConfig>().eq(SysConfig::getCode, "APP_DEFAULT_MATCH_LENGTH"));
        if (ObjectUtil.isNotEmpty(sysConfig)) {
            MIN_MATCH_LENGTH = Integer.parseInt(sysConfig.getValue());
        } else {
            MIN_MATCH_LENGTH = 0;
        }

        // 如果数量其中一个为0或者都为0，则忽略
        if (MIN_SAMPLE_SIZE <= 0 || RECENT_SAMPLE_SIZE <= 0 || MIN_MATCH_LENGTH <= 0) {
            log.info("代码克隆检测 -> 忽略空数据");
            DataSubmit dataSubmit = dataSubmitMapper.selectById(similaritySubmitDto.getId());
            dataSubmit.setSimilarity(BigDecimal.ZERO);
            dataSubmit.setSimilarityCategory(CloneLevelEnum.NOT_DETECTED.getValue());
            dataSubmitMapper.updateById(dataSubmit);
            return;
        }

        // 抽取样本库（排除当前提交用户自身的）
        LambdaQueryWrapper<DataLibrary> queryWrapper = new LambdaQueryWrapper<DataLibrary>()
                .ne(DataLibrary::getUserId, similaritySubmitDto.getUserId()) // 排除提交用户自身的提交
                .eq(DataLibrary::getProblemId, similaritySubmitDto.getProblemId())
                .eq(DataLibrary::getLanguage, similaritySubmitDto.getLanguage())
                .eq(similaritySubmitDto.getIsSet(), DataLibrary::getIsSet, true);

        // 先查询总数量
        long totalCount = dataLibraryMapper.selectCount(queryWrapper);

        List<DataLibrary> proSampleLibraries;

        if (totalCount > MIN_SAMPLE_SIZE) {
            // 如果总数超过最低限制，优先获取最近的RECENT_SAMPLE_SIZE条记录
            queryWrapper.orderByDesc(DataLibrary::getSubmitTime)
                    .last("LIMIT " + RECENT_SAMPLE_SIZE);
            proSampleLibraries = dataLibraryMapper.selectList(queryWrapper);

            // 如果获取的近期样本不足最低限制，补充旧样本
            if (proSampleLibraries.size() < MIN_SAMPLE_SIZE) {
                queryWrapper.clear();
                queryWrapper.ne(DataLibrary::getUserId, similaritySubmitDto.getUserId()) // 排除提交用户自身的提交
                        .eq(DataLibrary::getProblemId, similaritySubmitDto.getProblemId())
                        .orderByAsc(DataLibrary::getSubmitTime)
                        .last("LIMIT " + (MIN_SAMPLE_SIZE - proSampleLibraries.size()));
                List<DataLibrary> additionalSubmits = dataLibraryMapper.selectList(queryWrapper);
                proSampleLibraries.addAll(additionalSubmits);
            }
        } else {
            // 如果总数不足最低限制，获取全部
            log.info("总数不足最低限制，获取全部数据构建检测库");
            proSampleLibraries = dataLibraryMapper.selectList(queryWrapper);
        }

        // 空直接忽略
        if (ObjectUtil.isEmpty(proSampleLibraries)) {
            log.info("代码克隆检测 -> 忽略空数据");
            DataSubmit dataSubmit = dataSubmitMapper.selectById(similaritySubmitDto.getId());
            dataSubmit.setSimilarity(BigDecimal.ZERO);
            dataSubmit.setSimilarityCategory(CloneLevelEnum.NOT_DETECTED.getValue());
            dataSubmitMapper.updateById(dataSubmit);
            return;
        }

        List<TaskSimilarity> proSimilarityDetails = new ArrayList<>();
        AtomicReference<TaskSimilarity> maxProSimilarityDetailRef = new AtomicReference<>();

        proSampleLibraries.forEach(proSampleLibrary -> {
            // 使用一次样本库，那么样本库的访问次数加1
            proSampleLibrary.setAccessCount(proSampleLibrary.getAccessCount() + 1);

            CodeSimilarityCalculator.SimilarityDetail similarityDetail = codeSimilarityCalculator.getSimilarityDetail(
                    similaritySubmitDto.getLanguage().toLowerCase(),
                    proSampleLibrary.getCode(),
                    similaritySubmitDto.getCode(),
                    MIN_MATCH_LENGTH
            );

            TaskSimilarity proSimilarityDetail = new TaskSimilarity();
            // 基础信息
            proSimilarityDetail.setTaskId(similaritySubmitDto.getTaskId());
            proSimilarityDetail.setTaskType(similaritySubmitDto.getTaskType());
            proSimilarityDetail.setProblemId(similaritySubmitDto.getProblemId());
            proSimilarityDetail.setLanguage(proSampleLibrary.getLanguage());
            proSimilarityDetail.setSimilarity(BigDecimal.valueOf(similarityDetail.getSimilarity()));
            // 提交用户
            proSimilarityDetail.setSubmitUser(similaritySubmitDto.getUserId());
            proSimilarityDetail.setSubmitCode(similaritySubmitDto.getCode());
            proSimilarityDetail.setSubmitCodeLength(similaritySubmitDto.getCode().length());
            proSimilarityDetail.setSubmitId(similaritySubmitDto.getId());
            proSimilarityDetail.setSubmitTime(similaritySubmitDto.getCreateTime());
            proSimilarityDetail.setSubmitTokenName(similarityDetail.getTokenNames1());
            proSimilarityDetail.setSubmitTokenTexts(similarityDetail.getTokenTexts1());

            // 样本信息
            proSimilarityDetail.setOriginUser(proSampleLibrary.getUserId());
            proSimilarityDetail.setOriginCode(proSampleLibrary.getCode());
            proSimilarityDetail.setOriginCodeLength(proSampleLibrary.getCodeLength());
            proSimilarityDetail.setOriginId(proSampleLibrary.getSubmitId());
            proSimilarityDetail.setOriginTime(proSampleLibrary.getCreateTime());
            proSimilarityDetail.setOriginTokenName(similarityDetail.getTokenNames2());
            proSimilarityDetail.setOriginTokenTexts(similarityDetail.getTokenTexts2());

            proSimilarityDetails.add(proSimilarityDetail);

            // 使用原子引用更新
            maxProSimilarityDetailRef.updateAndGet(currentMax -> {
                if (currentMax == null) {
                    return proSimilarityDetail;
                } else {
                    BigDecimal currentSimilarity = BigDecimal.valueOf(similarityDetail.getSimilarity());
                    return currentSimilarity.compareTo(currentMax.getSimilarity()) > 0
                            ? proSimilarityDetail
                            : currentMax;
                }
            });
        });
        dataLibraryMapper.updateById(proSampleLibraries); // 更新访问次数

        // 任务详情记录
        taskSimilarityMapper.insert(proSimilarityDetails);

        // 任务ID和题目ID
        String taskId = similaritySubmitDto.getTaskId();
        String problemId = similaritySubmitDto.getProblemId();
        // 任务详情做分析，这里的报告类型是单次提交报告
        TaskSimilarity maxProSimilarityDetail = maxProSimilarityDetailRef.get(); // 原子获取
        // 样本数
        int sampleCount = proSampleLibraries.size();

        DataProblem proProblem = dataProblemMapper.selectById(similaritySubmitDto.getProblemId());
        if (proProblem == null) {
            return;
        }

        TaskReports reports = new TaskReports();
        reports.setReportType(ReportTypeEnum.SINGLE_SUBMIT.getValue());
        reports.setTaskId(taskId);
        reports.setProblemId(problemId);
        reports.setSampleCount(sampleCount);
        //相似组数
        reports.setAvgSimilarity(calculateAverageSimilarity(proSimilarityDetails));
        reports.setMaxSimilarity(maxProSimilarityDetail.getSimilarity());
        reports.setThreshold(proProblem.getThreshold());
        reports.setSimilarityDistribution(calculateSimilarityDistribution(proSimilarityDetails));
        DynamicCloneLevelDetector dynamicCloneLevelDetector = new DynamicCloneLevelDetector(proProblem.getThreshold());
        List<DynamicCloneLevelDetector.CloneLevel> levels = dynamicCloneLevelDetector.getLevels();
        reports.setDegreeStatistics(calculateSimilarityDegreeStatistics(proSimilarityDetails, levels));

        taskReportsMapper.insert(reports);

        DataSubmit proSubmit = dataSubmitMapper.selectById(similaritySubmitDto.getId());
        if (proSubmit == null) {
            return;
        }
        // 比较阈值和相似度,划分等级
        BigDecimal similarity = maxProSimilarityDetail.getSimilarity();
        CloneLevelEnum detect = dynamicCloneLevelDetector.detect(similarity);
        dataSubmitMapper.update(new LambdaUpdateWrapper<DataSubmit>()
                .eq(DataSubmit::getId, proSubmit.getId())
                .set(DataSubmit::getSimilarity, similarity)
                .set(DataSubmit::getSimilarityCategory, detect.getValue())
                .set(DataSubmit::getReportId, reports.getId())
        );
        log.info("更新成功");
    }

    /**
     * 计算相似度分布
     *
     * @param proSimilarityDetails 相似度详情列表
     * @return 分布结果列表
     */
    private List<Integer> calculateSimilarityDistribution(List<TaskSimilarity> proSimilarityDetails) {
        int[] distribution = new int[10];
        int totalCount = proSimilarityDetails.size();

        for (TaskSimilarity detail : proSimilarityDetails) {
            double similarity = detail.getSimilarity().doubleValue();
            int index = Math.min((int) (similarity * 10), 9);
            distribution[index]++;
        }

//        String[] ranges = {"0-10%", "10-20%", "20-30%", "30-40%", "40-50%",
//                "50-60%", "60-70%", "70-80%", "80-90%", "90-100%"};

        List<Integer> result = new ArrayList<>();
        for (int i = 0; i < distribution.length; i++) {
//                result.add(String.format("%s: %d个样本", ranges[i], distribution[i]));
            result.add(distribution[i]);
        }

        return result;
    }

    /**
     * 计算平均相似度
     *
     * @param proSimilarityDetails 相似度详情列表
     * @return 平均相似度，保留4位小数
     */
    private BigDecimal calculateAverageSimilarity(List<TaskSimilarity> proSimilarityDetails) {
        if (proSimilarityDetails == null || proSimilarityDetails.isEmpty()) {
            return BigDecimal.ZERO;
        }

        double totalSimilarity = 0.0;

        for (TaskSimilarity detail : proSimilarityDetails) {
            totalSimilarity += detail.getSimilarity().doubleValue();
        }

        return BigDecimal.valueOf(totalSimilarity / proSimilarityDetails.size())
                .setScale(4, RoundingMode.HALF_UP);
    }

    private List<DynamicCloneLevelDetector.CloneLevel> calculateSimilarityDegreeStatistics(List<TaskSimilarity> proSimilarityDetails, List<DynamicCloneLevelDetector.CloneLevel> levels) {
        if (proSimilarityDetails == null || proSimilarityDetails.isEmpty() || levels == null || levels.isEmpty()) {
            return levels;
        }

        // 按相似度阈值从高到低排序，确保正确的范围匹配
        List<DynamicCloneLevelDetector.CloneLevel> sortedLevels = levels.stream()
                .sorted((l1, l2) -> l2.getSimilarity().compareTo(l1.getSimilarity()))
                .collect(Collectors.toList());

        // 初始化各级别计数器
        int[] counts = new int[sortedLevels.size()];
        Arrays.fill(counts, 0);

        // 统计各个级别的数量
        for (TaskSimilarity detail : proSimilarityDetails) {
            BigDecimal similarity = detail.getSimilarity();
            if (similarity != null) {
                boolean matched = false;
                // 从最高级别开始检查
                for (int i = 0; i < sortedLevels.size(); i++) {
                    DynamicCloneLevelDetector.CloneLevel currentLevel = sortedLevels.get(i);
                    DynamicCloneLevelDetector.CloneLevel nextLevel = (i < sortedLevels.size() - 1) ? sortedLevels.get(i + 1) : null;

                    // 如果是最高级别（第一个），只需要大于等于该阈值
                    if (i == 0 && similarity.compareTo(currentLevel.getSimilarity()) >= 0) {
                        counts[i]++;
                        matched = true;
                        break;
                    }
                    // 如果是中间级别，需要大于等于当前阈值但小于上一级阈值
                    else if (i > 0 && nextLevel != null) {
                        if (similarity.compareTo(currentLevel.getSimilarity()) >= 0 &&
                                similarity.compareTo(sortedLevels.get(i - 1).getSimilarity()) < 0) {
                            counts[i]++;
                            matched = true;
                            break;
                        }
                    }
                    // 如果是最低级别（最后一个），只需要小于上一级阈值
                    else if (i == sortedLevels.size() - 1) {
                        if (similarity.compareTo(currentLevel.getSimilarity()) >= 0 &&
                                similarity.compareTo(sortedLevels.get(i - 1).getSimilarity()) < 0) {
                            counts[i]++;
                            matched = true;
                            break;
                        }
                    }
                }

                // 如果没有匹配任何级别，分配到最低级别
                if (!matched) {
                    counts[counts.length - 1]++;
                }
            }
        }

        // 计算总数
        int totalCount = proSimilarityDetails.size();

        // 更新levels中的count和percentage
        for (int i = 0; i < sortedLevels.size(); i++) {
            DynamicCloneLevelDetector.CloneLevel level = sortedLevels.get(i);
            level.setCount(counts[i]);

            if (totalCount > 0) {
                BigDecimal percentage = new BigDecimal(counts[i])
                        .divide(new BigDecimal(totalCount), 4, RoundingMode.HALF_UP)
                        .multiply(new BigDecimal(100));
                level.setPercentage(percentage);
            } else {
                level.setPercentage(BigDecimal.ZERO);
            }
        }

        return sortedLevels;
    }

}
