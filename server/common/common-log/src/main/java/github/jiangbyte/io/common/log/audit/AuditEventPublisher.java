package github.jiangbyte.io.common.log.audit;

/**
 * 审计事件发布接口：将审计消息投递到消息队列。
 *
 * Author: Charlie
 */
public interface AuditEventPublisher {

    /** 发布审计事件消息。 */
    void publish(AuditEventMessage message);
}
