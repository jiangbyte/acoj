package io.charlie.web.oj.modular.task.judge.handle;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import io.charlie.galaxy.utils.ranking.RankingUtil;
import io.charlie.web.oj.modular.data.library.service.DataLibraryService;
import io.charlie.web.oj.modular.data.ranking.enums.RankingEnums;
import io.charlie.web.oj.modular.data.ranking.service.UserRankingService;
import io.charlie.web.oj.modular.data.solved.entity.DataSolved;
import io.charlie.web.oj.modular.data.solved.mapper.DataSolvedMapper;
import io.charlie.web.oj.modular.data.submit.entity.DataSubmit;
import io.charlie.web.oj.modular.data.submit.mapper.DataSubmitMapper;
import io.charlie.web.oj.modular.task.judge.dto.JudgeResultDto;
import io.charlie.web.oj.modular.task.judge.dto.JudgeSubmitDto;
import io.charlie.web.oj.modular.task.judge.enums.JudgeStatus;
import io.charlie.web.oj.modular.task.judge.mq.CommonJudgeQueue;
import io.charlie.web.oj.modular.task.similarity.dto.SimilaritySubmitDto;
import io.charlie.web.oj.modular.task.similarity.handle.SimilarityHandleMessage;
import io.charlie.web.oj.modular.websocket.config.WebSocketConfig;
import io.charlie.web.oj.modular.websocket.data.WebSocketMessage;
import io.charlie.web.oj.modular.websocket.utils.WebSocketUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * @author ZhangJiangHu
 * @version v1.0
 * @date 20/09/2025
 * @description TODO
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JudgeHandleMessage {
    private final RabbitTemplate rabbitTemplate;
    private final WebSocketUtil webSocketUtil;
    private final DataSubmitMapper dataSubmitMapper;
    private final DataSolvedMapper dataSolvedMapper;
    private final RankingUtil rankingUtil;
    private final DataLibraryService dataLibraryService;
    private final SimilarityHandleMessage similarityHandleMessage;
    private final UserRankingService userRankingService;

    public void sendJudge(JudgeSubmitDto judgeSubmitDto) {
        log.info("发送消息：{}", JSONUtil.toJsonStr(judgeSubmitDto));

        WebSocketMessage<Map<String, String>> message = new WebSocketMessage<>();
        Map<String, String> data = new HashMap<>();
        data.put("id", judgeSubmitDto.getId());
        data.put("status", JudgeStatus.PENDING.getValue());
        message.setData(data);
        webSocketUtil.sendToTopic(WebSocketConfig.TOPIC_JUDGE_STATUS, judgeSubmitDto.getJudgeTaskId(), message);

        rabbitTemplate.convertAndSend(
                CommonJudgeQueue.EXCHANGE,
                CommonJudgeQueue.ROUTING_KEY,
                judgeSubmitDto
        );
        log.info("发送消息成功");
    }

    @Transactional
    @RabbitListener(queues = CommonJudgeQueue.BACK_QUEUE, concurrency = "5-10")
    public void receiveJudge(JudgeResultDto judgeResultDto) {
        log.info("接收到消息：{}", JSONUtil.toJsonStr(judgeResultDto));

        DataSubmit bean = BeanUtil.copyProperties(judgeResultDto, DataSubmit.class);
        bean.setUpdateTime(new Date());
        bean.setUpdateUser(bean.getUserId());
        dataSubmitMapper.updateById(bean);

        // 正式提交
        if (judgeResultDto.getSubmitType()) {
            // 通过
            if (bean.getStatus().equals(JudgeStatus.ACCEPTED.getValue())) {
                // 排行榜更新
                userRankingService.processSubmission(bean);

                // 更新 solved
                dataSolvedMapper.update(new LambdaUpdateWrapper<DataSolved>()
                        .eq(DataSolved::getUserId, bean.getUserId())
                        .eq(DataSolved::getProblemId, bean.getProblemId())
                        .eq(DataSolved::getSubmitId, bean.getId())
                        .set(DataSolved::getSolved, true)
                );

                // 添加到 library
                dataLibraryService.addLibrary(dataSubmitMapper.selectById(bean.getId()));

                // 本次提交相似报告
                String taskId = IdUtil.getSnowflakeNextIdStr();
                dataSubmitMapper.update(new LambdaUpdateWrapper<DataSubmit>()
                        .eq(DataSubmit::getId, bean.getId())
                        .set(DataSubmit::getTaskId, taskId)
                );

                SimilaritySubmitDto similaritySubmitDto = BeanUtil.toBean(judgeResultDto, SimilaritySubmitDto.class);
                similaritySubmitDto.setTaskId(taskId);
                similaritySubmitDto.setTaskType(false);
                similaritySubmitDto.setCreateTime(bean.getCreateTime());
                similarityHandleMessage.sendSimilarity(similaritySubmitDto);
            }
        }

        WebSocketMessage<JudgeResultDto> message = new WebSocketMessage<>();
        message.setData(judgeResultDto);
        webSocketUtil.sendToTopic(WebSocketConfig.TOPIC_JUDGE_STATUS,  judgeResultDto.getJudgeTaskId(), message);
        webSocketUtil.sendToTopicClose(WebSocketConfig.TOPIC_JUDGE_STATUS,  judgeResultDto.getJudgeTaskId());
        log.info("向客户端 发送关闭指令");
    }

}
