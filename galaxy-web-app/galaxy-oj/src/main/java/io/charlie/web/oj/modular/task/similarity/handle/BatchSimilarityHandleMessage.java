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
import cn.hutool.core.util.IdUtil;
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
import io.charlie.web.oj.modular.data.similarity.dto.CloneLevel;
import io.charlie.web.oj.modular.data.similarity.dto.TaskReportStats;
import io.charlie.web.oj.modular.data.similarity.entity.TaskSimilarity;
import io.charlie.web.oj.modular.data.similarity.mapper.TaskSimilarityMapper;
import io.charlie.web.oj.modular.data.submit.entity.DataSubmit;
import io.charlie.web.oj.modular.data.submit.mapper.DataSubmitMapper;
import io.charlie.web.oj.modular.task.similarity.dto.BatchSimilaritySubmitDto;
import io.charlie.web.oj.modular.task.similarity.dto.SimilaritySubmitDto;
import io.charlie.web.oj.modular.task.similarity.enums.CloneLevelEnum;
import io.charlie.web.oj.modular.task.similarity.enums.ReportTypeEnum;
import io.charlie.web.oj.modular.task.similarity.mq.CommonSimilarityQueue;
import io.charlie.web.oj.modular.task.similarity.service.SimilarityProgressService;
import io.charlie.web.oj.utils.similarity.utils.CodeSimilarityCalculator;
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
    private final DataLibraryMapper dataLibraryMapper;
    private final TaskSimilarityMapper taskSimilarityMapper;
    private final CodeSimilarityCalculator codeSimilarityCalculator;
    private final ThreadPoolTaskExecutor similarityTaskExecutor;
    private final TaskReportsMapper taskReportsMapper;

    private final SimilarityProgressService progressService;

    public void sendSimilarity(BatchSimilaritySubmitDto batchSimilaritySubmitDto) {
        log.info("代码克隆检测 -> 发送批量检测消息：{}", JSONUtil.toJsonStr(batchSimilaritySubmitDto));
        rabbitTemplate.convertAndSend(
                CommonSimilarityQueue.BATCH_EXCHANGE,
                CommonSimilarityQueue.BATCH_ROUTING_KEY,
                batchSimilaritySubmitDto
        );
    }

    @Transactional
    @RabbitListener(queues = CommonSimilarityQueue.BATCH_QUEUE, concurrency = "5-10")
    public void receiveSimilarity(BatchSimilaritySubmitDto batchSimilaritySubmitDto) {
        log.info("代码克隆检测 -> 接收批量检测消息：{}", JSONUtil.toJsonStr(batchSimilaritySubmitDto));

        StopWatch stopWatch = new StopWatch();
        stopWatch.start();

        String taskId = batchSimilaritySubmitDto.getTaskId();

        try {

            List<DataLibrary> dataLibraries = queryDataLibraries(batchSimilaritySubmitDto);
            log.info("代码克隆检测 -> 样本数量：{}", dataLibraries.size());

            // ========================================== 初始化进度 ==========================================
            progressService.initProgress(taskId, dataLibraries.size());
            progressService.updateProgress(taskId, 0, "开始相似度计算");

            if (dataLibraries.isEmpty()) {
                log.warn("代码克隆检测 -> 未找到符合条件的样本数据");
                progressService.completeProgress(taskId);
                return;
            }

//            List<TaskSimilarity> similarityResults = processSimilarityDetection(dataLibraries, batchSimilaritySubmitDto);


            // 分批处理并更新进度
            List<TaskSimilarity> similarityResults = new ArrayList<>();
            int batchSize = 100; // 每批处理数量
            int processed = 0;

            for (int i = 0; i < dataLibraries.size(); i += batchSize) {
                int end = Math.min(i + batchSize, dataLibraries.size());
                List<DataLibrary> batch = dataLibraries.subList(i, end);

                List<TaskSimilarity> batchResults = processBatchSimilarity(
                        batch, batchSimilaritySubmitDto);
                similarityResults.addAll(batchResults);

                processed = end;
                progressService.updateProgress(taskId, processed,
                        String.format("正在处理第 %d-%d 条数据", i + 1, end));
            }


            if (!similarityResults.isEmpty()) {
                progressService.updateProgress(taskId, processed, "保存检测结果");
                batchInsertSimilarityResults(similarityResults);
                log.info("代码克隆检测 -> 成功插入 {} 条相似度检测结果", similarityResults.size());
            }

            progressService.updateProgress(taskId, processed, "生成检测报告");
            saveSimilarityReportDataFromDB(batchSimilaritySubmitDto);
            progressService.completeProgress(taskId);
        } catch (Exception e) {
            log.error("代码克隆检测处理失败，消息体：{}", JSONUtil.toJsonStr(batchSimilaritySubmitDto), e);
            progressService.failProgress(taskId, e.getMessage());
            throw e;
        } finally {
            stopWatch.stop();
            log.info("代码克隆检测 -> 处理完成，耗时：{} ms", stopWatch.getTotalTimeMillis());
        }
    }

    private List<TaskSimilarity> processBatchSimilarity(List<DataLibrary> batch,
                                                        BatchSimilaritySubmitDto dto) {
        // 实际的相似度计算逻辑
        return processSimilarityDetection(batch, dto);
    }

    /**
     * 查询数据样本库
     */
    private List<DataLibrary> queryDataLibraries(BatchSimilaritySubmitDto dto) {
        LambdaQueryWrapper<DataLibrary> queryWrapper = new LambdaQueryWrapper<DataLibrary>()
                .eq(DataLibrary::getIsSet, Boolean.TRUE)
                .eq(DataLibrary::getLanguage, dto.getLanguage())
                .orderByDesc(DataLibrary::getUpdateTime)
                .orderByDesc(DataLibrary::getCreateTime)
                .last("LIMIT 1000");

//        if (dto.getIsSet()) {
        queryWrapper.eq(DataLibrary::getSetId, dto.getSetId())
                .in(DataLibrary::getProblemId, dto.getProblemIds());
//        } else {
//            queryWrapper.eq(DataLibrary::getProblemId, dto.getProblemIds().getFirst());
//        }

        return dataLibraryMapper.selectList(queryWrapper);
    }

    /**
     * 处理相似度检测 - 支持多线程
     */
    private List<TaskSimilarity> processSimilarityDetection(List<DataLibrary> dataLibraries,
                                                            BatchSimilaritySubmitDto dto) {
        // 如果是题集模式，按题目分组并行处理
//        if (dto.getIsSet()) {
        return processGroupedSimilarityDetection(dataLibraries, dto);
//        } else {
//            return processSingleProblemSimilarityDetection(dataLibraries, dto);
//        }
    }

    /**
     * 处理分组相似度检测（题集模式）
     */
    private List<TaskSimilarity> processGroupedSimilarityDetection(List<DataLibrary> dataLibraries,
                                                                   BatchSimilaritySubmitDto dto) {
        Map<String, List<DataLibrary>> groupByProblemIdMap = dataLibraries.stream()
                .collect(Collectors.groupingBy(DataLibrary::getProblemId));

        List<CompletableFuture<List<TaskSimilarity>>> futures = groupByProblemIdMap.values()
                .stream()
                .map(group -> CompletableFuture.supplyAsync(() ->
                        detectSimilarityForGroup(group, dto), similarityTaskExecutor))
                .toList();

        // 等待所有任务完成
        return futures.stream()
                .map(CompletableFuture::join)
                .flatMap(List::stream)
                .collect(Collectors.toList());
    }

    /**
     * 处理单个题目相似度检测
     */
    private List<TaskSimilarity> processSingleProblemSimilarityDetection(List<DataLibrary> dataLibraries,
                                                                         BatchSimilaritySubmitDto dto) {
        return detectSimilarityForGroup(dataLibraries, dto);
    }

    /**
     * 为单个分组检测相似度
     */
    private List<TaskSimilarity> detectSimilarityForGroup(List<DataLibrary> libraries,
                                                          BatchSimilaritySubmitDto dto) {
        List<TaskSimilarity> groupResults = new ArrayList<>();

        for (int i = 0; i < libraries.size(); i++) {
            DataLibrary submit = libraries.get(i);

            // 使用并行流处理内部循环
            List<TaskSimilarity> pairResults = libraries.parallelStream()
                    .filter(library -> !submit.getId().equals(library.getId()))
                    .map(library -> calculateSimilarity(submit, library, dto))
                    .filter(Objects::nonNull)
                    .toList();

            groupResults.addAll(pairResults);
        }

        return groupResults;
    }

    /**
     * 计算相似度 - 这是你需要实现的核心方法
     */
    private TaskSimilarity calculateSimilarity(DataLibrary submit, DataLibrary library, BatchSimilaritySubmitDto batchSimilaritySubmitDto) {
        try {
            CodeSimilarityCalculator.SimilarityDetail similarityDetail = codeSimilarityCalculator.getSimilarityDetail(
                    submit.getLanguage().toLowerCase(),
                    submit.getCode(),
                    library.getCode(),
                    batchSimilaritySubmitDto.getMinMatchLength()
            );

            TaskSimilarity taskSimilarity = new TaskSimilarity();
            // 基础信息
            taskSimilarity.setTaskId(batchSimilaritySubmitDto.getTaskId());
            taskSimilarity.setTaskType(batchSimilaritySubmitDto.getTaskType());
            taskSimilarity.setProblemId(submit.getProblemId());
            taskSimilarity.setSetId(batchSimilaritySubmitDto.getSetId());
//            taskSimilarity.setIsSet(batchSimilaritySubmitDto.getIsSet());
            taskSimilarity.setIsSet(Boolean.TRUE);
            taskSimilarity.setLanguage(batchSimilaritySubmitDto.getLanguage());
            taskSimilarity.setSimilarity(BigDecimal.valueOf(similarityDetail.getSimilarity()));

            // 提交信息
            taskSimilarity.setSubmitUser(submit.getUserId());
            taskSimilarity.setSubmitCode(submit.getCode());
            taskSimilarity.setSubmitCodeLength(submit.getCode().length());
            taskSimilarity.setSubmitId(submit.getSubmitId());
            taskSimilarity.setSubmitTime(submit.getCreateTime());
            taskSimilarity.setSubmitTokenName(similarityDetail.getSubmitTokenNames());
            taskSimilarity.setSubmitTokenTexts(similarityDetail.getSubmitTokenTexts());

            // 样本信息
            taskSimilarity.setOriginUser(library.getUserId());
            taskSimilarity.setOriginCode(library.getCode());
            taskSimilarity.setOriginCodeLength(library.getCodeLength());
            taskSimilarity.setOriginId(library.getSubmitId());
            taskSimilarity.setOriginTime(library.getCreateTime());
            taskSimilarity.setOriginTokenName(similarityDetail.getLibraryTokenNames());
            taskSimilarity.setOriginTokenTexts(similarityDetail.getLibraryTokenTexts());

            return taskSimilarity;

        } catch (Exception e) {
            log.error("计算相似度失败，submitId: {}, libraryId: {}", submit.getId(), library.getId(), e);
            return null;
        }
    }

    /**
     * 批量插入相似度结果
     */
    private void batchInsertSimilarityResults(List<TaskSimilarity> results) {
        // 分批插入，避免单次插入数据量过大
        int batchSize = 400;
        for (int i = 0; i < results.size(); i += batchSize) {
            int end = Math.min(i + batchSize, results.size());
            List<TaskSimilarity> batchList = results.subList(i, end);
            for (TaskSimilarity result : batchList) {
                taskSimilarityMapper.insert(result);
            }
        }
    }


    void saveSimilarityReportDataFromDB(BatchSimilaritySubmitDto batchSimilaritySubmitDto) {
        TaskReports taskReports = BeanUtil.toBean(batchSimilaritySubmitDto, TaskReports.class);
        taskReports.setId(null);
//        if (batchSimilaritySubmitDto.getIsSet()) {
        log.info("代码克隆检测 -> 单题集报告");
        taskReports.setReportType(ReportTypeEnum.SET_SINGLE_SUBMIT.getValue());
//        } else {
//            log.info("代码克隆检测 -> 单题目报告");
//            taskReports.setReportType(ReportTypeEnum.PROBLEM_SINGLE_SUBMIT.getValue());
//        }

        taskReports.setThreshold(batchSimilaritySubmitDto.getThreshold());

        // 从数据库中查询
        TaskReportStats taskReportStats = taskSimilarityMapper.selectSimilarityStatsByTaskId(batchSimilaritySubmitDto.getTaskId());
        if (taskReportStats.getAvgSimilarity() == null || taskReportStats.getMaxSimilarity() == null || taskReportStats.getAvgSimilarity().equals(BigDecimal.ZERO) || taskReportStats.getMaxSimilarity().equals(BigDecimal.ZERO)) {
            log.info("代码克隆检测 -> 相似为空 忽略报告");
            return;
        }

        taskReports.setSampleCount(taskReportStats.getSampleCount());
        taskReports.setSimilarityGroupCount(taskReportStats.getGroupCount());
        taskReports.setAvgSimilarity(taskReportStats.getAvgSimilarity());
        taskReports.setMaxSimilarity(taskReportStats.getMaxSimilarity());

        // 获取相似度分布数组
        List<Integer> distributionArray = taskSimilarityMapper.selectSimilarityDistribution(batchSimilaritySubmitDto.getTaskId());
        taskReports.setSimilarityDistribution(distributionArray);

        // 获取程度统计
        List<CloneLevel> degreeStats = taskSimilarityMapper.selectDegreeStatistics(batchSimilaritySubmitDto.getTaskId(), batchSimilaritySubmitDto.getThreshold());
        taskReports.setDegreeStatistics(degreeStats);

        taskReportsMapper.insert(taskReports);

        log.info("任务 {} 最大相似度 {} 平均相似度 {}", batchSimilaritySubmitDto.getTaskId(), taskReportStats.getMaxSimilarity(), taskReportStats.getAvgSimilarity());
    }
}