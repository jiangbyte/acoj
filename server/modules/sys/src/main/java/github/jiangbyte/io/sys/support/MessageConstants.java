package github.jiangbyte.io.sys.support;

/**
 * 消息模块常量：发布状态、反馈状态、目标范围与消息类型（通知/公告）。
 *
 * Author: Charlie
 */
public final class MessageConstants {

    public static final String DRAFT = "DRAFT";
    public static final String PUBLISHED = "PUBLISHED";
    public static final String REVOKED = "REVOKED";
    public static final String FEEDBACK_PENDING = "PENDING";
    public static final String TARGET_ALL = "ALL";
    public static final String TARGET_ACCOUNT_TYPE = "ACCOUNT_TYPE";
    public static final String TARGET_SPECIFIC = "SPECIFIC";
    public static final String KIND_NOTIFICATION = "NOTIFICATION";
    public static final String KIND_ANNOUNCEMENT = "ANNOUNCEMENT";

    private MessageConstants() {
    }
}
