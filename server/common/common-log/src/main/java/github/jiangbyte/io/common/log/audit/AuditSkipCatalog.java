package github.jiangbyte.io.common.log.audit;

import org.springframework.util.StringUtils;

import java.util.Locale;
import java.util.Set;

/**
 * 高频/低价值操作审计跳过表（resourceType + action）。
 *
 * Author: Charlie
 */
public final class AuditSkipCatalog {

    private static final Set<String> SKIP_KEYS = Set.of(
            // 会话与验证码
            "auth:refresh",
            "auth:send_login_code",
            "auth:send_register_code",
            // 文件与头像上传
            "sys_file:upload",
            "profile_center:upload_avatar",
            // 个人中心发码
            "profile_center:send_password_code",
            "profile_center:send_bind_phone_code",
            "profile_center:send_bind_email_code",
            // 消息已读
            "sys_notice:read",
            "sys_notice:read_all",
            // 展示图互动（曝光/点击）
            "sys_banner:interaction",
            // 第三方实名回调（轮询/回调频繁）
            "real_name_case:callback");

    private AuditSkipCatalog() {
    }

    public static boolean shouldSkip(String resourceType, String action) {
        if (!StringUtils.hasText(resourceType) || !StringUtils.hasText(action)) {
            return false;
        }
        return SKIP_KEYS.contains(key(resourceType, action));
    }

    private static String key(String resourceType, String action) {
        return normalize(resourceType) + ":" + normalize(action);
    }

    private static String normalize(String value) {
        return value.trim()
                .toLowerCase(Locale.ROOT)
                .replace('-', '_');
    }
}
