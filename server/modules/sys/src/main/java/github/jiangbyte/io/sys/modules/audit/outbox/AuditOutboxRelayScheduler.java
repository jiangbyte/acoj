package github.jiangbyte.io.sys.modules.audit.outbox;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import tools.jackson.databind.ObjectMapper;
import github.jiangbyte.io.common.log.audit.AuditEventMessage;
import github.jiangbyte.io.common.log.config.HeiLogProperties;
import github.jiangbyte.io.sys.modules.audit.entity.SysOperationAuditOutbox;
import github.jiangbyte.io.sys.modules.audit.mapper.SysOperationAuditOutboxMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.RedisStreamCommands.TrimOptions;
import org.springframework.data.redis.connection.RedisStreamCommands.XAddOptions;
import org.springframework.data.redis.connection.stream.MapRecord;
import org.springframework.data.redis.connection.stream.RecordId;
import org.springframework.data.redis.connection.stream.StreamRecords;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 审计 outbox relay：认领 PENDING → XADD Redis Stream → DONE；失败累加 attempts，达上限进 DEAD。
 *
 * Author: Charlie
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AuditOutboxRelayScheduler {

    private final SysOperationAuditOutboxMapper outboxMapper;
    private final AuditOutboxClaimSupport outboxClaimSupport;
    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;
    private final HeiLogProperties heiLogProperties;
    private final TransactionTemplate transactionTemplate;

    @Scheduled(fixedDelayString = "${hei.log.audit.outbox-relay-interval-ms:2000}")
    public void relay() {
        HeiLogProperties.Audit audit = heiLogProperties.getAudit();
        if (!audit.isAsyncEnabled()) {
            return;
        }
        int batchSize = Math.max(1, audit.getOutboxRelayBatchSize());
        OffsetDateTime staleBefore = OffsetDateTime.now()
                .minus(Duration.ofMillis(Math.max(1000L, audit.getOutboxClaimStaleMs())));

        List<SysOperationAuditOutbox> claimed = transactionTemplate.execute(
                status -> outboxClaimSupport.claimBatch(batchSize, staleBefore));
        if (claimed == null || claimed.isEmpty()) {
            return;
        }

        int maxAttempts = Math.max(1, audit.getOutboxMaxAttempts());
        for (SysOperationAuditOutbox row : claimed) {
            try {
                publishToStream(row.getPayload(), audit);
                markDone(row.getId());
            } catch (Exception ex) {
                handleFailure(row, maxAttempts, ex);
            }
        }
    }

    private void publishToStream(String payloadJson, HeiLogProperties.Audit audit) throws Exception {
        AuditEventMessage message = objectMapper.readValue(payloadJson, AuditEventMessage.class);
        Map<String, String> fields = new HashMap<>();
        fields.put("data", objectMapper.writeValueAsString(message));
        fields.put("timestamp", String.valueOf(message.getOccurredAt() != null
                ? message.getOccurredAt().toEpochMilli()
                : System.currentTimeMillis()));

        MapRecord<String, String, String> record = StreamRecords.newRecord()
                .in(audit.getStreamKey())
                .ofMap(fields);
        RecordId recordId = redisTemplate.opsForStream()
                .add(record, XAddOptions.trim(TrimOptions.maxLen(audit.getMaxLen()).approximate()));
        if (recordId == null) {
            throw new IllegalStateException("XADD returned null record id");
        }
    }

    private void markDone(String id) {
        outboxMapper.update(null, Wrappers.<SysOperationAuditOutbox>lambdaUpdate()
                .eq(SysOperationAuditOutbox::getId, id)
                .set(SysOperationAuditOutbox::getStatus, "DONE"));
    }

    private void handleFailure(SysOperationAuditOutbox row, int maxAttempts, Exception ex) {
        int attempts = row.getAttempts() == null ? 1 : row.getAttempts();
        if (attempts >= maxAttempts) {
            outboxMapper.update(null, Wrappers.<SysOperationAuditOutbox>lambdaUpdate()
                    .eq(SysOperationAuditOutbox::getId, row.getId())
                    .set(SysOperationAuditOutbox::getStatus, "DEAD"));
            log.error("Audit outbox dead-lettered, id={}, attempts={}, error={}",
                    row.getId(), attempts, ex.getMessage(), ex);
        } else {
            outboxMapper.update(null, Wrappers.<SysOperationAuditOutbox>lambdaUpdate()
                    .eq(SysOperationAuditOutbox::getId, row.getId())
                    .set(SysOperationAuditOutbox::getStatus, "PENDING")
                    .set(SysOperationAuditOutbox::getClaimedAt, null));
            log.warn("Audit outbox relay failed, will retry, id={}, attempts={}, error={}",
                    row.getId(), attempts, ex.getMessage());
        }
    }
}
