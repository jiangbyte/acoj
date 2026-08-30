package github.jiangbyte.io.common.web.util;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.util.StringUtils;

/**
 * 客户端 IP 解析工具，带显式 X-Forwarded-For / X-Real-IP 信任开关。
 *
 * Author: Charlie
 */
public final class ClientIpResolver {

    private ClientIpResolver() {
    }

    /** 解析客户端 IP（可选信任转发头）。 */
    public static String resolve(HttpServletRequest request, boolean trustForwardedHeaders) {
        if (trustForwardedHeaders) {
            String forwarded = request.getHeader("X-Forwarded-For");
            if (StringUtils.hasText(forwarded)) {
                String first = forwarded.split(",")[0].trim();
                if (StringUtils.hasText(first)) {
                    return first;
                }
            }
            String realIp = request.getHeader("X-Real-IP");
            if (StringUtils.hasText(realIp)) {
                return realIp.trim();
            }
        }
        String remote = request.getRemoteAddr();
        return StringUtils.hasText(remote) ? remote : "unknown";
    }
}
