package io.charlie.web.oj.modular.task.similarity.handle;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.alibaba.nacos.shaded.com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import io.charlie.web.oj.modular.data.library.entity.DataLibrary;
import io.charlie.web.oj.modular.data.library.service.DataLibraryService;
import io.charlie.web.oj.modular.data.problem.entity.DataProblem;
import io.charlie.web.oj.modular.data.problem.mapper.DataProblemMapper;
import io.charlie.web.oj.modular.data.reports.entity.TaskReports;
import io.charlie.web.oj.modular.data.reports.mapper.TaskReportsMapper;
import io.charlie.web.oj.modular.data.similarity.dto.CloneLevel;
import io.charlie.web.oj.modular.data.similarity.dto.TaskReportStats;
import io.charlie.web.oj.modular.data.similarity.entity.TaskSimilarity;
import io.charlie.web.oj.modular.data.similarity.mapper.TaskSimilarityMapper;
import io.charlie.web.oj.modular.data.submit.entity.DataSubmit;
import io.charlie.web.oj.modular.data.submit.mapper.DataSubmitMapper;
import io.charlie.web.oj.modular.task.similarity.config.SimilarityConfigProperties;
import io.charlie.web.oj.modular.task.similarity.dto.SimilaritySubmitDto;
import io.charlie.web.oj.modular.task.similarity.enums.CloneLevelEnum;
import io.charlie.web.oj.modular.task.similarity.enums.ReportTypeEnum;
import io.charlie.web.oj.modular.task.similarity.mq.SimilarlyQueueProperties;
import io.charlie.web.oj.utils.similarity.utils.CodeTokenUtil;
import io.charlie.web.oj.utils.similarity.utils.SimilarityCalculator;
import io.charlie.web.oj.utils.similarity.utils.TokenDetail;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class SingleSimilarityHandleMessage {

    private final RabbitTemplate rabbitTemplate;
    private final DataSubmitMapper dataSubmitMapper;
    private final DataProblemMapper dataProblemMapper;
    private final DataLibraryService dataLibraryService;
    private final TaskSimilarityMapper taskSimilarityMapper;
    private final TaskReportsMapper taskReportsMapper;
    private final SimilarityCalculator codeSimilarityCalculator;

    private final SimilarityConfigProperties similarityConfigProperties;

    private final SimilarlyQueueProperties properties;

    private final CodeTokenUtil codeTokenUtil;

    // 创建专用线程池
    private final ExecutorService similarityExecutor = Executors.newFixedThreadPool(
            Runtime.getRuntime().availableProcessors() * 2, // 根据CPU核心数调整
            new ThreadFactoryBuilder().setNameFormat("similarity-pool-%d").build()
    );

    public void sendSimilarity(SimilaritySubmitDto dto) {
        log.info("代码克隆检测 -> 发送检测消息：{}", JSONUtil.toJsonStr(dto));
        rabbitTemplate.convertAndSend(
                properties.getSingle().getExchange(),
                properties.getSingle().getRoutingKey(),
                dto
        );
    }

    @Transactional
    @RabbitListener(queues = "${oj.mq.similarity.single.queue}", concurrency = "10-20")
    public void receiveSimilarity(SimilaritySubmitDto submitDto) {
        try {
            log.info("代码克隆检测 -> 接收检测消息：{}", JSONUtil.toJsonStr(submitDto));

            List<DataLibrary> dataLibraries = new ArrayList<>();

            if (submitDto.getIsSet()) {
                Integer singleSetProblemLibrarySize = similarityConfigProperties.getSingleSetProblemLibrarySize();
                LambdaQueryWrapper<DataLibrary> query = new LambdaQueryWrapper<DataLibrary>()
                        .eq(DataLibrary::getIsSet, Boolean.TRUE) // 题集
                        .eq(DataLibrary::getSetId, submitDto.getSetId()) // 题集ID
                        .eq(DataLibrary::getProblemId, submitDto.getProblemId()) // 题目ID
                        .eq(DataLibrary::getLanguage, submitDto.getLanguage()) // 语言
                        .ne(DataLibrary::getUserId, submitDto.getUserId()) // 非该用户
                        .orderByDesc(DataLibrary::getUpdateTime)
                        .last("LIMIT " + singleSetProblemLibrarySize); // 动态设置LIMIT
                dataLibraries = dataLibraryService.list(query);
            } else {
                Integer singleProblemLibrarySize = similarityConfigProperties.getSingleProblemLibrarySize();
                LambdaQueryWrapper<DataLibrary> query = new LambdaQueryWrapper<DataLibrary>()
                        .eq(DataLibrary::getIsSet, Boolean.FALSE) // 非题集
                        .eq(DataLibrary::getProblemId, submitDto.getProblemId()) // 题目ID
                        .eq(DataLibrary::getLanguage, submitDto.getLanguage()) // 语言
                        .ne(DataLibrary::getUserId, submitDto.getUserId()) // 非该用户
                        .orderByDesc(DataLibrary::getUpdateTime)
                        .last("LIMIT " + singleProblemLibrarySize); // 动态设置LIMIT
                dataLibraries = dataLibraryService.list(query);
            }

            calculateTask(submitDto, dataLibraries);

            saveSimilarityReport(submitDto);
        } catch (Exception e) {
            log.error("代码克隆检测处理失败，消息体：{}", JSONUtil.toJsonStr(submitDto), e);
            // 可以考虑重试机制或死信队列处理
            throw e; // 保持事务回滚
        }
    }

    private void calculateTask(SimilaritySubmitDto submitDto, List<DataLibrary> dataLibraries) {
        log.info("代码克隆检测 -> 计算相似度");
        List<TaskSimilarity> details = new ArrayList<>();
        if (dataLibraries.isEmpty()) {
            log.info("代码克隆检测 -> 样本为空");
            return;
        }

        TokenDetail tokensDetail = codeTokenUtil.getCodeTokensDetail(
                submitDto.getLanguage().toLowerCase(),
                submitDto.getCode()
        );

        log.info("代码克隆检测 -> 样本数量：{}", dataLibraries.size());

        List<CompletableFuture<TaskSimilarity>> futures = dataLibraries.stream()
                .map(dataLibrary -> CompletableFuture.supplyAsync(() -> {
                    BigDecimal similarity = codeSimilarityCalculator.calculateBigDecimalFinal(
                            tokensDetail.getTokens(),
                            dataLibrary.getCodeToken(),
                            submitDto.getIsSet() ?
                                    similarityConfigProperties.getSetSingleSubmitSensitivity() :
                                    similarityConfigProperties.getProblemSingleSubmitSensitivity()
                    );

                    TaskSimilarity taskSimilarity = new TaskSimilarity();
                    // 基础信息
                    taskSimilarity.setTaskId(submitDto.getTaskId());
                    taskSimilarity.setTaskType(submitDto.getTaskType());
                    taskSimilarity.setProblemId(submitDto.getProblemId());
                    taskSimilarity.setSetId(submitDto.getSetId());
                    taskSimilarity.setIsSet(submitDto.getIsSet());
                    taskSimilarity.setLanguage(dataLibrary.getLanguage());
                    taskSimilarity.setSimilarity(similarity);

                    // 提交信息
                    taskSimilarity.setSubmitUser(submitDto.getUserId());
                    taskSimilarity.setSubmitCode(submitDto.getCode());
                    taskSimilarity.setSubmitCodeLength(submitDto.getCode().length());
                    taskSimilarity.setSubmitId(submitDto.getId());
                    taskSimilarity.setSubmitTime(submitDto.getCreateTime());
                    taskSimilarity.setSubmitTokenName(tokensDetail.getTokenNames());
                    taskSimilarity.setSubmitTokenTexts(tokensDetail.getTokenTexts());

                    // 样本信息
                    taskSimilarity.setOriginUser(dataLibrary.getUserId());
                    taskSimilarity.setOriginCode(dataLibrary.getCode());
                    taskSimilarity.setOriginCodeLength(dataLibrary.getCodeLength());
                    taskSimilarity.setOriginId(dataLibrary.getSubmitId());
                    taskSimilarity.setOriginTime(dataLibrary.getCreateTime());
                    taskSimilarity.setOriginTokenName(dataLibrary.getCodeTokenName());
                    taskSimilarity.setOriginTokenTexts(dataLibrary.getCodeTokenTexts());

                    return taskSimilarity;
                }, similarityExecutor))
                .toList();

        // 等待所有任务完成
        details = futures.stream()
                .map(CompletableFuture::join)
                .collect(Collectors.toList());

        taskSimilarityMapper.insert(details);
    }

    void saveSimilarityReport(SimilaritySubmitDto submitDto) {
        DataProblem dataProblem = dataProblemMapper.selectById(submitDto.getProblemId());

        TaskReports taskReports = BeanUtil.toBean(submitDto, TaskReports.class);
        taskReports.setId(null);
        if (submitDto.getIsSet()) {
            log.info("代码克隆检测 -> 单题集报告");
            taskReports.setReportType(ReportTypeEnum.SET_SINGLE_SUBMIT.getValue());
        } else {
            log.info("代码克隆检测 -> 单题目报告");
            taskReports.setReportType(ReportTypeEnum.PROBLEM_SINGLE_SUBMIT.getValue());
        }

        taskReports.setThreshold(dataProblem.getThreshold());

        // 从数据库中查询
        TaskReportStats taskReportStats = taskSimilarityMapper.selectSimilarityStats(
                submitDto.getTaskId(), // 任务ID，判题结束后生成 IdUtil.objectId();
                submitDto.getProblemId(), // 题目ID
                submitDto.getSetId(), // 题集ID
                submitDto.getIsSet() ? 1 : 0  // 是否是题集
        );
        if (taskReportStats.getAvgSimilarity() == null ||
                taskReportStats.getMaxSimilarity() == null ||
                taskReportStats.getAvgSimilarity().equals(BigDecimal.ZERO) ||
                taskReportStats.getMaxSimilarity().equals(BigDecimal.ZERO)
        ) {
            log.info("代码克隆检测 -> 相似为空 忽略报告");
            dataSubmitMapper.update(new LambdaUpdateWrapper<DataSubmit>()
                    .eq(DataSubmit::getId, submitDto.getId())
                    .set(DataSubmit::getSimilarity, BigDecimal.ZERO)
                    .set(DataSubmit::getSimilarityCategory, CloneLevelEnum.NOT_DETECTED.getValue())
//                    .set(DataSubmit::getIsFinish, Boolean.TRUE)
                    .set(DataSubmit::getReportId, null)
            );
            return;
        }

        taskReports.setSampleCount(taskReportStats.getSampleCount());
        taskReports.setSimilarityGroupCount(taskReportStats.getGroupCount());
        taskReports.setAvgSimilarity(taskReportStats.getAvgSimilarity());
        taskReports.setMaxSimilarity(taskReportStats.getMaxSimilarity());

        // 获取相似度分布数组
        List<Integer> distributionArray = taskSimilarityMapper.selectSimilarityDistribution(submitDto.getTaskId());
        taskReports.setSimilarityDistribution(distributionArray);

        // 获取程度统计
        List<CloneLevel> degreeStats = taskSimilarityMapper.selectDegreeStatistics(submitDto.getTaskId(), dataProblem.getThreshold());
        taskReports.setDegreeStatistics(degreeStats);

        taskReportsMapper.insert(taskReports);

        log.info("更新提交 {} 最大相似度 {} 平均相似度 {}", submitDto.getId(), taskReportStats.getMaxSimilarity(), taskReportStats.getAvgSimilarity());
        String degreeBySimilarity = taskSimilarityMapper.getDegreeBySimilarity(taskReportStats.getMaxSimilarity(), dataProblem.getThreshold());

        dataSubmitMapper.update(new LambdaUpdateWrapper<DataSubmit>()
                .eq(DataSubmit::getId, submitDto.getId())
                .set(DataSubmit::getSimilarity, taskReportStats.getMaxSimilarity())
                .set(DataSubmit::getSimilarityCategory, degreeBySimilarity)
//                .set(DataSubmit::getIsFinish, Boolean.TRUE)
                .set(StrUtil.isNotEmpty(taskReports.getId()), DataSubmit::getReportId, taskReports.getId())
        );
    }
}