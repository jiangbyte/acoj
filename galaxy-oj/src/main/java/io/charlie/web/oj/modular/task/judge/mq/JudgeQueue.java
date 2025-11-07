package io.charlie.web.oj.modular.task.judge.mq;

import lombok.RequiredArgsConstructor;
import org.springframework.amqp.core.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author ZhangJiangHu
 * @version v1.0
 * @date 20/09/2025
 * @description 判题队列配置
 */
@Configuration
@RequiredArgsConstructor
public class JudgeQueue {
    private final JudgeQueueProperties judgeQueueProperties;

    // 判题交换机
    @Bean
    public DirectExchange judgeMessageExchange() {
        return new DirectExchange(judgeQueueProperties.getCommon().getExchange(), true, false);
    }

    // 判题队列（配置死信队列）
    @Bean
    public Queue judgeMessageQueue() {
        return QueueBuilder.durable(judgeQueueProperties.getCommon().getQueue())
//                .deadLetterExchange(DEAD_LETTER_EXCHANGE) // 死信交换机
//                .deadLetterRoutingKey(DEAD_LETTER_ROUTING_KEY) // 死信路由键
                .build();
    }

    // 判题队列绑定
    @Bean
    public Binding judgeMessageBinding() {
        return BindingBuilder.bind(judgeMessageQueue())
                .to(judgeMessageExchange())
                .with(judgeQueueProperties.getCommon().getRoutingKey());
    }
}
