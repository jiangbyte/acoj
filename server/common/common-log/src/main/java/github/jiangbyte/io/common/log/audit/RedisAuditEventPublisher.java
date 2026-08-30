package github.jiangbyte.io.common.log.audit;

import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;
import github.jiangbyte.io.common.log.config.HeiLogProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.stream.MapRecord;
import org.springframework.data.redis.connection.stream.RecordId;
import org.springframework.data.redis.connection.stream.StreamInfo;
import org.springframework.data.redis.connection.stream.StreamRecords;
import org.springframework.data.redis.connection.RedisStreamCommands.XAddOptions;
import org.springframework.data.redis.connection.RedisStreamCommands.TrimOptions;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.util.HashMap;
import java.util.Map;

/**
 * 基于 Redis Stream 的审计事件发布器。
 * <p>
 * 使用 XADD 发布消息，配合 MAXLEN 限制堆积。
 * </p>
 *
 * Author: Charlie
 */
@Slf4j
@RequiredArgsConstructor
public class RedisAuditEventPublisher implements AuditEventPublisher {

    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;
    private final HeiLogProperties.Audit auditProps;

    @Override
    public void publish(AuditEventMessage message) {
        if (message == null) {
            return;
        }
        String streamKey = auditProps.getStreamKey();
        int maxLen = auditProps.getMaxLen();

        Map<String, String> fields = toFields(message);

        try {
            // 使用 XADD + MAXLEN ~ 近似裁剪策略
            MapRecord<String, String, String> record = StreamRecords.newRecord()
                    .in(streamKey)
                    .ofMap(fields);
            RecordId recordId = redisTemplate.opsForStream().add(record, XAddOptions.trim(TrimOptions.maxLen(maxLen).approximate()));

            if (recordId != null) {
                message.setMessageId(recordId.getValue());
                log.debug("Audit event published to Redis Stream, stream={}, recordId={}", streamKey, recordId);
            } else {
                log.warn("Failed to publish audit event, recordId is null");
            }
        } catch (Exception e) {
            log.error("Failed to publish audit event to Redis Stream, stream={}", streamKey, e);
        }
    }

    private Map<String, String> toFields(AuditEventMessage message) {
        Map<String, String> fields = new HashMap<>();
        try {
            String json = objectMapper.writeValueAsString(message);
            fields.put("data", json);
            fields.put("timestamp", String.valueOf(message.getOccurredAt() != null
                    ? message.getOccurredAt().toEpochMilli()
                    : System.currentTimeMillis()));
        } catch (JacksonException e) {
            log.error("Failed to serialize AuditEventMessage", e);
            fields.put("data", "{}");
        }
        return fields;
    }
}
