package io.charlie.web.oj.modular.task.similarity.handle;

import cn.hutool.json.JSONUtil;
import io.charlie.web.oj.modular.data.library.entity.DataLibrary;
import io.charlie.web.oj.modular.data.library.service.DataLibraryService;
import io.charlie.web.oj.modular.data.problem.mapper.DataProblemMapper;
import io.charlie.web.oj.modular.data.reports.mapper.TaskReportsMapper;
import io.charlie.web.oj.modular.data.similarity.entity.TaskSimilarity;
import io.charlie.web.oj.modular.data.similarity.mapper.TaskSimilarityMapper;
import io.charlie.web.oj.modular.data.solved.mapper.DataSolvedMapper;
import io.charlie.web.oj.modular.data.submit.entity.DataSubmit;
import io.charlie.web.oj.modular.data.submit.mapper.DataSubmitMapper;
import io.charlie.web.oj.modular.sys.config.mapper.SysConfigMapper;
import io.charlie.web.oj.modular.task.similarity.data.Config;
import io.charlie.web.oj.modular.task.similarity.data.SimilarityResult;
import io.charlie.web.oj.modular.task.similarity.dto.BatchSimilaritySubmitDto;
import io.charlie.web.oj.modular.task.similarity.mq.CommonSimilarityQueue;
import io.charlie.web.oj.modular.task.similarity.utils.CodeSimilarityCalculator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.dromara.trans.service.impl.TransService;
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
 * @description 相似度处理
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class BatchSimilarityHandleMessage {
    private final RabbitTemplate rabbitTemplate;
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
        // 发送消息
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

    }

    private void notify(BatchSimilaritySubmitDto dto, SimilarityResult result, String reportId) {
        // TODO: 待完善

    }

}
