package github.jiangbyte.io.common.log.audit;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;
import java.time.Instant;
import java.util.Map;

/**
 * 审计事件消息体：资源、动作、操作者与请求上下文等字段。
 *
 * Author: Charlie
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuditEventMessage implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private String resourceType;
    private String resourceId;
    private String action;
    private String actionName;
    private String actionType;
    private String moduleLabel;
    private String operatorName;
    /** 可读操作内容 */
    private String summary;
    private Map<String, Object> beforeData;
    private Map<String, Object> afterData;
    private Integer durationMs;
    private String method;
    private String path;
    private Integer statusCode;
    private String accountId;
    private String accountType;
    private String requestId;
    private String ip;
    private String userAgent;
    private Instant occurredAt;
    /**
     * 消息 ID；在发布到 Stream 后设置，供消费者追踪。
     */
    private String messageId;
}
