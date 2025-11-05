//package io.charlie.web.oj.modular.task.similarity.handle;
//
//import cn.hutool.json.JSONUtil;
//import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
//import io.charlie.web.oj.modular.data.library.entity.DataLibrary;
//import io.charlie.web.oj.modular.data.library.mapper.DataLibraryMapper;
//import io.charlie.web.oj.modular.data.library.service.DataLibraryService;
//import io.charlie.web.oj.modular.data.problem.mapper.DataProblemMapper;
//import io.charlie.web.oj.modular.data.reports.mapper.TaskReportsMapper;
//import io.charlie.web.oj.modular.data.similarity.entity.TaskSimilarity;
//import io.charlie.web.oj.modular.data.similarity.mapper.TaskSimilarityMapper;
//import io.charlie.web.oj.modular.data.solved.mapper.DataSolvedMapper;
//import io.charlie.web.oj.modular.data.submit.mapper.DataSubmitMapper;
//import io.charlie.web.oj.modular.sys.config.mapper.SysConfigMapper;
//import io.charlie.web.oj.modular.task.similarity.data.SimilarityResult;
//import io.charlie.web.oj.modular.task.similarity.dto.BatchSimilaritySubmitDto;
//import io.charlie.web.oj.modular.task.similarity.dto.SimilaritySubmitDto;
//import io.charlie.web.oj.modular.task.similarity.mq.CommonSimilarityQueue;
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


package io.charlie.web.oj.modular.task.similarity.handle;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import io.charlie.web.oj.modular.data.library.entity.DataLibrary;
import io.charlie.web.oj.modular.data.library.mapper.DataLibraryMapper;
import io.charlie.web.oj.modular.data.reports.entity.TaskReports;
import io.charlie.web.oj.modular.data.reports.mapper.TaskReportsMapper;
import io.charlie.web.oj.modular.data.similarity.dto.CloneLevel;
import io.charlie.web.oj.modular.data.similarity.dto.TaskReportStats;
import io.charlie.web.oj.modular.data.similarity.entity.TaskSimilarity;
import io.charlie.web.oj.modular.data.similarity.mapper.TaskSimilarityMapper;
import io.charlie.web.oj.modular.task.similarity.dto.BatchSimilaritySubmitDto;
import io.charlie.web.oj.modular.task.similarity.dto.BatchSimilaritySubmitResultDto;
import io.charlie.web.oj.modular.task.similarity.enums.ReportTypeEnum;
import io.charlie.web.oj.modular.task.similarity.mq.SimilarlyQueueProperties;
import io.charlie.web.oj.modular.task.similarity.service.SimilarityProgressService;
import io.charlie.web.oj.utils.similarity.utils.SimilarityCalculator;
import io.charlie.web.oj.utils.similarity.utils.SimilarityDetail;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StopWatch;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

/**
 * @author ZhangJiangHu
 * @version v1.0
 * @date 20/09/2025
 * @description 相似度处理
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class BatchSimilarityHandleMessage {
    private final RabbitTemplate rabbitTemplate;
    private final TaskSimilarityMapper taskSimilarityMapper;
    private final TaskReportsMapper taskReportsMapper;

    private final SimilarlyQueueProperties properties;

    public void sendSimilarity(BatchSimilaritySubmitDto batchSimilaritySubmitDto) {
        log.info("代码克隆检测 -> 发送检测消息：{}", JSONUtil.toJsonStr(batchSimilaritySubmitDto));
        rabbitTemplate.convertAndSend(
                properties.getCommon().getExchange(),
                properties.getCommon().getRoutingKey(),
                batchSimilaritySubmitDto
        );
    }

    @Transactional
    @RabbitListener(queues = "${oj.mq.similarity.result.queue}", concurrency = "1")
    public void receiveSimilarity(BatchSimilaritySubmitResultDto batchSimilaritySubmitDto) {
        log.info("收到相似检测任务消息，任务ID: {}, 报告ID: {}",
                batchSimilaritySubmitDto.getTaskId(),
                batchSimilaritySubmitDto.getReportId());

        try {
            // 1. 参数基础校验
            if (batchSimilaritySubmitDto == null) {
                log.error("接收到的消息为空");
                return;
            }

            if (batchSimilaritySubmitDto.getTaskId() == null || batchSimilaritySubmitDto.getReportId() == null) {
                log.error("任务ID或报告ID为空，任务ID: {}, 报告ID: {}",
                        batchSimilaritySubmitDto.getTaskId(),
                        batchSimilaritySubmitDto.getReportId());
                return;
            }

            UpdateWrapper<TaskReports> updateWrapper = new UpdateWrapper<TaskReports>().checkSqlInjection();

            // 2. 从数据库中查询统计信息
            TaskReportStats taskReportStats = taskSimilarityMapper.selectSimilarityStatsByTaskId(batchSimilaritySubmitDto.getTaskId());

            // 处理统计信息为空的情况
            if (taskReportStats == null) {
                log.warn("未找到任务 {} 的统计信息，使用默认值", batchSimilaritySubmitDto.getTaskId());
                taskReportStats = new TaskReportStats();
                taskReportStats.setSampleCount(0);
                taskReportStats.setGroupCount(0);
                taskReportStats.setAvgSimilarity(BigDecimal.ZERO);
                taskReportStats.setMaxSimilarity(BigDecimal.ZERO);
            }

            // 3. 获取相似度分布数组
            List<Integer> distributionArray = taskSimilarityMapper.selectSimilarityDistribution(batchSimilaritySubmitDto.getTaskId());
            if (distributionArray == null) {
                distributionArray = new ArrayList<>();
                log.info("任务 {} 的相似度分布为空，使用空列表", batchSimilaritySubmitDto.getTaskId());
            }

            // 4. 获取程度统计
            List<CloneLevel> degreeStats = taskSimilarityMapper.selectDegreeStatistics(
                    batchSimilaritySubmitDto.getTaskId(),
                    batchSimilaritySubmitDto.getThreshold()
            );
            if (degreeStats == null) {
                degreeStats = new ArrayList<>();
                log.info("任务 {} 的程度统计为空，使用空列表", batchSimilaritySubmitDto.getTaskId());
            }

            // 5. 构建更新条件
            TaskReports taskReports = new TaskReports();
            taskReports.setId(batchSimilaritySubmitDto.getReportId());
            taskReports.setTaskId(batchSimilaritySubmitDto.getTaskId());
            taskReports.setSampleCount(taskReportStats.getSampleCount());
            taskReports.setSimilarityGroupCount(taskReportStats.getGroupCount());
            taskReports.setAvgSimilarity(taskReportStats.getAvgSimilarity());
            taskReports.setMaxSimilarity(taskReportStats.getMaxSimilarity());
            taskReports.setSimilarityDistribution(distributionArray);
            taskReports.setDegreeStatistics(degreeStats);

            // 6. 执行更新并检查结果
            int updateCount = taskReportsMapper.updateById(taskReports);
            if (updateCount == 0) {
                log.warn("未更新任何记录，报告ID: {}, 任务ID: {}",
                        batchSimilaritySubmitDto.getReportId(),
                        batchSimilaritySubmitDto.getTaskId());
            } else {
                log.info("成功更新 {} 条记录", updateCount);
            }

            log.info("任务 {} 统计完成 - 样本数: {}, 相似组数: {}, 最大相似度: {}, 平均相似度: {}",
                    batchSimilaritySubmitDto.getTaskId(),
                    taskReportStats.getSampleCount(),
                    taskReportStats.getGroupCount(),
                    taskReportStats.getMaxSimilarity(),
                    taskReportStats.getAvgSimilarity());

        } catch (Exception e) {
            log.error("处理相似检测任务失败，任务ID: {}, 报告ID: {}, 错误信息: {}",
                    batchSimilaritySubmitDto != null ? batchSimilaritySubmitDto.getTaskId() : "null",
                    batchSimilaritySubmitDto != null ? batchSimilaritySubmitDto.getReportId() : "null",
                    e.getMessage(), e);
            // 异常重试
            throw e;
        }
    }
}