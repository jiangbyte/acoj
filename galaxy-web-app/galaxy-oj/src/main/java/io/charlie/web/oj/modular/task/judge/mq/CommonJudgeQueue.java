package io.charlie.web.oj.modular.task.judge.mq;

import lombok.RequiredArgsConstructor;
import org.springframework.amqp.core.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author ZhangJiangHu
 * @version v1.0
 * @date 20/09/2025
 * @description TODO
 */
@Configuration
@RequiredArgsConstructor
public class CommonJudgeQueue {
    // 判题消息队列
    public static final String EXCHANGE = "common.judge.exchange"; // 判题交换机
    public static final String QUEUE = "common.judge.queue"; // 判题队列
    public static final String ROUTING_KEY = "common.judge.routing"; // 判题路由键

    // 判题交换机
    @Bean
    public DirectExchange commonJudgeExchange() {
        return new DirectExchange(EXCHANGE, true, false);
    }

    // 判题队列（配置死信队列）
    @Bean
    public Queue commonJudgeQueueQ() {
        return QueueBuilder.durable(QUEUE)
//                .deadLetterExchange(DEAD_LETTER_EXCHANGE) // 死信交换机
//                .deadLetterRoutingKey(DEAD_LETTER_ROUTING_KEY) // 死信路由键
                .build();
    }

    // 判题队列绑定
    @Bean
    public Binding commonJudgeBinding() {
        return BindingBuilder.bind(commonJudgeQueueQ())
                .to(commonJudgeExchange())
                .with(ROUTING_KEY);
    }
    // 判题消息队列
    public static final String BACK_EXCHANGE = "common.judge.result.exchange"; // 判题交换机
    public static final String BACK_QUEUE = "common.judge.result.queue"; // 判题队列
    public static final String BACK_ROUTING_KEY = "common.judge.result.routing"; // 判题路由键

    // 判题交换机
    @Bean
    public DirectExchange commonBackJudgeExchange() {
        return new DirectExchange(BACK_EXCHANGE, true, false);
    }

    // 判题队列（配置死信队列）
    @Bean
    public Queue commonBackJudgeQueue() {
        return QueueBuilder.durable(BACK_QUEUE)
//                .deadLetterExchange(DEAD_LETTER_EXCHANGE) // 死信交换机
//                .deadLetterRoutingKey(DEAD_LETTER_ROUTING_KEY) // 死信路由键
                .build();
    }

    // 判题队列绑定
    @Bean
    public Binding commonBackJudgeBinding() {
        return BindingBuilder.bind(commonBackJudgeQueue())
                .to(commonBackJudgeExchange())
                .with(BACK_ROUTING_KEY);
    }
}
