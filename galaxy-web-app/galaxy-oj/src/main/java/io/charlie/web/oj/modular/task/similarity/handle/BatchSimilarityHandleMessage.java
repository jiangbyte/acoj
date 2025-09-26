package io.charlie.web.oj.modular.task.similarity.handle;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
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
import io.charlie.web.oj.modular.task.similarity.data.Config;
import io.charlie.web.oj.modular.task.similarity.dto.BatchSimilaritySubmitDto;
import io.charlie.web.oj.modular.task.similarity.dto.SimilaritySubmitDto;
import io.charlie.web.oj.modular.task.similarity.enums.CloneLevelEnum;
import io.charlie.web.oj.modular.task.similarity.enums.ReportTypeEnum;
import io.charlie.web.oj.modular.task.similarity.mq.CommonSimilarityQueue;
import io.charlie.web.oj.modular.task.similarity.utils.CodeSimilarityCalculator;
import io.charlie.web.oj.modular.task.similarity.utils.DynamicCloneLevelDetector;
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
    private final RankingUtil rankingUtil;
    private final DataLibraryMapper dataLibraryMapper;
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

        if (Config.shouldSkip(dto.getConfig())) {
            skipDetection(dto);
            return;
        }

        WebSocketMessage<String> message = new WebSocketMessage<>();
        message.setData("完成");
        webSocketUtil.sendToTopic(WebSocketConfig.TOPIC_SIMILARITY_BATCH_STATUS, dto.getBatchTaskId(), message);
        webSocketUtil.sendToTopicClose(WebSocketConfig.TOPIC_SIMILARITY_BATCH_STATUS, dto.getBatchTaskId());
    }

    private void skipDetection(BatchSimilaritySubmitDto dto) {
        log.info("代码克隆检测 -> 忽略空数据");
//        updateSubmitSimilarity(dto.getId(), BigDecimal.ZERO, CloneLevelEnum.NOT_DETECTED.getValue(), null);
    }

}
