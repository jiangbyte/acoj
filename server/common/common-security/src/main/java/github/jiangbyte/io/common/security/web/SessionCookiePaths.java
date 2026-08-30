package github.jiangbyte.io.common.security.web;

import org.springframework.util.StringUtils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 会话 Cookie Path 约定与改写辅助方法。
 *
 * Author: Charlie
 */
public final class SessionCookiePaths {

    public static final String LEGACY_ROOT_PATH = "/";
    public static final String LEGACY_COOKIE_NAME = "hei_session";

    private static final Pattern CLIENT_ROOT =
            Pattern.compile("^(/api/v\\d+/(admin|portal))(/|$)");

    private SessionCookiePaths() {
    }

    /**
     * 从请求路径解析客户端 Cookie Path：固定为 {@code /api/{ver}/{admin|portal}}，
     * 避免 refresh 等深层路径把 Path 收窄到 {@code /api/v1/admin/auth}。
     */
    public static String fromRequestPath(String requestPath) {
        if (!StringUtils.hasText(requestPath)) {
            return LEGACY_ROOT_PATH;
        }
        String path = requestPath;
        while (path.length() > 1 && path.endsWith("/")) {
            path = path.substring(0, path.length() - 1);
        }
        if (!StringUtils.hasText(path) || "/".equals(path)) {
            return LEGACY_ROOT_PATH;
        }
        Matcher matcher = CLIENT_ROOT.matcher(path);
        if (matcher.find()) {
            return matcher.group(1);
        }
        return path;
    }
}
