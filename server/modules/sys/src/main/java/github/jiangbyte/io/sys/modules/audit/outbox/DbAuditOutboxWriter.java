package github.jiangbyte.io.sys.modules.audit.outbox;

import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;
import github.jiangbyte.io.common.log.audit.AuditEventMessage;
import github.jiangbyte.io.common.log.audit.AuditOutboxWriter;
import github.jiangbyte.io.sys.modules.audit.entity.SysOperationAuditOutbox;
import github.jiangbyte.io.sys.modules.audit.mapper.SysOperationAuditOutboxMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;

/**
 * 将审计事件以 PENDING 写入 sys_operation_audit_outbox（独立事务）。
 *
 * Author: Charlie
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DbAuditOutboxWriter implements AuditOutboxWriter {

    private final SysOperationAuditOutboxMapper outboxMapper;
    private final ObjectMapper objectMapper;

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void write(AuditEventMessage message) {
        if (message == null) {
            return;
        }
        try {
            SysOperationAuditOutbox row = new SysOperationAuditOutbox();
            row.setPayload(objectMapper.writeValueAsString(message));
            row.setStatus("PENDING");
            row.setAttempts(0);
            row.setCreatedAt(OffsetDateTime.now());
            outboxMapper.insert(row);
        } catch (JacksonException e) {
            log.error("Failed to serialize audit event for outbox", e);
        } catch (Exception e) {
            log.error("Failed to write audit outbox", e);
        }
    }
}
