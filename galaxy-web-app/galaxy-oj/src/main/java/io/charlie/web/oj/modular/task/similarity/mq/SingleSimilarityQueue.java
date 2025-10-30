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
public class SingleSimilarityQueue {
    private final SimilarlyQueueProperties similarlyQueueProperties;

    @Bean
    public DirectExchange singleSimilarityMessageExchange() {
        return new DirectExchange(similarlyQueueProperties.getSingle().getExchange(), true, false);
    }

    @Bean
    public Queue singleSimilarityMessageQueue() {
        return QueueBuilder.durable(similarlyQueueProperties.getSingle().getQueue())
//                .deadLetterExchange(DEAD_LETTER_EXCHANGE) // 死信交换机
//                .deadLetterRoutingKey(DEAD_LETTER_ROUTING_KEY) // 死信路由键
                .build();
    }

    @Bean
    public Binding singleSimilarityMessageBinding() {
        return BindingBuilder.bind(singleSimilarityMessageQueue())
                .to(singleSimilarityMessageExchange())
                .with(similarlyQueueProperties.getSingle().getRoutingKey());
    }
}
