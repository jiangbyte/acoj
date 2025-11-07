package io.charlie.web.oj.modular.task.solved.mq;

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
public class SolvedQueue {
    private final SolvedQueueProperties solvedQueueProperties;

    @Bean
    public DirectExchange solvedMessageExchange() {
        return new DirectExchange(solvedQueueProperties.getCommon().getExchange(), true, false);
    }

    @Bean
    public Queue solvedMessageQueue() {
        return QueueBuilder.durable(solvedQueueProperties.getCommon().getQueue())
//                .deadLetterExchange(DEAD_LETTER_EXCHANGE) // 死信交换机
//                .deadLetterRoutingKey(DEAD_LETTER_ROUTING_KEY) // 死信路由键
                .build();
    }

    @Bean
    public Binding solvedMessageBinding() {
        return BindingBuilder.bind(solvedMessageQueue())
                .to(solvedMessageExchange())
                .with(solvedQueueProperties.getCommon().getRoutingKey());
    }

}
