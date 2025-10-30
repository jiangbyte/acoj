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
public class BatchSimilarityQueue {
    private final SimilarlyQueueProperties similarlyQueueProperties;

    @Bean
    public DirectExchange batchSimilarityMessageExchange() {
        return new DirectExchange(similarlyQueueProperties.getBatch().getExchange(), true, false);
    }

    @Bean
    public Queue batchSimilarityMessageQueue() {
        return QueueBuilder.durable(similarlyQueueProperties.getBatch().getQueue())
//                .deadLetterExchange(DEAD_LETTER_EXCHANGE) // 死信交换机
//                .deadLetterRoutingKey(DEAD_LETTER_ROUTING_KEY) // 死信路由键
                .build();
    }

    @Bean
    public Binding batchSimilarityMessageBinding() {
        return BindingBuilder.bind(batchSimilarityMessageQueue())
                .to(batchSimilarityMessageExchange())
                .with(similarlyQueueProperties.getBatch().getRoutingKey());
    }
}
