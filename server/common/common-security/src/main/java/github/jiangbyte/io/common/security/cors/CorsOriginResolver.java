package github.jiangbyte.io.common.security.cors;

import github.jiangbyte.io.common.security.config.HeiSecurityProperties;
import org.springframework.core.env.Environment;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

/**
 * CORS 允许源解析：合并配置、前端基址与本地开发默认端口。
 *
 * Author: Charlie
 */
public final class CorsOriginResolver {

    private static final List<String> LOCAL_DEFAULTS = List.of(
            "http://localhost:5173",
            "http://127.0.0.1:5173",
            "http://localhost:5163",
            "http://127.0.0.1:5163",
            "http://localhost:5174",
            "http://127.0.0.1:5174");

    private CorsOriginResolver() {
    }

    /** 解析允许的 CORS Origin 列表。 */
    public static List<String> resolve(HeiSecurityProperties securityProperties, Environment environment) {
        Set<String> origins = new LinkedHashSet<>();
        List<String> configured =
                securityProperties == null ? List.of() : securityProperties.getCorsAllowedOrigins();
        if (configured != null) {
            for (String origin : configured) {
                if (StringUtils.hasText(origin)) {
                    origins.add(origin.trim());
                }
            }
        }
        if (origins.isEmpty()) {
            origins.addAll(LOCAL_DEFAULTS);
        }
        if (environment != null) {
            String frontend = environment.getProperty("hei.app.frontend-base-url");
            if (StringUtils.hasText(frontend)) {
                origins.add(frontend.trim());
            }
        }
        return new ArrayList<>(origins);
    }

    public static boolean allowsAny(List<String> allowedOrigins) {
        if (allowedOrigins == null) {
            return false;
        }
        for (String origin : allowedOrigins) {
            if ("*".equals(origin)) {
                return true;
            }
        }
        return false;
    }

    /** 判断请求 Origin 是否允许。 */
    public static boolean isAllowed(String requestOrigin, List<String> allowedOrigins) {
        if (!StringUtils.hasText(requestOrigin) || allowedOrigins == null || allowedOrigins.isEmpty()) {
            return false;
        }
        if (allowsAny(allowedOrigins)) {
            return true;
        }
        String normalized = requestOrigin.trim();
        for (String allowed : allowedOrigins) {
            if (allowed != null && allowed.equalsIgnoreCase(normalized)) {
                return true;
            }
            if (matchesPattern(normalized, allowed)) {
                return true;
            }
        }
        return false;
    }

    /** 极简 pattern：仅支持末尾 {@code *}（如 {@code http://localhost:*}）。 */
    private static boolean matchesPattern(String origin, String pattern) {
        if (pattern == null || !pattern.endsWith("*") || pattern.length() == 1) {
            return false;
        }
        String prefix = pattern.substring(0, pattern.length() - 1);
        return origin.toLowerCase(Locale.ROOT).startsWith(prefix.toLowerCase(Locale.ROOT));
    }
}
