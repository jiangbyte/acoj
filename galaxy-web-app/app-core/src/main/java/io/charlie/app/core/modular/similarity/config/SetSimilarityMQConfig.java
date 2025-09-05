package io.charlie.app.core.modular.similarity.config;

import lombok.RequiredArgsConstructor;
import org.springframework.amqp.core.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author Charlie Zhang
 * @version v1.0
 * @date 23/07/2025
 * @description 题目克隆检测消息队列配置
 */
@Configuration
@RequiredArgsConstructor
public class SetSimilarityMQConfig {
    // 判题消息队列
    public static final String EXCHANGE = "set.similarity.exchange"; // 判题交换机
    public static final String QUEUE = "set.similarity.queue"; // 判题队列
    public static final String ROUTING_KEY = "set.similarity.routing.key"; // 判题路由键
    // 死信
    public static final String DEAD_LETTER_EXCHANGE = "set.similarity.dead.exchange"; // 死信交换机
    public static final String DEAD_LETTER_QUEUE = "set.similarity.dead.queue"; // 死信队列
    public static final String DEAD_LETTER_ROUTING_KEY = "set.similarity.dead.routing.key"; // 死信路由键

    // 判题交换机
    @Bean
    public DirectExchange setSimilarityExchange() {
        return new DirectExchange(EXCHANGE, true, false);
    }

    // 判题队列（配置死信队列）
    @Bean
    public Queue setSimilarityQueue() {
        return QueueBuilder.durable(QUEUE)
//                .deadLetterExchange(DEAD_LETTER_EXCHANGE) // 死信交换机
//                .deadLetterRoutingKey(DEAD_LETTER_ROUTING_KEY) // 死信路由键
                .build();
    }

    // 判题队列绑定
    @Bean
    public Binding problemSimilarityBinding() {
        return BindingBuilder.bind(setSimilarityQueue())
                .to(setSimilarityExchange())
                .with(ROUTING_KEY);
    }

    // 死信交换机
//    @Bean
//    public DirectExchange problemJudgeDeadLetterExchange() {
//        return new DirectExchange(DEAD_LETTER_EXCHANGE, true, false);
//    }
//
//    // 死信队列
//    @Bean
//    public Queue problemJudgeDeadLetterQueue() {
//        return new Queue(DEAD_LETTER_QUEUE, true);
//    }
//
//    // 死信队列绑定
//    @Bean
//    public Binding problemJudgeDeadLetterBinding() {
//        return BindingBuilder.bind(problemJudgeDeadLetterQueue())
//                .to(problemJudgeDeadLetterExchange())
//                .with(DEAD_LETTER_ROUTING_KEY);
//    }
}
