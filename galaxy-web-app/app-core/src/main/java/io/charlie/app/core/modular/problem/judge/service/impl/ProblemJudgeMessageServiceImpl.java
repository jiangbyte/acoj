package io.charlie.app.core.modular.problem.judge.service.impl;

import cn.hutool.json.JSONUtil;
import io.charlie.app.core.modular.problem.judge.config.ProblemJudgeMQConfig;
import io.charlie.app.core.modular.problem.judge.dto.ProJudgeSubmitDto;
import io.charlie.app.core.modular.problem.judge.dto.ProJudgeSubmitResultDto;
import io.charlie.app.core.modular.problem.judge.service.ProblemJudgeMessageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author ZhangJiangHu
 * @version v1.0
 * @date 06/08/2025
 * @description TODO
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ProblemJudgeMessageServiceImpl implements ProblemJudgeMessageService {
    private final RabbitTemplate rabbitTemplate;

    @Override
    public void sendJudgeRequest(ProJudgeSubmitDto proJudgeSubmitDto) {
        // 发送消息
//            amqpTemplate.convertAndSend(
//                    PReqMQConfig.EXCHANGE,
//                    PReqMQConfig.ROUTING_KEY,
//                    message,
//                    msg -> {
//                        msg.getMessageProperties().setDeliveryMode(MessageDeliveryMode.PERSISTENT);
//                        return msg;
//                    }
//            );
        rabbitTemplate.convertAndSend(
                ProblemJudgeMQConfig.EXCHANGE,
                ProblemJudgeMQConfig.ROUTING_KEY,
                proJudgeSubmitDto
        );
    }

    @Override
    public void handleJudgeResult(ProJudgeSubmitResultDto proJudgeSubmitResultDto) {

    }

    @Transactional
//    @RabbitListener(queues = ProblemJudgeMQConfig.QUEUE, concurrency = "5-10")
    public void handleMessage(ProJudgeSubmitDto message) {
        log.info("接收到消息：{}", JSONUtil.toJsonStr(message));
    }
}
