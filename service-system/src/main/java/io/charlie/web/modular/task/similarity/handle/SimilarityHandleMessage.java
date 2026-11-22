//package io.charlie.web.modular.task.similarity.handle;
//
//import cn.hutool.json.JSONUtil;
//import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
//import io.charlie.web.modular.data.library.entity.DataLibrary;
//import io.charlie.web.modular.data.library.mapper.DataLibraryMapper;
//import io.charlie.web.modular.data.library.service.DataLibraryService;
//import io.charlie.web.modular.data.problem.mapper.DataProblemMapper;
//import io.charlie.web.modular.data.reports.mapper.TaskReportsMapper;
//import io.charlie.web.modular.data.similarity.entity.TaskSimilarity;
//import io.charlie.web.modular.data.similarity.mapper.TaskSimilarityMapper;
//import io.charlie.web.modular.data.solved.mapper.DataSolvedMapper;
//import io.charlie.web.modular.data.submit.mapper.DataSubmitMapper;
//import io.charlie.web.modular.sys.config.mapper.SysConfigMapper;
//import io.charlie.web.modular.task.similarity.data.SimilarityResult;
//import io.charlie.web.modular.task.similarity.dto.BatchSimilaritySubmitDto;
//import io.charlie.web.modular.task.similarity.dto.SimilaritySubmitDto;
//import io.charlie.web.modular.task.similarity.mq.CommonSimilarityQueue;
//import io.charlie.web.oj.utils.similarity.utils.CodeSimilarityCalculator;
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import org.dromara.trans.service.impl.TransService;
//import org.springframework.amqp.rabbit.annotation.RabbitListener;
//import org.springframework.amqp.rabbit.core.RabbitTemplate;
//import org.springframework.stereotype.Component;
//import org.springframework.transaction.annotation.Transactional;
//
//import java.math.BigDecimal;
//import java.util.ArrayList;
//import java.util.Collections;
//import java.util.List;
//import java.util.Map;
//import java.util.stream.Collectors;
//
///**
// * @author ZhangJiangHu
// * @version v1.0
// * @date 20/09/2025
// * @description 相似度处理
// */
//@Slf4j
//@Component
//@RequiredArgsConstructor
//public class BatchSimilarityHandleMessage {
//    private final RabbitTemplate rabbitTemplate;
//    private final DataSubmitMapper dataSubmitMapper;
//    private final DataSolvedMapper dataSolvedMapper;
//    private final DataProblemMapper dataProblemMapper;
//    private final DataLibraryService dataLibraryService;
//    private final DataLibraryMapper dataLibraryMapper;
//    private final SysConfigMapper sysConfigMapper;
//    private final TaskSimilarityMapper taskSimilarityMapper;
//    private final TaskReportsMapper taskReportsMapper;
//    private final CodeSimilarityCalculator codeSimilarityCalculator;
//    private final TransService transService;
//
//    public void sendSimilarity(BatchSimilaritySubmitDto batchSimilaritySubmitDto) {
//        // 发送消息
//        rabbitTemplate.convertAndSend(
//                CommonSimilarityQueue.BATCH_EXCHANGE,
//                CommonSimilarityQueue.BATCH_ROUTING_KEY,
//                batchSimilaritySubmitDto
//        );
//    }
//
//
//    @Transactional
//    @RabbitListener(queues = CommonSimilarityQueue.BATCH_QUEUE, concurrency = "5-10")
//    public void receiveSimilarity(BatchSimilaritySubmitDto batchSimilaritySubmitDto) {
//        try {
//            log.info("代码克隆检测 -> 接收批量检测消息：{}", JSONUtil.toJsonStr(batchSimilaritySubmitDto));
//
//            // problemId参数
//            List<String> problemIdParam = batchSimilaritySubmitDto.getProblemIds();
//
//            if (batchSimilaritySubmitDto.getIsSet()) {
//                List<DataLibrary> dataLibraries = dataLibraryMapper.selectList(new LambdaQueryWrapper<DataLibrary>()
//                        .eq(DataLibrary::getIsSet, Boolean.TRUE) // 是题集
//                        .eq(DataLibrary::getSetId, batchSimilaritySubmitDto.getSetId()) // 这个题集的
//                        .in(DataLibrary::getProblemId, problemIdParam) // 这个题集的题目
//                        .eq(DataLibrary::getLanguage, batchSimilaritySubmitDto.getLanguage()) // 这个题集的这个题目的语言
//                        .orderByDesc(DataLibrary::getUpdateTime)  // 先按更新时间倒序
//                        .orderByDesc(DataLibrary::getCreateTime)  // 再按创建时间倒序
//                        .last("LIMIT 1000") // 限制1000条数据
//                );
//                log.info("代码克隆检测 -> 样本数量：{}", dataLibraries.size());
//
//                // 按题目id进行分组，分组进行检测
//                Map<String, List<DataLibrary>> groupByProblemIdMap = dataLibraries.stream()
//                        .collect(Collectors.groupingBy(DataLibrary::getProblemId));
//
//                List<TaskSimilarity> details = new ArrayList<>();
//                // 按组检测
//                for (Map.Entry<String, List<DataLibrary>> entry : groupByProblemIdMap.entrySet()) {
//                    List<DataLibrary> libraries = entry.getValue();
//                    for (DataLibrary submit : libraries) {
//                        for (DataLibrary library : libraries) {
//                            // 不进行自检测
//                            if (submit.getId().equals(library.getId())) {
//                                continue;
//                            }
//                            // TODO 检测
//                            // details add 方法
//                        }
//                    }
//                }
//
//                // 调用插入
//                //  taskSimilarityMapper.insert(details);
//            } else {
//                List<DataLibrary> dataLibraries = dataLibraryMapper.selectList(new LambdaQueryWrapper<DataLibrary>()
//                        .eq(DataLibrary::getIsSet, Boolean.FALSE) // 不是题集
//                        .eq(DataLibrary::getProblemId, problemIdParam.getFirst()) // 这个题目
//                        .eq(DataLibrary::getLanguage, batchSimilaritySubmitDto.getLanguage()) // 这个题目的语言
//                        .orderByDesc(DataLibrary::getUpdateTime)  // 先按更新时间倒序
//                        .orderByDesc(DataLibrary::getCreateTime)  // 再按创建时间倒序
//                        .last("LIMIT 1000") // 限制1000条数据
//                );
//                log.info("代码克隆检测 -> 样本数量：{}", dataLibraries.size());
//
//                List<TaskSimilarity> details = new ArrayList<>();
//                for (DataLibrary submit : dataLibraries) {
//                    for (DataLibrary library : dataLibraries) {
//                        // 不进行自检测
//                        if (submit.getId().equals(library.getId())) {
//                            continue;
//                        }
//                        // TODO 检测
//                        // details add 方法
//                    }
//                }
//
//                // 调用插入
//                //  taskSimilarityMapper.insert(details);
//            }
//        } catch (Exception e) {
//            log.error("代码克隆检测处理失败，消息体：{}", JSONUtil.toJsonStr(batchSimilaritySubmitDto), e);
//            // 可以考虑重试机制或死信队列处理
//            throw e; // 保持事务回滚
//        }
//    }
//
//}


package io.charlie.web.modular.task.similarity.handle;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import io.charlie.web.modular.data.reports.entity.TaskReports;
import io.charlie.web.modular.data.reports.mapper.TaskReportsMapper;
import io.charlie.web.modular.data.similarity.dto.CloneLevel;
import io.charlie.web.modular.data.similarity.dto.TaskReportStats;
import io.charlie.web.modular.data.similarity.mapper.TaskSimilarityMapper;
import io.charlie.web.modular.task.similarity.dto.SimilaritySubmitDto;
import io.charlie.web.modular.task.similarity.dto.SimilaritySubmitResultDto;
import io.charlie.web.modular.task.similarity.enums.SimilarityExecStatus;
import io.charlie.web.modular.task.similarity.mq.SimilarlyQueueProperties;
import io.charlie.web.modular.task.similarity.service.SimilarityProgressService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * @author ZhangJiangHu
 * @version v1.0
 * @date 20/09/2025
 * @description 相似度处理：发任务 + 收结果并回写报告状态
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class SimilarityHandleMessage {
    private final RabbitTemplate rabbitTemplate;
    private final TaskSimilarityMapper taskSimilarityMapper;
    private final TaskReportsMapper taskReportsMapper;
    private final SimilarityProgressService progressService;

    private final SimilarlyQueueProperties properties;

    public void sendSimilarity(SimilaritySubmitDto similaritySubmitDto) {
        log.info("代码克隆检测 -> 发送检测消息：{}", JSONUtil.toJsonStr(similaritySubmitDto));
        rabbitTemplate.convertAndSend(
                properties.getCommon().getExchange(),
                properties.getCommon().getRoutingKey(),
                similaritySubmitDto
        );
    }

    @Transactional
    @RabbitListener(queues = "${system.mq.similarity.result.queue}", concurrency = "1")
    public void receiveSimilarity(SimilaritySubmitResultDto submitResultDto) {
        // 1. 基础校验
        if (submitResultDto == null) {
            log.error("接收到的消息为空");
            return;
        }
        log.info("收到相似检测任务消息，任务ID: {}, 报告ID: {}, isOk={}",
                submitResultDto.getTaskId(),
                submitResultDto.getReportId(),
                submitResultDto.getIsOk());

        if (submitResultDto.getTaskId() == null || submitResultDto.getReportId() == null) {
            log.error("任务ID或报告ID为空，任务ID: {}, 报告ID: {}",
                    submitResultDto.getTaskId(),
                    submitResultDto.getReportId());
            return;
        }

        try {
            // 2. worker 明确失败：只标记 FAILED，不假装完成统计
            if (Boolean.FALSE.equals(submitResultDto.getIsOk())) {
                markFailed(submitResultDto.getReportId(), submitResultDto.getTaskId(),
                        "Similarity worker reported failure (insufficient samples or compute error)");
                return;
            }

            // 3. 汇总 task_similarity 统计
            TaskReportStats taskReportStats = taskSimilarityMapper.selectSimilarityStatsByTaskId(submitResultDto.getTaskId());
            if (taskReportStats == null) {
                log.warn("未找到任务 {} 的统计信息，使用默认值", submitResultDto.getTaskId());
                taskReportStats = new TaskReportStats();
                taskReportStats.setSampleCount(0);
                taskReportStats.setGroupCount(0);
                taskReportStats.setAvgSimilarity(BigDecimal.ZERO);
                taskReportStats.setMaxSimilarity(BigDecimal.ZERO);
            }

            List<Integer> distributionArray = taskSimilarityMapper.selectSimilarityDistribution(submitResultDto.getTaskId());
            if (distributionArray == null) {
                distributionArray = new ArrayList<>();
            }

            List<CloneLevel> degreeStats = taskSimilarityMapper.selectDegreeStatistics(
                    submitResultDto.getTaskId(),
                    submitResultDto.getThreshold()
            );
            if (degreeStats == null) {
                degreeStats = new ArrayList<>();
            }

            // 4. 回写统计 + COMPLETED
            TaskReports taskReports = new TaskReports();
            taskReports.setId(submitResultDto.getReportId());
            taskReports.setTaskId(submitResultDto.getTaskId());
            taskReports.setSampleCount(taskReportStats.getSampleCount());
            taskReports.setSimilarityGroupCount(taskReportStats.getGroupCount());
            taskReports.setAvgSimilarity(taskReportStats.getAvgSimilarity());
            taskReports.setMaxSimilarity(taskReportStats.getMaxSimilarity());
            taskReports.setSimilarityDistribution(distributionArray);
            taskReports.setDegreeStatistics(degreeStats);
            taskReports.setExecStatus(SimilarityExecStatus.COMPLETED.getValue());
            taskReports.setExecMessage(null);

            int updateCount = taskReportsMapper.updateById(taskReports);
            if (updateCount == 0) {
                log.warn("未更新任何记录，报告ID: {}, 任务ID: {}",
                        submitResultDto.getReportId(),
                        submitResultDto.getTaskId());
            }
            progressService.completeProgress(submitResultDto.getTaskId());

            log.info("任务 {} 统计完成 - 样本数: {}, 相似组数: {}, 最大相似度: {}, 平均相似度: {}",
                    submitResultDto.getTaskId(),
                    taskReportStats.getSampleCount(),
                    taskReportStats.getGroupCount(),
                    taskReportStats.getMaxSimilarity(),
                    taskReportStats.getAvgSimilarity());

        } catch (Exception e) {
            log.error("处理相似检测任务失败，任务ID: {}, 报告ID: {}, 错误信息: {}",
                    submitResultDto.getTaskId(),
                    submitResultDto.getReportId(),
                    e.getMessage(), e);
            // 5. 消费异常落库 FAILED，避免列表一直 PROCESSING；再抛出触发 MQ 重试策略由容器决定
            try {
                markFailed(submitResultDto.getReportId(), submitResultDto.getTaskId(),
                        abbreviate(e.getMessage()));
            } catch (Exception markEx) {
                log.error("标记任务失败状态时出错: {}", markEx.getMessage(), markEx);
            }
            throw e;
        }
    }

    /**
     * 将报告标记为 FAILED 并同步 Redis 进度。
     */
    private void markFailed(String reportId, String taskId, String message) {
        TaskReports failed = new TaskReports();
        failed.setId(reportId);
        failed.setTaskId(taskId);
        failed.setExecStatus(SimilarityExecStatus.FAILED.getValue());
        failed.setExecMessage(abbreviate(StrUtil.blankToDefault(message, "Unknown error")));
        taskReportsMapper.updateById(failed);
        progressService.failProgress(taskId, failed.getExecMessage());
        log.warn("任务 {} 标记为 FAILED: {}", taskId, failed.getExecMessage());
    }

    private static String abbreviate(String message) {
        if (message == null) {
            return null;
        }
        return message.length() <= 500 ? message : message.substring(0, 500);
    }
}
