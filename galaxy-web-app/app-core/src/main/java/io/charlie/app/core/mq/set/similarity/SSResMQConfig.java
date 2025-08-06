//package io.charlie.app.core.mq.set.similarity;
//
//import lombok.RequiredArgsConstructor;
//import org.springframework.amqp.core.*;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//
///**
// * @author Charlie Zhang
// * @version v1.0
// * @date 23/07/2025
// * @description 题目提交消息队列配置
// */
//@Configuration
//@RequiredArgsConstructor
//public class SSResMQConfig {
//    // 判题消息队列
//    public static final String EXCHANGE = "set.similarity.result.exchange"; // 判题交换机
//    public static final String QUEUE = "set.similarity.result.queue"; // 判题队列
//    public static final String ROUTING_KEY = "set.similarity.result.routing.key"; // 判题路由键
//    // 死信
//    public static final String DEAD_LETTER_EXCHANGE = "set.similarity.result.dead.exchange"; // 死信交换机
//    public static final String DEAD_LETTER_QUEUE = "set.similarity.result.dead.queue"; // 死信队列
//    public static final String DEAD_LETTER_ROUTING_KEY = "set.similarity.result.dead.routing.key"; // 死信路由键
//
//    // 判题交换机
//    @Bean
//    public DirectExchange setSimilarityResultExchange() {
//        return new DirectExchange(EXCHANGE, true, false);
//    }
//
//    // 判题队列（配置死信队列）
//    @Bean
//    public Queue setSimilarityResultQueue() {
//        return QueueBuilder.durable(QUEUE)
////                .deadLetterExchange(DEAD_LETTER_EXCHANGE) // 死信交换机
////                .deadLetterRoutingKey(DEAD_LETTER_ROUTING_KEY) // 死信路由键
//                .build();
//    }
//
//    // 判题队列绑定
//    @Bean
//    public Binding setSimilarityJudgeBinding() {
//        return BindingBuilder.bind(setSimilarityResultQueue())
//                .to(setSimilarityResultExchange())
//                .with(ROUTING_KEY);
//    }
//
//    // 死信交换机
////    @Bean
////    public DirectExchange setSimilarityResultDeadLetterExchange() {
////        return new DirectExchange(DEAD_LETTER_EXCHANGE, true, false);
////    }
////
////    // 死信队列
////    @Bean
////    public Queue setSimilarityResultDeadLetterQueue() {
////        return new Queue(DEAD_LETTER_QUEUE, true);
////    }
////
////    // 死信队列绑定
////    @Bean
////    public Binding setSimilarityJudgeDeadLetterBinding() {
////        return BindingBuilder.bind(setSimilarityResultDeadLetterQueue())
////                .to(setSimilarityResultDeadLetterExchange())
////                .with(DEAD_LETTER_ROUTING_KEY);
////    }
//}
