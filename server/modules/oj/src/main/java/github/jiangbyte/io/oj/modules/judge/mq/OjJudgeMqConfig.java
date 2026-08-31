package github.jiangbyte.io.oj.modules.judge.mq;

import github.jiangbyte.io.oj.config.OjProperties;
import org.springframework.amqp.core.AcknowledgeMode;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 判题 RabbitMQ 拓扑：work / retry(TTL+DLX) / dlq。
 *
 * Author: Charlie
 */
@Configuration
public class OjJudgeMqConfig {

    public static final String ROUTING_WORK = "work";
    public static final String ROUTING_RETRY = "retry";
    public static final String ROUTING_DLQ = "dlq";

    public static final String LISTENER_FACTORY = "ojJudgeListenerContainerFactory";
    public static final String RABBIT_TEMPLATE = "ojJudgeRabbitTemplate";

    @Bean
    public TopicExchange ojJudgeExchange(OjProperties properties) {
        return new TopicExchange(properties.getJudge().getMq().getExchange(), true, false);
    }

    @Bean
    public TopicExchange ojJudgeDlxExchange(OjProperties properties) {
        return new TopicExchange(dlxName(properties), true, false);
    }

    @Bean
    public Queue ojJudgeWorkQueue(OjProperties properties) {
        return QueueBuilder.durable(properties.getJudge().getMq().getWorkQueue()).build();
    }

    @Bean
    public Queue ojJudgeRetryQueue(OjProperties properties) {
        return QueueBuilder.durable(properties.getJudge().getMq().getRetryQueue())
                .deadLetterExchange(dlxName(properties))
                .deadLetterRoutingKey(ROUTING_WORK)
                .build();
    }

    @Bean
    public Queue ojJudgeDlq(OjProperties properties) {
        return QueueBuilder.durable(properties.getJudge().getMq().getDlq()).build();
    }

    @Bean
    public Binding ojJudgeWorkBinding(Queue ojJudgeWorkQueue, TopicExchange ojJudgeExchange) {
        return BindingBuilder.bind(ojJudgeWorkQueue).to(ojJudgeExchange).with(ROUTING_WORK);
    }

    @Bean
    public Binding ojJudgeRetryBinding(Queue ojJudgeRetryQueue, TopicExchange ojJudgeExchange) {
        return BindingBuilder.bind(ojJudgeRetryQueue).to(ojJudgeExchange).with(ROUTING_RETRY);
    }

    @Bean
    public Binding ojJudgeDlqBinding(Queue ojJudgeDlq, TopicExchange ojJudgeExchange) {
        return BindingBuilder.bind(ojJudgeDlq).to(ojJudgeExchange).with(ROUTING_DLQ);
    }

    @Bean
    public Binding ojJudgeDlxToWorkBinding(Queue ojJudgeWorkQueue, TopicExchange ojJudgeDlxExchange) {
        return BindingBuilder.bind(ojJudgeWorkQueue).to(ojJudgeDlxExchange).with(ROUTING_WORK);
    }

    @Bean(name = RABBIT_TEMPLATE)
    public RabbitTemplate ojJudgeRabbitTemplate(ConnectionFactory connectionFactory) {
        if (connectionFactory instanceof CachingConnectionFactory caching) {
            caching.setPublisherConfirmType(CachingConnectionFactory.ConfirmType.CORRELATED);
            caching.setPublisherReturns(true);
        }
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMandatory(true);
        return template;
    }

    @Bean(name = LISTENER_FACTORY)
    public SimpleRabbitListenerContainerFactory ojJudgeListenerContainerFactory(
            ConnectionFactory connectionFactory,
            OjProperties properties) {
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);
        factory.setAcknowledgeMode(AcknowledgeMode.MANUAL);
        factory.setPrefetchCount(properties.getJudge().getMq().getPrefetch());
        factory.setConcurrentConsumers(1);
        factory.setMaxConcurrentConsumers(Math.max(1, properties.getJudge().getWorkerConcurrency()));
        factory.setMissingQueuesFatal(false);
        factory.setAutoStartup(true);
        return factory;
    }

    static String dlxName(OjProperties properties) {
        return properties.getJudge().getMq().getExchange() + ".dlx";
    }
}
