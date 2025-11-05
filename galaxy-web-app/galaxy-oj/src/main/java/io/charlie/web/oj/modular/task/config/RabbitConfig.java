package io.charlie.web.oj.modular.task.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.AcknowledgeMode;
import org.springframework.amqp.rabbit.config.RetryInterceptorBuilder;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.listener.RabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.retry.RejectAndDontRequeueRecoverer;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author charlie-zhang-code
 * @version v1.0
 * @date 2025/4/13
 * @description Rabbit 配置
 */
@Slf4j
@Configuration
public class RabbitConfig {
    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        rabbitTemplate.setMessageConverter(new Jackson2JsonMessageConverter());
        rabbitTemplate.setChannelTransacted(true);
        rabbitTemplate.setUsePublisherConnection(true);

        // 添加确认回调，确保消息可靠投递
        rabbitTemplate.setConfirmCallback((correlationData, ack, cause) -> {
            if (!ack) {
                log.warn("Message failed to reach broker: {}", cause);
            }
        });

        return rabbitTemplate;
    }


    @Bean
    public RabbitListenerContainerFactory<?> rabbitListenerContainerFactory(ConnectionFactory connectionFactory) {
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);
        factory.setMessageConverter(new Jackson2JsonMessageConverter());
        factory.setConcurrentConsumers(5); // 初始消费者数量
        factory.setMaxConcurrentConsumers(20); // 最大消费者数量
        factory.setPrefetchCount(10); // 控制预取数量
        // 设置重试机制
        factory.setAdviceChain(RetryInterceptorBuilder.stateless()
                .maxAttempts(3) // 最大重试次数
                .backOffOptions(1000, 2.0, 5000) // 初始间隔1秒，倍数2.0，最大间隔10秒
                .recoverer(new RejectAndDontRequeueRecoverer()) // 超过重试次数后不重新入队
                .build());

        return factory;
    }

    /**
     * 判题结果队列专用配置 - 高并发处理
     */
    @Bean("judgeResultContainerFactory")
    public RabbitListenerContainerFactory<?> judgeResultContainerFactory(ConnectionFactory connectionFactory) {
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);
        factory.setMessageConverter(new Jackson2JsonMessageConverter());

        // 判题结果处理相对轻量，可以支持较高并发
        factory.setConcurrentConsumers(3);
        factory.setMaxConcurrentConsumers(10);
        factory.setPrefetchCount(1);

        // 判题结果需要快速确认，避免重复判题
        factory.setAcknowledgeMode(AcknowledgeMode.AUTO);

        // 判题结果处理失败通常不需要重试，直接记录日志
        factory.setAdviceChain(RetryInterceptorBuilder.stateless()
                .maxAttempts(2) // 最多重试1次
                .backOffOptions(500, 1.5, 2000) // 快速重试
                .recoverer(new RejectAndDontRequeueRecoverer() {
                    @Override
                    public void recover(org.springframework.amqp.core.Message message,
                                        Throwable cause) {
                        log.error("判题结果处理失败，消息将丢弃。消息体: {}", new String(message.getBody()), cause);
                    }
                })
                .build());

        return factory;
    }
}
