package github.jiangbyte.io.oj.modules.judge.mq;

import github.jiangbyte.io.common.core.exception.BizException;
import github.jiangbyte.io.oj.config.OjProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageDeliveryMode;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * 判题消息发布：work 立即投递 / retry 带 TTL 延迟。
 *
 * Author: Charlie
 */
@Slf4j
@Component
public class OjJudgePublisher {

    private final OjProperties ojProperties;
    private final ObjectMapper objectMapper;
    private final RabbitTemplate ojJudgeRabbitTemplate;

    public OjJudgePublisher(
            OjProperties ojProperties,
            ObjectMapper objectMapper,
            @Qualifier(OjJudgeMqConfig.RABBIT_TEMPLATE) RabbitTemplate ojJudgeRabbitTemplate) {
        this.ojProperties = ojProperties;
        this.objectMapper = objectMapper;
        this.ojJudgeRabbitTemplate = ojJudgeRabbitTemplate;
    }

    /** 发布到主判题队列。 */
    public void publishWork(OjJudgeMessage message) {
        send(OjJudgeMqConfig.ROUTING_WORK, message, null);
    }

    /** 发布到重试队列，消息级 TTL（毫秒）到期后经 DLX 回 work。 */
    public void publishRetry(OjJudgeMessage message, long expirationMs) {
        long ttl = Math.max(1L, expirationMs);
        send(OjJudgeMqConfig.ROUTING_RETRY, message, String.valueOf(ttl));
    }

    /** 毒消息进 DLQ。 */
    public void publishDlq(OjJudgeMessage message) {
        send(OjJudgeMqConfig.ROUTING_DLQ, message, null);
    }

    private void send(String routingKey, OjJudgeMessage payload, String expirationMs) {
        try {
            Map<String, Object> body = new HashMap<>();
            body.put("submission_id", payload.submissionId());
            body.put("request_id", payload.requestId());
            body.put("enqueue_at", payload.enqueueAt());
            body.put("reason", payload.reason());
            byte[] bytes = objectMapper.writeValueAsBytes(body);

            MessageProperties props = new MessageProperties();
            props.setContentType(MessageProperties.CONTENT_TYPE_JSON);
            props.setContentEncoding(StandardCharsets.UTF_8.name());
            props.setDeliveryMode(MessageDeliveryMode.PERSISTENT);
            if (expirationMs != null) {
                props.setExpiration(expirationMs);
            }
            Message message = new Message(bytes, props);

            CorrelationData correlation = new CorrelationData(UUID.randomUUID().toString());
            String exchange = ojProperties.getJudge().getMq().getExchange();
            ojJudgeRabbitTemplate.send(exchange, routingKey, message, correlation);
            CorrelationData.Confirm confirm = correlation.getFuture().get(5, TimeUnit.SECONDS);
            if (confirm == null || !confirm.isAck()) {
                throw new BizException("判题消息未确认: " + (confirm == null ? "timeout" : confirm.getReason()));
            }
        } catch (BizException ex) {
            throw ex;
        } catch (Exception ex) {
            log.warn("publish oj.judge.{} failed: {}", routingKey, ex.toString());
            throw new BizException("判题消息发布失败: " + ex.getMessage());
        }
    }
}
