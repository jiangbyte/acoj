package io.charlie.app.core.modular.similarity.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import io.charlie.app.core.modular.judge.enums.JudgeStatus;
import io.charlie.app.core.modular.problem.problem.entity.ProProblem;
import io.charlie.app.core.modular.problem.problem.mapper.ProProblemMapper;
import io.charlie.app.core.modular.problem.reports.entity.ProSimilarityReports;
import io.charlie.app.core.modular.problem.reports.mapper.ProSimilarityReportsMapper;
import io.charlie.app.core.modular.problem.sample.entity.ProSampleLibrary;
import io.charlie.app.core.modular.problem.sample.mapper.ProSampleLibraryMapper;
import io.charlie.app.core.modular.problem.sample.service.ProSampleLibraryService;
import io.charlie.app.core.modular.problem.similarity.entity.ProSimilarityDetail;
import io.charlie.app.core.modular.problem.similarity.mapper.ProSimilarityDetailMapper;
import io.charlie.app.core.modular.problem.submit.entity.ProSubmit;
import io.charlie.app.core.modular.problem.submit.mapper.ProSubmitMapper;
import io.charlie.app.core.modular.reports.entity.DataReports;
import io.charlie.app.core.modular.reports.mapper.DataReportsMapper;
import io.charlie.app.core.modular.similarity.config.ProblemSimilarityMQConfig;
import io.charlie.app.core.modular.similarity.config.ProblemSimilarityResultMQConfig;
import io.charlie.app.core.modular.similarity.dto.SimilarityResult;
import io.charlie.app.core.modular.similarity.dto.SimilarityResultDto;
import io.charlie.app.core.modular.similarity.dto.SimilaritySubmitDto;
import io.charlie.app.core.modular.similarity.enums.CloneLevelEnum;
import io.charlie.app.core.modular.similarity.enums.ReportTypeEnum;
import io.charlie.app.core.modular.similarity.param.ProblemReportConfigParam;
import io.charlie.app.core.modular.similarity.service.ProblemSimilarityMessageService;
import io.charlie.app.core.modular.similarity.utils.CodeSimilarityCalculator;
import io.charlie.app.core.modular.similarity.utils.DynamicCloneLevelDetector;
import io.charlie.app.core.modular.sys.config.entity.SysConfig;
import io.charlie.app.core.modular.sys.config.mapper.SysConfigMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

/**
 * @author ZhangJiangHu
 * @version v1.0
 * @date 11/08/2025
 * @description 题目克隆检服务实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ProblemSimilarityMessageServiceImpl implements ProblemSimilarityMessageService {
    private final RabbitTemplate rabbitTemplate;
    private final CodeSimilarityCalculator codeSimilarityCalculator;
    private final ProSubmitMapper proSubmitMapper;
    private final SysConfigMapper sysConfigMapper;
    private final ProSampleLibraryMapper proSampleLibraryMapper;
    private final ProProblemMapper proProblemMapper;
    private final ProSimilarityDetailMapper proSimilarityDetailMapper;
    private final ProSimilarityReportsMapper proSimilarityReportsMapper;
    private final DataReportsMapper dataReportsMapper;

    @Override
    public void sendSimilarityRequest(SimilaritySubmitDto similaritySubmitDto) {
        log.info("代码克隆检测 -> 发送消息：{}", JSONUtil.toJsonStr(similaritySubmitDto));
        rabbitTemplate.convertAndSend(
                ProblemSimilarityMQConfig.EXCHANGE,
                ProblemSimilarityMQConfig.ROUTING_KEY,
                similaritySubmitDto
        );
    }

    @Transactional
    @RabbitListener(queues = ProblemSimilarityMQConfig.QUEUE, concurrency = "5-10")
    @Override
    public void handleSimilarityRequest(SimilaritySubmitDto similaritySubmitDto) {
        log.info("代码克隆检测 -> 接收检测消息：{}", JSONUtil.toJsonStr(similaritySubmitDto));

        SimilarityResultDto bean = BeanUtil.toBean(similaritySubmitDto, SimilarityResultDto.class);

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
            ProSubmit proSubmit = proSubmitMapper.selectById(similaritySubmitDto.getId());
            if (proSubmit == null) {
                return;
            }
            proSubmit.setSimilarity(BigDecimal.ZERO);
            proSubmit.setSimilarityBehavior(CloneLevelEnum.NOT_DETECTED.getValue());
            proSubmitMapper.updateById(proSubmit);
            return;
        }

        // 抽取样本库（排除当前提交用户自身的）
        LambdaQueryWrapper<ProSampleLibrary> queryWrapper = new LambdaQueryWrapper<ProSampleLibrary>()
                .ne(ProSampleLibrary::getUserId, similaritySubmitDto.getUserId()) // 排除提交用户自身的提交
                .eq(ProSampleLibrary::getProblemId, similaritySubmitDto.getProblemId());

        // 先查询总数量
        long totalCount = proSampleLibraryMapper.selectCount(queryWrapper);

        List<ProSampleLibrary> proSampleLibraries;

        if (totalCount > MIN_SAMPLE_SIZE) {
            // 如果总数超过最低限制，优先获取最近的RECENT_SAMPLE_SIZE条记录
            queryWrapper.orderByDesc(ProSampleLibrary::getSubmitTime)
                    .last("LIMIT " + RECENT_SAMPLE_SIZE);
            proSampleLibraries = proSampleLibraryMapper.selectList(queryWrapper);

            // 如果获取的近期样本不足最低限制，补充旧样本
            if (proSampleLibraries.size() < MIN_SAMPLE_SIZE) {
                queryWrapper.clear();
                queryWrapper.ne(ProSampleLibrary::getUserId, similaritySubmitDto.getUserId()) // 排除提交用户自身的提交
                        .eq(ProSampleLibrary::getProblemId, similaritySubmitDto.getProblemId())
                        .orderByAsc(ProSampleLibrary::getSubmitTime)
                        .last("LIMIT " + (MIN_SAMPLE_SIZE - proSampleLibraries.size()));
                List<ProSampleLibrary> additionalSubmits = proSampleLibraryMapper.selectList(queryWrapper);
                proSampleLibraries.addAll(additionalSubmits);
            }
        } else {
            // 如果总数不足最低限制，获取全部
            log.info("总数不足最低限制，获取全部数据构建检测库");
            proSampleLibraries = proSampleLibraryMapper.selectList(queryWrapper);
        }

        // 空直接忽略
        if (ObjectUtil.isEmpty(proSampleLibraries)) {
            log.info("代码克隆检测 -> 忽略空数据");
            ProSubmit proSubmit = proSubmitMapper.selectById(similaritySubmitDto.getId());
            if (proSubmit == null) {
                return;
            }
            proSubmit.setSimilarity(BigDecimal.ZERO);
            proSubmit.setSimilarityBehavior(CloneLevelEnum.NOT_DETECTED.getValue());
            proSubmitMapper.updateById(proSubmit);
            return;
        }

        List<ProSimilarityDetail> proSimilarityDetails = new ArrayList<>();
        AtomicReference<ProSimilarityDetail> maxProSimilarityDetailRef = new AtomicReference<>();

        proSampleLibraries.forEach(proSampleLibrary -> {
            // 使用一次样本库，那么样本库的访问次数加1
            proSampleLibrary.setAccessCount(proSampleLibrary.getAccessCount() + 1);

            CodeSimilarityCalculator.SimilarityDetail similarityDetail = codeSimilarityCalculator.getSimilarityDetail(
                    similaritySubmitDto.getLanguage().toLowerCase(),
                    proSampleLibrary.getCode(),
                    similaritySubmitDto.getCode(),
                    MIN_MATCH_LENGTH
            );

            ProSimilarityDetail proSimilarityDetail = new ProSimilarityDetail();
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
        proSampleLibraryMapper.updateById(proSampleLibraries); // 更新访问次数

        // 任务详情记录
        proSimilarityDetailMapper.insert(proSimilarityDetails);

        // 任务ID和题目ID
        String taskId = similaritySubmitDto.getTaskId();
        String problemId = similaritySubmitDto.getProblemId();
        // 任务详情做分析，这里的报告类型是单次提交报告
        ProSimilarityDetail maxProSimilarityDetail = maxProSimilarityDetailRef.get(); // 原子获取
        // 样本数
        int sampleCount = proSampleLibraries.size();

        ProProblem proProblem = proProblemMapper.selectById(similaritySubmitDto.getProblemId());
        if (proProblem == null) {
            return;
        }

        ProSimilarityReports reports = new ProSimilarityReports();
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

        proSimilarityReportsMapper.insert(reports);

        ProSubmit proSubmit = proSubmitMapper.selectById(similaritySubmitDto.getId());
        if (proSubmit == null) {
            return;
        }
        // 比较阈值和相似度,划分等级
        BigDecimal similarity = maxProSimilarityDetail.getSimilarity();
        CloneLevelEnum detect = dynamicCloneLevelDetector.detect(similarity);
        proSubmitMapper.update(new LambdaUpdateWrapper<ProSubmit>()
                .eq(ProSubmit::getId, proSubmit.getId())
                .set(ProSubmit::getSimilarity, similarity)
                .set(ProSubmit::getSimilarityBehavior, detect.getValue())
                .set(ProSubmit::getReportId, reports.getId())
        );
        log.info("更新成功");

//        this.sendSimilarityResult(bean);
    }

    @Override
    public String problemSimilarityReport(ProblemReportConfigParam problemReportConfigParam) {
        String taskId = IdUtil.getSnowflakeNextIdStr();
        log.info("开始生成题目报告 任务ID {}", taskId);

        // 获取配置参数
        String problemId = problemReportConfigParam.getProblemId();

        int MIN_MATCH_LENGTH;
        SysConfig sysConfig = sysConfigMapper.selectOne(new LambdaQueryWrapper<SysConfig>().eq(SysConfig::getCode, "APP_DEFAULT_MATCH_LENGTH"));
        if (ObjectUtil.isNotEmpty(sysConfig)) {
            MIN_MATCH_LENGTH = Integer.parseInt(sysConfig.getValue());
        } else {
            MIN_MATCH_LENGTH = 0;
        }


        // 抽取样本库
        LambdaQueryWrapper<ProSampleLibrary> queryWrapper = new LambdaQueryWrapper<ProSampleLibrary>()
                .eq(ProSampleLibrary::getProblemId, problemId);

        // 先查询总数量
        long totalCount = 0L;

        if (problemReportConfigParam.getUseAllSample()) {
            totalCount = proSampleLibraryMapper.selectCount(queryWrapper);
        } else {
            totalCount = problemReportConfigParam.getSampleCount();
        }

        List<ProSampleLibrary> proSampleLibraries;

        queryWrapper.orderByDesc(ProSampleLibrary::getSubmitTime)
                .last("LIMIT " + totalCount);
        proSampleLibraries = proSampleLibraryMapper.selectList(queryWrapper);

        // 空直接忽略
        if (ObjectUtil.isEmpty(proSampleLibraries)) {
            log.info("代码克隆检测 -> 忽略空数据");
            return null;
        }

        List<ProSimilarityDetail> proSimilarityDetails = new ArrayList<>();
        AtomicReference<ProSimilarityDetail> maxProSimilarityDetailRef = new AtomicReference<>();

        // 使用迭代器实现单向检测
        Iterator<ProSampleLibrary> outerIterator = proSampleLibraries.iterator();
        int outerIndex = 0;

        while (outerIterator.hasNext()) {
            ProSampleLibrary baseSampleLibrary = outerIterator.next();

            // 创建内层迭代器，从当前外层元素的下一个开始
            Iterator<ProSampleLibrary> innerIterator = proSampleLibraries.listIterator(outerIndex + 1);

            while (innerIterator.hasNext()) {
                ProSampleLibrary compareSampleLibrary = innerIterator.next();

                // 忽略样本用户自身之间的比较
                if (baseSampleLibrary.getUserId().equals(compareSampleLibrary.getUserId())) {
                    continue;
                }

                // 使用一次样本库，那么样本库的访问次数加1
                compareSampleLibrary.setAccessCount(compareSampleLibrary.getAccessCount() + 1);

                CodeSimilarityCalculator.SimilarityDetail similarityDetail = codeSimilarityCalculator.getSimilarityDetail(
                        baseSampleLibrary.getLanguage().toLowerCase(),
                        compareSampleLibrary.getCode(),
                        baseSampleLibrary.getCode(),
                        MIN_MATCH_LENGTH
                );

                ProSimilarityDetail proSimilarityDetail = new ProSimilarityDetail();
                // 基础信息
                proSimilarityDetail.setTaskId(taskId);
                proSimilarityDetail.setTaskType(true);
                proSimilarityDetail.setProblemId(problemId);
                proSimilarityDetail.setLanguage(baseSampleLibrary.getLanguage());
                proSimilarityDetail.setSimilarity(BigDecimal.valueOf(similarityDetail.getSimilarity()));
                // 提交用户
                proSimilarityDetail.setSubmitUser(baseSampleLibrary.getUserId());
                proSimilarityDetail.setSubmitCode(baseSampleLibrary.getCode());
                proSimilarityDetail.setSubmitCodeLength(baseSampleLibrary.getCode().length());
                proSimilarityDetail.setSubmitId(baseSampleLibrary.getSubmitId());
                proSimilarityDetail.setSubmitTime(baseSampleLibrary.getCreateTime());
                proSimilarityDetail.setSubmitTokenName(similarityDetail.getTokenNames1());
                proSimilarityDetail.setSubmitTokenTexts(similarityDetail.getTokenTexts1());

                // 样本信息
                proSimilarityDetail.setOriginUser(compareSampleLibrary.getUserId());
                proSimilarityDetail.setOriginCode(compareSampleLibrary.getCode());
                proSimilarityDetail.setOriginCodeLength(compareSampleLibrary.getCodeLength());
                proSimilarityDetail.setOriginId(compareSampleLibrary.getSubmitId());
                proSimilarityDetail.setOriginTime(compareSampleLibrary.getCreateTime());
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
            }

            outerIndex++;
        }



//            // proSampleLibraries
//        for (ProSampleLibrary baseSampleLibrary : proSampleLibraries) {
//            for (ProSampleLibrary compareSampleLibrary : proSampleLibraries) {
//                // 忽略样本用户自身之间的比较
//                if (baseSampleLibrary.getUserId().equals(compareSampleLibrary.getUserId())) {
//                    continue;
//                }
//
//                // 使用一次样本库，那么样本库的访问次数加1
//                compareSampleLibrary.setAccessCount(compareSampleLibrary.getAccessCount() + 1);
//
//                CodeSimilarityCalculator.SimilarityDetail similarityDetail = codeSimilarityCalculator.getSimilarityDetail(
//                        baseSampleLibrary.getLanguage().toLowerCase(),
//                        compareSampleLibrary.getCode(),
//                        baseSampleLibrary.getCode(),
//                        MIN_MATCH_LENGTH
//                );
//
//                ProSimilarityDetail proSimilarityDetail = new ProSimilarityDetail();
//                // 基础信息
//                proSimilarityDetail.setTaskId(taskId);
//                proSimilarityDetail.setTaskType(true);
//                proSimilarityDetail.setProblemId(problemId);
//                proSimilarityDetail.setLanguage(baseSampleLibrary.getLanguage());
//                proSimilarityDetail.setSimilarity(BigDecimal.valueOf(similarityDetail.getSimilarity()));
//                // 提交用户
//                proSimilarityDetail.setSubmitUser(baseSampleLibrary.getUserId());
//                proSimilarityDetail.setSubmitCode(baseSampleLibrary.getCode());
//                proSimilarityDetail.setSubmitCodeLength(baseSampleLibrary.getCode().length());
//                proSimilarityDetail.setSubmitId(baseSampleLibrary.getSubmitId());
//                proSimilarityDetail.setSubmitTime(baseSampleLibrary.getCreateTime());
//                proSimilarityDetail.setSubmitTokenName(similarityDetail.getTokenNames1());
//                proSimilarityDetail.setSubmitTokenTexts(similarityDetail.getTokenTexts1());
//
//                // 样本信息
//                proSimilarityDetail.setOriginUser(compareSampleLibrary.getUserId());
//                proSimilarityDetail.setOriginCode(compareSampleLibrary.getCode());
//                proSimilarityDetail.setOriginCodeLength(compareSampleLibrary.getCodeLength());
//                proSimilarityDetail.setOriginId(compareSampleLibrary.getSubmitId());
//                proSimilarityDetail.setOriginTime(compareSampleLibrary.getCreateTime());
//                proSimilarityDetail.setOriginTokenName(similarityDetail.getTokenNames2());
//                proSimilarityDetail.setOriginTokenTexts(similarityDetail.getTokenTexts2());
//
//                proSimilarityDetails.add(proSimilarityDetail);
//
//                // 使用原子引用更新
//                maxProSimilarityDetailRef.updateAndGet(currentMax -> {
//                    if (currentMax == null) {
//                        return proSimilarityDetail;
//                    } else {
//                        BigDecimal currentSimilarity = BigDecimal.valueOf(similarityDetail.getSimilarity());
//                        return currentSimilarity.compareTo(currentMax.getSimilarity()) > 0
//                                ? proSimilarityDetail
//                                : currentMax;
//                    }
//                });
//            }
//        }

        proSampleLibraryMapper.updateById(proSampleLibraries); // 更新访问次数

        // 任务详情记录
        proSimilarityDetailMapper.insert(proSimilarityDetails);

        // 任务详情做分析，这里的报告类型是单次提交报告
        ProSimilarityDetail maxProSimilarityDetail = maxProSimilarityDetailRef.get(); // 原子获取
        // 样本数
        int sampleCount = proSampleLibraries.size();

        ProProblem proProblem = proProblemMapper.selectById(problemId);
        if (proProblem == null) {
            return null;
        }

        ProSimilarityReports reports = new ProSimilarityReports();
        reports.setReportType(ReportTypeEnum.PROBLEM_SUBMIT.getValue());
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

        proSimilarityReportsMapper.insert(reports);

        log.info("代码克隆检测 -> 检测完成");

        DataReports dataReports = new DataReports();
        dataReports.setTaskId(taskId);
        dataReports.setReportType(ReportTypeEnum.PROBLEM_SUBMIT.getValue());
        dataReports.setIsSet(false);
        dataReports.setReportId(reports.getId());

        dataReportsMapper.insert(dataReports);

        return dataReports.getId();
    }

    /**
     * 计算相似度分布
     *
     * @param proSimilarityDetails 相似度详情列表
     * @return 分布结果列表
     */
    private List<Integer> calculateSimilarityDistribution(List<ProSimilarityDetail> proSimilarityDetails) {
        int[] distribution = new int[10];
        int totalCount = proSimilarityDetails.size();

        for (ProSimilarityDetail detail : proSimilarityDetails) {
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
    private BigDecimal calculateAverageSimilarity(List<ProSimilarityDetail> proSimilarityDetails) {
        if (proSimilarityDetails == null || proSimilarityDetails.isEmpty()) {
            return BigDecimal.ZERO;
        }

        double totalSimilarity = 0.0;

        for (ProSimilarityDetail detail : proSimilarityDetails) {
            totalSimilarity += detail.getSimilarity().doubleValue();
        }

        return BigDecimal.valueOf(totalSimilarity / proSimilarityDetails.size())
                .setScale(4, RoundingMode.HALF_UP);
    }

    private List<DynamicCloneLevelDetector.CloneLevel> calculateSimilarityDegreeStatistics(List<ProSimilarityDetail> proSimilarityDetails, List<DynamicCloneLevelDetector.CloneLevel> levels) {
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
        for (ProSimilarityDetail detail : proSimilarityDetails) {
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
