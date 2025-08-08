package io.charlie.app.core.modular.judge.config;

import lombok.RequiredArgsConstructor;
import org.springframework.amqp.core.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author Charlie Zhang
 * @version v1.0
 * @date 23/07/2025
 * @description 题目提交消息队列配置
 */
@Configuration
@RequiredArgsConstructor
public class ProblemResultMQConfig {
    // 判题消息队列
    public static final String EXCHANGE = "problem.result.exchange"; // 判题交换机
    public static final String QUEUE = "problem.result.queue"; // 判题队列
    public static final String ROUTING_KEY = "problem.result.routing.key"; // 判题路由键
    // 死信
    public static final String DEAD_LETTER_EXCHANGE = "problem.result.dead.exchange"; // 死信交换机
    public static final String DEAD_LETTER_QUEUE = "problem.result.dead.queue"; // 死信队列
    public static final String DEAD_LETTER_ROUTING_KEY = "problem.result.dead.routing.key"; // 死信路由键

    // 判题交换机
    @Bean
    public DirectExchange problemResultExchange() {
        return new DirectExchange(EXCHANGE, true, false);
    }

    // 判题队列（配置死信队列）
    @Bean
    public Queue problemResultQueue() {
        return QueueBuilder.durable(QUEUE)
//                .deadLetterExchange(DEAD_LETTER_EXCHANGE) // 死信交换机
//                .deadLetterRoutingKey(DEAD_LETTER_ROUTING_KEY) // 死信路由键
                .build();
    }

    // 判题队列绑定
    @Bean
    public Binding problemResultBinding() {
        return BindingBuilder.bind(problemResultQueue())
                .to(problemResultExchange())
                .with(ROUTING_KEY);
    }
//
//    // 死信交换机
//    @Bean
//    public DirectExchange problemResultDeadLetterExchange() {
//        return new DirectExchange(DEAD_LETTER_EXCHANGE, true, false);
//    }
//
//    // 死信队列
//    @Bean
//    public Queue problemResultDeadLetterQueue() {
//        return new Queue(DEAD_LETTER_QUEUE, true);
//    }
//
//    // 死信队列绑定
//    @Bean
//    public Binding problemJudgeDeadLetterBinding() {
//        return BindingBuilder.bind(problemResultDeadLetterQueue())
//                .to(problemResultDeadLetterExchange())
//                .with(DEAD_LETTER_ROUTING_KEY);
//    }
}
