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
public class JudgeResultQueue {
    private final JudgeQueueProperties judgeQueueProperties;

    @Bean
    public DirectExchange judgeResultMessageExchange() {
        return new DirectExchange(judgeQueueProperties.getResult().getExchange(), true, false);
    }

    @Bean
    public Queue judgeResultMessageQueue() {
        return QueueBuilder.durable(judgeQueueProperties.getResult().getQueue())
//                .deadLetterExchange(DEAD_LETTER_EXCHANGE) // 死信交换机
//                .deadLetterRoutingKey(DEAD_LETTER_ROUTING_KEY) // 死信路由键
                .build();
    }

    @Bean
    public Binding judgeResultMessageBinding() {
        return BindingBuilder.bind(judgeResultMessageQueue())
                .to(judgeResultMessageExchange())
                .with(judgeQueueProperties.getResult().getRoutingKey());
    }
}
