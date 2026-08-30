package github.jiangbyte.io.common.log.audit;

/**
 * 审计 outbox 写入 SPI：由业务模块（如 sys）实现，common-log 不依赖业务库表。
 * <p>
 * 存在实现时，{@code OperationAuditAspect} 优先写入 outbox；否则回退到 {@link AuditEventPublisher}。
 * </p>
 *
 * Author: Charlie
 */
public interface AuditOutboxWriter {

    /** 将审计事件以 PENDING 状态写入 outbox（建议独立事务）。 */
    void write(AuditEventMessage message);
}
