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
public class CommonSimilarityQueue {
    // 判题消息队列
    public static final String EXCHANGE = "common.similarity.exchange"; // 判题交换机等
    public static final String BATCH_EXCHANGE = "common.similarity.batch.exchange"; // 判题交换机等
    public static final String QUEUE = "common.similarity.queue"; // 判题队列
    public static final String BATCH_QUEUE = "common.similarity.batch.queue"; // 判题队列
    public static final String ROUTING_KEY = "common.similarity.routing"; // 判题路由键
    public static final String BATCH_ROUTING_KEY = "common.similarity.batch.routing"; // 判题路由键

    // 判题交换机
    @Bean
    public DirectExchange commonSimilarityExchange() {
        return new DirectExchange(EXCHANGE, true, false);
    }

    // 判题队列（配置死信队列）
    @Bean
    public Queue commonSimilarityQueueQ() {
        return QueueBuilder.durable(QUEUE)
//                .deadLetterExchange(DEAD_LETTER_EXCHANGE) // 死信交换机
//                .deadLetterRoutingKey(DEAD_LETTER_ROUTING_KEY) // 死信路由键
                .build();
    }

    // 判题队列绑定
    @Bean
    public Binding commonSimilarityBinding() {
        return BindingBuilder.bind(commonSimilarityQueueQ())
                .to(commonSimilarityExchange())
                .with(ROUTING_KEY);
    }

    // 判题交换机
    @Bean
    public DirectExchange commonSimilarityExchange1() {
        return new DirectExchange(BATCH_EXCHANGE, true, false);
    }

    // 判题队列（配置死信队列）
    @Bean
    public Queue commonSimilarityQueueQ1() {
        return QueueBuilder.durable(BATCH_QUEUE)
//                .deadLetterExchange(DEAD_LETTER_EXCHANGE) // 死信交换机
//                .deadLetterRoutingKey(DEAD_LETTER_ROUTING_KEY) // 死信路由键
                .build();
    }

    // 判题队列绑定
    @Bean
    public Binding commonSimilarityBinding1() {
        return BindingBuilder.bind(commonSimilarityQueueQ1())
                .to(commonSimilarityExchange1())
                .with(BATCH_ROUTING_KEY);
    }
}
