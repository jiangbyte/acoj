package github.jiangbyte.io.sys.modules.audit;

import github.jiangbyte.io.common.log.audit.AuditEventHandler;
import github.jiangbyte.io.common.log.audit.AuditEventMessage;
import github.jiangbyte.io.sys.modules.audit.service.AuditService;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/**
 * Sys 模块审计事件处理器：实现 AuditEventHandler 接口供 RedisAuditEventConsumer 调用。
 *
 * Author: Charlie
 */
@Component
@ConditionalOnClass(AuditEventHandler.class)
@RequiredArgsConstructor
public class SysAuditEventHandler implements AuditEventHandler {

    private final AuditService auditService;

    @Override
    public void persist(AuditEventMessage message) {
        auditService.persistEvent(message);
    }
}
