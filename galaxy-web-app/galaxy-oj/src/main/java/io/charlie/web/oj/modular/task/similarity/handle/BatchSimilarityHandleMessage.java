package io.charlie.web.oj.modular.task.similarity.handle;

import cn.hutool.json.JSONUtil;
import io.charlie.web.oj.modular.data.library.entity.DataLibrary;
import io.charlie.web.oj.modular.data.library.service.DataLibraryService;
import io.charlie.web.oj.modular.data.problem.mapper.DataProblemMapper;
import io.charlie.web.oj.modular.data.reports.mapper.TaskReportsMapper;
import io.charlie.web.oj.modular.data.similarity.mapper.TaskSimilarityMapper;
import io.charlie.web.oj.modular.data.solved.mapper.DataSolvedMapper;
import io.charlie.web.oj.modular.data.submit.entity.DataSubmit;
import io.charlie.web.oj.modular.data.submit.mapper.DataSubmitMapper;
import io.charlie.web.oj.modular.sys.config.mapper.SysConfigMapper;
import io.charlie.web.oj.modular.task.similarity.data.BatchSimilarityResult;
import io.charlie.web.oj.modular.task.similarity.data.Config;
import io.charlie.web.oj.modular.task.similarity.data.SimilarityResult;
import io.charlie.web.oj.modular.task.similarity.dto.BatchSimilaritySubmitDto;
import io.charlie.web.oj.modular.task.similarity.dto.SimilaritySubmitDto;
import io.charlie.web.oj.modular.task.similarity.mq.CommonSimilarityQueue;
import io.charlie.web.oj.modular.task.similarity.utils.CodeSimilarityCalculator;
import io.charlie.web.oj.modular.websocket.config.WebSocketConfig;
import io.charlie.web.oj.modular.websocket.data.WebSocketMessage;
import io.charlie.web.oj.modular.websocket.utils.WebSocketUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.dromara.trans.service.impl.TransService;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

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
    private final WebSocketUtil webSocketUtil;
    private final DataSubmitMapper dataSubmitMapper;
    private final DataSolvedMapper dataSolvedMapper;
    private final DataProblemMapper dataProblemMapper;
    private final DataLibraryService dataLibraryService;
    private final SysConfigMapper sysConfigMapper;
    private final TaskSimilarityMapper taskSimilarityMapper;
    private final TaskReportsMapper taskReportsMapper;
    private final CodeSimilarityCalculator codeSimilarityCalculator;
    private final TransService transService;

    public void sendSimilarity(BatchSimilaritySubmitDto batchSimilaritySubmitDto) {
        WebSocketMessage<String> message = new WebSocketMessage<>();
        message.setData("加载中");
        webSocketUtil.sendToTopic(WebSocketConfig.TOPIC_SIMILARITY_BATCH_STATUS, batchSimilaritySubmitDto.getBatchTaskId(), message);

        rabbitTemplate.convertAndSend(
                CommonSimilarityQueue.EXCHANGE1,
                CommonSimilarityQueue.ROUTING_KEY1,
                batchSimilaritySubmitDto
        );
    }


    @Transactional
    @RabbitListener(queues = CommonSimilarityQueue.QUEUE, concurrency = "5-10")
    public void receiveSimilarity(BatchSimilaritySubmitDto dto) {
        log.info("代码克隆检测 -> 接收检测消息：{}", JSONUtil.toJsonStr(dto));

        Config config = dto.getConfig();
        if (Config.shouldSkip(config)) {
            skipDetection(dto);
            return;
        }

        int batchSize = 1000; // 批次1000
        AtomicReference<String> reportId = new AtomicReference<>("");
        AtomicReference<BatchSimilarityResult> result = new AtomicReference<>();
        dataLibraryService.processCodeLibrariesInBatches(
                dto.getIsSet(),
                dto.getLanguage(),
                dto.getProblemIds(),
                List.of(dto.getSetId()),
                dto.getUserIds(),
                batchSize,
                libraries -> {
                    if (libraries.isEmpty()) {
                        skipDetection(dto);
                        return;
                    }
                    // 分批处理，避免大数据内存溢出（虽然达不到大数据）
                    result.set(calculateSimilarities(dto, libraries, config.getMinMatchLength()));
                    reportId.set(saveResults(dto, result.get()));
                }
        );

        // 更新信息
        updateSubmitAndNotify(dto, result.get(), reportId.get());
    }

    private void skipDetection(BatchSimilaritySubmitDto dto) {
        log.info("代码克隆检测 -> 忽略空数据");
//        updateSubmitSimilarity(dto.getId(), BigDecimal.ZERO, CloneLevelEnum.NOT_DETECTED.getValue(), null);
    }

    private BatchSimilarityResult calculateSimilarities(BatchSimilaritySubmitDto dto, List<DataLibrary> samples, int minMatchLength) {
        return null;
    }

    private String saveResults(BatchSimilaritySubmitDto dto, BatchSimilarityResult result) {
        taskSimilarityMapper.insert(result.getDetails());

//        TaskReports report = buildTaskReport(dto, result, problem);
//        taskReportsMapper.insert(report);
//        return report.getId();
        return "";
    }

    private void updateSubmitAndNotify(BatchSimilaritySubmitDto dto, BatchSimilarityResult result, String reportId) {
        // TODO: 待完善

        WebSocketMessage<DataSubmit> message = new WebSocketMessage<>();
        webSocketUtil.sendToTopic(WebSocketConfig.TOPIC_SIMILARITY_BATCH_STATUS, dto.getBatchTaskId(), message);
        webSocketUtil.sendToTopicClose(WebSocketConfig.TOPIC_SIMILARITY_BATCH_STATUS, dto.getBatchTaskId());
    }

}
