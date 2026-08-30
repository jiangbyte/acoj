package github.jiangbyte.io.common.log.audit;

import tools.jackson.databind.ObjectMapper;
import github.jiangbyte.io.common.log.config.HeiLogProperties;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Range;
import org.springframework.data.redis.connection.stream.Consumer;
import org.springframework.data.redis.connection.stream.MapRecord;
import org.springframework.data.redis.connection.stream.PendingMessage;
import org.springframework.data.redis.connection.stream.PendingMessages;
import org.springframework.data.redis.connection.stream.ReadOffset;
import org.springframework.data.redis.connection.stream.RecordId;
import org.springframework.data.redis.connection.stream.StreamOffset;
import org.springframework.data.redis.connection.stream.StreamRecords;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.stream.StreamMessageListenerContainer;
import org.springframework.scheduling.annotation.Scheduled;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * 基于 Redis Stream 的审计事件消费者。
 * <p>
 * 使用 XREADGROUP 消费，支持消费者组实现多实例消费。
 * 长度控制优先依赖 XADD MAXLEN ~；定时 XTRIM 默认关闭，避免误伤未 ACK 消息。
 * 通过 XPENDING + XCLAIM 回收超时 pending 消息。
 * </p>
 *
 * Author: Charlie
 */
@Slf4j
public class RedisAuditEventConsumer {

    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;
    private final HeiLogProperties.Audit auditProps;
    private final List<AuditEventHandler> handlers;

    private StreamMessageListenerContainer<String, MapRecord<String, String, String>> container;
    private final AtomicBoolean running = new AtomicBoolean(false);
    private volatile String consumerName = "consumer-unknown";

    public RedisAuditEventConsumer(
            StringRedisTemplate redisTemplate,
            ObjectMapper objectMapper,
            HeiLogProperties.Audit auditProps,
            List<AuditEventHandler> handlers) {
        this.redisTemplate = redisTemplate;
        this.objectMapper = objectMapper;
        this.auditProps = auditProps;
        this.handlers = handlers != null ? handlers : List.of();
    }

    @PostConstruct
    public void start() {
        if (!auditProps.isConsumeEnabled()) {
            log.info("Audit consumer is disabled");
            return;
        }

        if (handlers.isEmpty()) {
            log.warn("No AuditEventHandler found, consumer will not process messages");
            return;
        }

        String streamKey = auditProps.getStreamKey();
        String groupName = auditProps.getGroupName();
        consumerName = "consumer-" + getLocalHostName();

        try {
            // 创建消费者组（如果不存在）
            createConsumerGroupIfNeeded(streamKey, groupName);

            // 创建 StreamMessageListenerContainer
            StreamMessageListenerContainer.StreamMessageListenerContainerOptions<String, MapRecord<String, String, String>> options =
                    StreamMessageListenerContainer.StreamMessageListenerContainerOptions.builder()
                            .pollTimeout(Duration.ofMillis(100))
                            .batchSize(10)
                            .build();

            container = StreamMessageListenerContainer.create(redisTemplate.getConnectionFactory(), options);

            // 使用 XREADGROUP 消费
            Consumer consumer = Consumer.from(groupName, consumerName);
            StreamOffset<String> offset = StreamOffset.create(streamKey, ReadOffset.lastConsumed());

            container.receive(consumer, offset, this::handleMessage);

            container.start();
            running.set(true);
            log.info("Audit consumer started, stream={}, group={}, consumer={}", streamKey, groupName, consumerName);

        } catch (Exception e) {
            log.error("Failed to start audit consumer", e);
        }
    }

    @PreDestroy
    public void stop() {
        running.set(false);
        if (container != null) {
            container.stop();
            log.info("Audit consumer stopped");
        }
    }

    /**
     * 可选的软裁剪：默认关闭。开启时仅做近似 XTRIM，主要仍依赖 XADD MAXLEN。
     */
    @Scheduled(fixedDelayString = "${hei.log.audit.trim-interval-ms:60000}")
    public void trimStream() {
        if (!running.get() || !auditProps.isTrimEnabled()) {
            return;
        }

        String streamKey = auditProps.getStreamKey();
        int maxLen = auditProps.getMaxLen();

        try {
            Long trimmed = redisTemplate.opsForStream().trim(streamKey, maxLen, true);
            if (trimmed != null && trimmed > 0) {
                log.debug("Soft-trimmed {} approximate messages from stream {}", trimmed, streamKey);
            }
        } catch (Exception e) {
            log.warn("Failed to trim stream {}", streamKey, e);
        }
    }

    /**
     * 回收超时 pending：XPENDING + XCLAIM 后重新处理。
     */
    @Scheduled(fixedDelayString = "${hei.log.audit.reclaim-interval-ms:30000}")
    public void reclaimPending() {
        if (!running.get()) {
            return;
        }

        String streamKey = auditProps.getStreamKey();
        String groupName = auditProps.getGroupName();
        Duration minIdle = Duration.ofMillis(Math.max(1000L, auditProps.getReclaimIdleMs()));

        try {
            PendingMessages pending = redisTemplate.opsForStream()
                    .pending(streamKey, groupName, Range.unbounded(), 100);
            if (pending == null || pending.isEmpty()) {
                return;
            }

            List<RecordId> staleIds = new ArrayList<>();
            for (PendingMessage message : pending) {
                if (message.getElapsedTimeSinceLastDelivery() != null
                        && message.getElapsedTimeSinceLastDelivery().compareTo(minIdle) >= 0) {
                    staleIds.add(message.getId());
                }
            }
            if (staleIds.isEmpty()) {
                return;
            }

            List<MapRecord<String, Object, Object>> claimed = redisTemplate.opsForStream()
                    .claim(streamKey, groupName, consumerName, minIdle, staleIds.toArray(RecordId[]::new));
            if (claimed == null || claimed.isEmpty()) {
                return;
            }

            log.info("Reclaimed {} pending audit messages, stream={}, group={}",
                    claimed.size(), streamKey, groupName);
            for (MapRecord<String, Object, Object> record : claimed) {
                handleClaimedMessage(record);
            }
        } catch (Exception e) {
            log.warn("Failed to reclaim pending audit messages, stream={}", streamKey, e);
        }
    }

    private void handleClaimedMessage(MapRecord<String, Object, Object> record) {
        Object raw = record.getValue() != null ? record.getValue().get("data") : null;
        String json = raw != null ? String.valueOf(raw) : null;
        MapRecord<String, String, String> normalized = StreamRecords.newRecord()
                .in(record.getStream())
                .withId(record.getId())
                .ofMap(Map.of("data", json != null ? json : "{}"));
        handleMessage(normalized);
    }

    private void handleMessage(MapRecord<String, String, String> record) {
        String recordId = record.getId().getValue();
        String json = record.getValue().get("data");

        try {
            AuditEventMessage message = objectMapper.readValue(json, AuditEventMessage.class);
            if (message.getMessageId() == null || message.getMessageId().isBlank()) {
                message.setMessageId(recordId);
            }
            log.debug("Received audit event, recordId={}, messageId={}", recordId, message.getMessageId());

            // 调用所有处理器持久化；任一失败则不 ACK
            boolean allOk = true;
            for (AuditEventHandler handler : handlers) {
                try {
                    handler.persist(message);
                } catch (Exception ex) {
                    allOk = false;
                    log.error("Handler {} failed to persist audit event", handler.getClass().getSimpleName(), ex);
                }
            }

            if (!allOk) {
                log.warn("Skip ACK for audit event due to handler failure, recordId={}", recordId);
                return;
            }

            String groupName = auditProps.getGroupName();
            redisTemplate.opsForStream().acknowledge(auditProps.getStreamKey(), groupName, record.getId());
            log.debug("Audit event acknowledged, recordId={}", recordId);

        } catch (Exception e) {
            log.error("Failed to process audit event, recordId={}", recordId, e);
            // 不 ACK，消息会被重新投递
        }
    }

    private void createConsumerGroupIfNeeded(String streamKey, String groupName) {
        try {
            // 3-arg 版本带 MKSTREAM，stream 不存在会自动创建
            redisTemplate.opsForStream().createGroup(streamKey, ReadOffset.from("0"), groupName);
            log.info("Created consumer group, stream={}, group={}", streamKey, groupName);
        } catch (Exception e) {
            String msg = e.getMessage();
            if (msg != null && msg.contains("BUSYGROUP")) {
                log.debug("Consumer group already exists, stream={}, group={}", streamKey, groupName);
            } else if (msg != null && (msg.contains("NOGROUP") || msg.contains("no such key"))) {
                // stream 不存在，先创建 stream 再创建组
                createStreamThenGroup(streamKey, groupName);
            } else {
                log.warn("Failed to create consumer group, stream={}, group={}", streamKey, groupName, e);
            }
        }
    }

    private void createStreamThenGroup(String streamKey, String groupName) {
        try {
            // 写入占位消息创建 stream，再按 RecordId 删掉占位
            MapRecord<String, String, String> placeholder = StreamRecords.newRecord()
                    .in(streamKey)
                    .ofMap(Map.of("data", "{}"));
            RecordId placeholderId = redisTemplate.opsForStream().add(placeholder);
            if (placeholderId != null) {
                redisTemplate.opsForStream().delete(streamKey, placeholderId);
            }
            log.info("Created stream, stream={}", streamKey);

            // 再次尝试创建消费者组
            redisTemplate.opsForStream().createGroup(streamKey, ReadOffset.from("0"), groupName);
            log.info("Created consumer group after stream creation, stream={}, group={}", streamKey, groupName);
        } catch (Exception ex) {
            String msg = ex.getMessage();
            if (msg != null && msg.contains("BUSYGROUP")) {
                log.debug("Consumer group already exists, stream={}, group={}", streamKey, groupName);
            } else {
                log.warn("Failed to create stream and consumer group, stream={}, group={}", streamKey, groupName, ex);
            }
        }
    }

    private static String getLocalHostName() {
        try {
            return java.net.InetAddress.getLocalHost().getHostName();
        } catch (Exception e) {
            return "unknown";
        }
    }
}
