//package io.charlie.app.core.mq.problem.similarity;
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
//public class PSResMQConfig {
//    // 判题消息队列
//    public static final String EXCHANGE = "problem.similarity.result.exchange"; // 判题交换机
//    public static final String QUEUE = "problem.similarity.result.queue"; // 判题队列
//    public static final String ROUTING_KEY = "problem.similarity.result.routing.key"; // 判题路由键
//    // 死信
//    public static final String DEAD_LETTER_EXCHANGE = "problem.similarity.result.dead.exchange"; // 死信交换机
//    public static final String DEAD_LETTER_QUEUE = "problem.similarity.result.dead.queue"; // 死信队列
//    public static final String DEAD_LETTER_ROUTING_KEY = "problem.similarity.result.dead.routing.key"; // 死信路由键
//
//    // 判题交换机
//    @Bean
//    public DirectExchange problemSimilarityResultExchange() {
//        return new DirectExchange(EXCHANGE, true, false);
//    }
//
//    // 判题队列（配置死信队列）
//    @Bean
//    public Queue problemSimilarityResultQueue() {
//        return QueueBuilder.durable(QUEUE)
////                .deadLetterExchange(DEAD_LETTER_EXCHANGE) // 死信交换机
////                .deadLetterRoutingKey(DEAD_LETTER_ROUTING_KEY) // 死信路由键
//                .build();
//    }
//
//    // 判题队列绑定
//    @Bean
//    public Binding problemJudgeBinding() {
//        return BindingBuilder.bind(problemSimilarityResultQueue())
//                .to(problemSimilarityResultExchange())
//                .with(ROUTING_KEY);
//    }
//
////    // 死信交换机
////    @Bean
////    public DirectExchange problemSimilarityResultDeadLetterExchange() {
////        return new DirectExchange(DEAD_LETTER_EXCHANGE, true, false);
////    }
////
////    // 死信队列
////    @Bean
////    public Queue problemSimilarityResultDeadLetterQueue() {
////        return new Queue(DEAD_LETTER_QUEUE, true);
////    }
////
////    // 死信队列绑定
////    @Bean
////    public Binding problemJudgeDeadLetterBinding() {
////        return BindingBuilder.bind(problemSimilarityResultDeadLetterQueue())
////                .to(problemSimilarityResultDeadLetterExchange())
////                .with(DEAD_LETTER_ROUTING_KEY);
////    }
//}
