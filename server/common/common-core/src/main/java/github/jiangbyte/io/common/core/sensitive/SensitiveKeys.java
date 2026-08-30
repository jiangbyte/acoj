package github.jiangbyte.io.common.core.sensitive;

import java.util.Arrays;
import java.util.Collection;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 日志等场景的敏感键名匹配工具（非 HTTP Jackson 脱敏）。
 *
 * <p>HTTP 响应脱敏请用 {@link github.jiangbyte.io.common.core.jackson.Sensitive}。
 * 匹配规则：规范化后全等，或以敏感词结尾（如 {@code userPassword}）；不用 {@code contains}。
 *
 * Author: Charlie
 */
public final class SensitiveKeys {

    /**
     * 日志脱敏默认键。不含裸 {@code token}；需要时可通过配置显式加入。
     */
    public static final Set<String> DEFAULT = Set.of(
            "password",
            "secret",
            "cryptokey",
            "crypto-key",
            "accesskey",
            "access-key",
            "privatekey",
            "private-key",
            "accesstoken",
            "refreshtoken",
            "idtoken",
            "apitoken",
            "authtoken",
            "authorization");

    private SensitiveKeys() {
    }

    public static Set<String> normalize(Collection<String> keys) {
        if (keys == null || keys.isEmpty()) {
            return DEFAULT;
        }
        return keys.stream()
                .filter(k -> k != null && !k.isBlank())
                .map(SensitiveKeys::canonical)
                .collect(Collectors.toUnmodifiableSet());
    }

    public static Set<String> normalizeCsv(String csv) {
        if (csv == null || csv.isBlank()) {
            return DEFAULT;
        }
        return normalize(Arrays.asList(csv.split(",")));
    }

    public static boolean matches(String name, Set<String> keys) {
        if (name == null || name.isBlank() || keys == null || keys.isEmpty()) {
            return false;
        }
        String c = canonical(name);
        for (String key : keys) {
            if (key == null || key.isBlank()) {
                continue;
            }
            if (c.equals(key)) {
                return true;
            }
            // user_password / api_secret → 以敏感词结尾
            if (c.length() > key.length() && c.endsWith(key)) {
                return true;
            }
        }
        return false;
    }

    private static String canonical(String name) {
        return name.trim().toLowerCase(Locale.ROOT).replace("_", "").replace("-", "");
    }
}
