package github.jiangbyte.io.common.log.audit;

/**
 * 审计事件持久化接口：供具体业务模块实现（如 sys 模块）。
 *
 * Author: Charlie
 */
public interface AuditEventHandler {

    /**
     * 持久化审计事件到数据库。
     *
     * @param message 审计消息
     */
    void persist(AuditEventMessage message);
}
