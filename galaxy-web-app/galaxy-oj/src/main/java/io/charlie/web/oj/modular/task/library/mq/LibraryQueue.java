package io.charlie.web.oj.modular.task.library.mq;

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
public class LibraryQueue {
    private final LibraryQueueProperties libraryQueueProperties;

    @Bean
    public DirectExchange libraryMessageExchange() {
        return new DirectExchange(libraryQueueProperties.getCommon().getExchange(), true, false);
    }

    @Bean
    public Queue libraryMessageQueue() {
        return QueueBuilder.durable(libraryQueueProperties.getCommon().getQueue())
//                .deadLetterExchange(DEAD_LETTER_EXCHANGE) // 死信交换机
//                .deadLetterRoutingKey(DEAD_LETTER_ROUTING_KEY) // 死信路由键
                .build();
    }

    @Bean
    public Binding libraryMessageBinding() {
        return BindingBuilder.bind(libraryMessageQueue())
                .to(libraryMessageExchange())
                .with(libraryQueueProperties.getCommon().getRoutingKey());
    }

}
