package io.charlie.web.oj.modular.task.similarity.handle;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
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
import io.charlie.web.oj.modular.task.similarity.mq.CommonSimilarityQueue;
import io.charlie.web.oj.utils.similarity.utils.CodeSimilarityCalculator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.*;

@Slf4j
@Component
@RequiredArgsConstructor
public class SimilarityHandleMessage {

    private final RabbitTemplate rabbitTemplate;
    private final DataSubmitMapper dataSubmitMapper;
    private final DataProblemMapper dataProblemMapper;
    private final DataLibraryService dataLibraryService;
    private final TaskSimilarityMapper taskSimilarityMapper;
    private final TaskReportsMapper taskReportsMapper;
    private final CodeSimilarityCalculator codeSimilarityCalculator;

    private final SimilarityConfigProperties similarityConfigProperties;

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
        try {
            log.info("代码克隆检测 -> 接收检测消息：{}", JSONUtil.toJsonStr(similaritySubmitDto));

            // setId参数
            List<String> setIdParam = (similaritySubmitDto.getSetId() == null || similaritySubmitDto.getSetId().isEmpty())
                    ? null
                    : List.of(similaritySubmitDto.getSetId());
            // problemId参数
            List<String> problemIdParam = (similaritySubmitDto.getProblemId() == null || similaritySubmitDto.getProblemId().isEmpty())
                    ? null
                    : List.of(similaritySubmitDto.getProblemId());

            dataLibraryService.processCodeLibrariesInBatches(
                    similaritySubmitDto.getIsSet(),
                    similaritySubmitDto.getLanguage(),
                    problemIdParam,
                    setIdParam,
                    null,
                    similaritySubmitDto.getIsSet() ? similarityConfigProperties.getSetSingleSubmitBatchSize() : similarityConfigProperties.getProblemSingleSubmitBatchSize(),
                    null,
                    null,
                    similaritySubmitDto.getUserId(),
                    similarityConfigProperties.getProblemSingleSubmitBatchEndSize(),
                    dataLibraries -> calculateTaskSimilarities(similaritySubmitDto, dataLibraries)
            );

            saveSimilarityReportDataFromDB(similaritySubmitDto);
        } catch (Exception e) {
            log.error("代码克隆检测处理失败，消息体：{}", JSONUtil.toJsonStr(similaritySubmitDto), e);
            // 可以考虑重试机制或死信队列处理
            throw e; // 保持事务回滚
        }
    }

    private List<TaskSimilarity> calculateTaskSimilarities(SimilaritySubmitDto similaritySubmitDto, List<DataLibrary> dataLibraries) {
        log.info("代码克隆检测 -> 计算相似度");
        List<TaskSimilarity> details = new ArrayList<>();
        if (dataLibraries.isEmpty()) {
            log.info("代码克隆检测 -> 样本为空");
            return details;
        }

        log.info("代码克隆检测 -> 样本数量：{}", dataLibraries.size());
        for (DataLibrary dataLibrary : dataLibraries) {
            CodeSimilarityCalculator.SimilarityDetail similarityDetail = codeSimilarityCalculator.getSimilarityDetail(
                    similaritySubmitDto.getLanguage().toLowerCase(),
                    similaritySubmitDto.getCode(),
                    dataLibrary.getCode(),
                    similaritySubmitDto.getIsSet() ? similarityConfigProperties.getSetSingleSubmitSensitivity() : similarityConfigProperties.getProblemSingleSubmitSensitivity()
            );

            TaskSimilarity taskSimilarity = new TaskSimilarity();
            // 基础信息
            taskSimilarity.setTaskId(similaritySubmitDto.getTaskId());
            taskSimilarity.setTaskType(similaritySubmitDto.getTaskType());
            taskSimilarity.setProblemId(similaritySubmitDto.getProblemId());
            taskSimilarity.setSetId(similaritySubmitDto.getSetId());
            taskSimilarity.setIsSet(similaritySubmitDto.getIsSet());
            taskSimilarity.setLanguage(dataLibrary.getLanguage());
            taskSimilarity.setSimilarity(BigDecimal.valueOf(similarityDetail.getSimilarity()));

            // 提交信息
            taskSimilarity.setSubmitUser(similaritySubmitDto.getUserId());
            taskSimilarity.setSubmitCode(similaritySubmitDto.getCode());
            taskSimilarity.setSubmitCodeLength(similaritySubmitDto.getCode().length());
            taskSimilarity.setSubmitId(similaritySubmitDto.getId());
            taskSimilarity.setSubmitTime(similaritySubmitDto.getCreateTime());
            taskSimilarity.setSubmitTokenName(similarityDetail.getSubmitTokenNames());
            taskSimilarity.setSubmitTokenTexts(similarityDetail.getSubmitTokenTexts());

            // 样本信息
            taskSimilarity.setOriginUser(dataLibrary.getUserId());
            taskSimilarity.setOriginCode(dataLibrary.getCode());
            taskSimilarity.setOriginCodeLength(dataLibrary.getCodeLength());
            taskSimilarity.setOriginId(dataLibrary.getSubmitId());
            taskSimilarity.setOriginTime(dataLibrary.getCreateTime());
            taskSimilarity.setOriginTokenName(similarityDetail.getLibraryTokenNames());
            taskSimilarity.setOriginTokenTexts(similarityDetail.getLibraryTokenTexts());

            // 增加
            details.add(taskSimilarity);
        }

        // 直接插入
        taskSimilarityMapper.insert(details);
        return List.of();
    }

    void saveSimilarityReportDataFromDB(SimilaritySubmitDto similaritySubmitDto) {
        DataProblem dataProblem = dataProblemMapper.selectById(similaritySubmitDto.getProblemId());

        TaskReports taskReports = BeanUtil.toBean(similaritySubmitDto, TaskReports.class);
        taskReports.setId(null);
        if (similaritySubmitDto.getIsSet()) {
            log.info("代码克隆检测 -> 单题集报告");
            taskReports.setReportType(ReportTypeEnum.SET_SINGLE_SUBMIT.getValue());
        } else {
            log.info("代码克隆检测 -> 单题目报告");
            taskReports.setReportType(ReportTypeEnum.PROBLEM_SINGLE_SUBMIT.getValue());
        }

        taskReports.setThreshold(dataProblem.getThreshold());

        // 从数据库中查询
        TaskReportStats taskReportStats = taskSimilarityMapper.selectSimilarityStats(similaritySubmitDto.getTaskId(), similaritySubmitDto.getProblemId(), similaritySubmitDto.getSetId(), similaritySubmitDto.getIsSet() ? 1 : 0);
        if (taskReportStats.getAvgSimilarity() == null || taskReportStats.getMaxSimilarity() == null || taskReportStats.getAvgSimilarity().equals(BigDecimal.ZERO) || taskReportStats.getMaxSimilarity().equals(BigDecimal.ZERO)) {
            log.info("代码克隆检测 -> 相似为空 忽略报告");
            dataSubmitMapper.update(new LambdaUpdateWrapper<DataSubmit>()
                    .eq(DataSubmit::getId, similaritySubmitDto.getId())
                    .set(DataSubmit::getSimilarity, BigDecimal.ZERO)
                    .set(DataSubmit::getSimilarityCategory, CloneLevelEnum.NOT_DETECTED.getValue())
                    .set(DataSubmit::getIsFinish, Boolean.TRUE)
                    .set(DataSubmit::getReportId, null)
            );
            return;
        }

        taskReports.setSampleCount(taskReportStats.getSampleCount());
        taskReports.setSimilarityGroupCount(taskReportStats.getGroupCount());
        taskReports.setAvgSimilarity(taskReportStats.getAvgSimilarity());
        taskReports.setMaxSimilarity(taskReportStats.getMaxSimilarity());

        // 获取相似度分布数组
        List<Integer> distributionArray = taskSimilarityMapper.selectSimilarityDistribution(similaritySubmitDto.getTaskId());
        taskReports.setSimilarityDistribution(distributionArray);

        // 获取程度统计
        List<CloneLevel> degreeStats = taskSimilarityMapper.selectDegreeStatistics(similaritySubmitDto.getTaskId(), dataProblem.getThreshold());
        taskReports.setDegreeStatistics(degreeStats);

        taskReportsMapper.insert(taskReports);

        log.info("更新提交 {} 最大相似度 {} 平均相似度 {}", similaritySubmitDto.getId(), taskReportStats.getMaxSimilarity(), taskReportStats.getAvgSimilarity());
        String degreeBySimilarity = taskSimilarityMapper.getDegreeBySimilarity(taskReportStats.getMaxSimilarity(), dataProblem.getThreshold());

        dataSubmitMapper.update(new LambdaUpdateWrapper<DataSubmit>()
                .eq(DataSubmit::getId, similaritySubmitDto.getId())
                .set(DataSubmit::getSimilarity, taskReportStats.getMaxSimilarity())
                .set(DataSubmit::getSimilarityCategory, degreeBySimilarity)
                .set(DataSubmit::getIsFinish, Boolean.TRUE)
                .set(StrUtil.isNotEmpty(taskReports.getId()), DataSubmit::getReportId, taskReports.getId())
        );

    }
}