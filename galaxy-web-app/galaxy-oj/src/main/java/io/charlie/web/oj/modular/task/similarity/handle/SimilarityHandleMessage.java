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
import io.charlie.web.oj.modular.task.similarity.msg.SimilarityMessage;
import io.charlie.web.oj.modular.task.similarity.msg.SimilarityResultMessage;
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
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

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
    private final SimilarityCalculator codeSimilarityCalculator;

    private final SimilarityConfigProperties similarityConfigProperties;

    private final SimilarlyQueueProperties properties;

    private final CodeTokenUtil codeTokenUtil;

    public void sendSimilarity(SimilarityMessage message) {
        log.info("代码克隆检测 -> 发送检测消息：{}", JSONUtil.toJsonStr(message));
        rabbitTemplate.convertAndSend(
                properties.getCommon().getExchange(),
                properties.getCommon().getRoutingKey(),
                message
        );
    }

    @Transactional
    @RabbitListener(queues = "${oj.mq.similarity.result.queue}", concurrency = "10-20")
    public void receiveSimilarity(SimilarityResultMessage resultMessage) {
        try {
            log.info("代码克隆检测 -> 接收检测消息：{}", JSONUtil.toJsonStr(resultMessage));

            if (resultMessage.getSkip()) {
                log.info("代码克隆检测 -> 忽略");
                return;
            }

            DataProblem dataProblem = dataProblemMapper.selectById(resultMessage.getProblemId());

            TaskReports taskReports = new TaskReports();
            taskReports.setId(null);
            taskReports.setTaskId(resultMessage.getTaskId());
            taskReports.setSetId(resultMessage.getSetId());
            taskReports.setProblemId(resultMessage.getProblemId());
            taskReports.setIsSet(resultMessage.getIsSet());
            taskReports.setThreshold(dataProblem.getThreshold());
            taskReports.setThreshold(dataProblem.getThreshold());

            if (resultMessage.getIsSet()) {
                log.info("代码克隆检测 -> 单题集报告");
                taskReports.setReportType(ReportTypeEnum.SET_SINGLE_SUBMIT.getValue());
            } else {
                log.info("代码克隆检测 -> 单题目报告");
                taskReports.setReportType(ReportTypeEnum.PROBLEM_SINGLE_SUBMIT.getValue());
            }

            // 从数据库中查询
            TaskReportStats taskReportStats = taskSimilarityMapper.selectSimilarityStats(
                    resultMessage.getTaskId(), // 任务ID，判题结束后生成 IdUtil.objectId();
                    resultMessage.getProblemId(), // 题目ID
                    resultMessage.getSetId(), // 题集ID
                    resultMessage.getIsSet() ? 1 : 0  // 是否是题集
            );
            if (taskReportStats.getAvgSimilarity() == null ||
                    taskReportStats.getMaxSimilarity() == null ||
                    taskReportStats.getAvgSimilarity().equals(BigDecimal.ZERO) ||
                    taskReportStats.getMaxSimilarity().equals(BigDecimal.ZERO)
            ) {
                log.info("代码克隆检测 -> 相似为空 忽略报告");
                dataSubmitMapper.update(new LambdaUpdateWrapper<DataSubmit>()
                        .eq(DataSubmit::getId, resultMessage.getSubmitId())
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
            List<Integer> distributionArray = taskSimilarityMapper.selectSimilarityDistribution(resultMessage.getTaskId());
            taskReports.setSimilarityDistribution(distributionArray);

            // 获取程度统计
            List<CloneLevel> degreeStats = taskSimilarityMapper.selectDegreeStatistics(resultMessage.getTaskId(), resultMessage.getThreshold());
            taskReports.setDegreeStatistics(degreeStats);

            taskReportsMapper.insert(taskReports);

            log.info("更新提交 {} 最大相似度 {} 平均相似度 {}", resultMessage.getSubmitId(), taskReportStats.getMaxSimilarity(), taskReportStats.getAvgSimilarity());
            String degreeBySimilarity = taskSimilarityMapper.getDegreeBySimilarity(taskReportStats.getMaxSimilarity(), dataProblem.getThreshold());

            dataSubmitMapper.update(new LambdaUpdateWrapper<DataSubmit>()
                    .eq(DataSubmit::getId, resultMessage.getSubmitId())
                    .set(DataSubmit::getSimilarity, taskReportStats.getMaxSimilarity())
                    .set(DataSubmit::getSimilarityCategory, degreeBySimilarity)
                    .set(DataSubmit::getIsFinish, Boolean.TRUE)
                    .set(StrUtil.isNotEmpty(taskReports.getId()), DataSubmit::getReportId, taskReports.getId())
            );

        } catch (Exception e) {
            log.error("代码克隆检测处理失败，消息体：{}", JSONUtil.toJsonStr(resultMessage), e);
            throw e;
        }
    }
}