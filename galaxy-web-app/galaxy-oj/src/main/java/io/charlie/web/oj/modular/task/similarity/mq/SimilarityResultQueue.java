package io.charlie.web.oj.modular.task.similarity.mq;

import lombok.RequiredArgsConstructor;
import org.springframework.amqp.core.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author ZhangJiangHu
 * @version v1.0
 * @date 20/09/2025
 * @description 相似度计算队列配置
 */
@Configuration
@RequiredArgsConstructor
public class SimilarityResultQueue {
    private final SimilarlyQueueProperties similarlyQueueProperties;

    @Bean
    public DirectExchange similarityResultMessageExchange() {
        return new DirectExchange(similarlyQueueProperties.getResult().getExchange(), true, false);
    }

    @Bean
    public Queue similarityResultMessageQueue() {
        return QueueBuilder.durable(similarlyQueueProperties.getResult().getQueue())
//                .deadLetterExchange(DEAD_LETTER_EXCHANGE) // 死信交换机
//                .deadLetterRoutingKey(DEAD_LETTER_ROUTING_KEY) // 死信路由键
                .build();
    }

    @Bean
    public Binding similarityMessageBinding() {
        return BindingBuilder.bind(similarityResultMessageQueue())
                .to(similarityResultMessageExchange())
                .with(similarlyQueueProperties.getResult().getRoutingKey());
    }
}
