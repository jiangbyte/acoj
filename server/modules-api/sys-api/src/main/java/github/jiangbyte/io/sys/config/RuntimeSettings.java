package github.jiangbyte.io.sys.config;

import java.util.Collections;
import java.util.Locale;
import java.util.Map;

/**
 * {@code sys_config} 的不可变视图：持有配置 Map 与版本号，供热更新后的只读访问与类型转换。
 *
 * Author: Charlie
 */
public final class RuntimeSettings {

    private final Map<String, String> values;
    private final long version;

    public RuntimeSettings(Map<String, String> values, long version) {
        this.values = Collections.unmodifiableMap(values);
        this.version = version;
    }

    /** 当前快照版本号（每次 reload 递增）。 */
    public long version() {
        return version;
    }

    /** 原始配置 Map（不可变）。 */
    public Map<String, String> asMap() {
        return values;
    }

    /** 按 key 取值；不存在时返回 null。 */
    public String get(String key) {
        return values.get(key);
    }

    /** 按 key 取值；不存在或空白时返回默认值。 */
    public String get(String key, String defaultValue) {
        String v = values.get(key);
        return v == null || v.isBlank() ? defaultValue : v;
    }

    /** 解析布尔配置；无法识别时返回默认值。 */
    public boolean getBoolean(String key, boolean defaultValue) {
        String value = values.get(key);
        if (value == null || value.isBlank()) {
            return defaultValue;
        }
        String normalized = value.trim().toLowerCase(Locale.ROOT);
        if ("true".equals(normalized) || "1".equals(normalized) || "yes".equals(normalized) || "on".equals(normalized)) {
            return true;
        }
        if ("false".equals(normalized) || "0".equals(normalized) || "no".equals(normalized) || "off".equals(normalized)) {
            return false;
        }
        return defaultValue;
    }

    /** 解析 int 配置；无法解析时返回默认值。 */
    public int getInt(String key, int defaultValue) {
        String value = values.get(key);
        if (value == null || value.isBlank()) {
            return defaultValue;
        }
        try {
            return Integer.parseInt(value.trim());
        } catch (NumberFormatException ex) {
            return defaultValue;
        }
    }

    /** 解析 long 配置；无法解析时返回默认值。 */
    public long getLong(String key, long defaultValue) {
        String value = values.get(key);
        if (value == null || value.isBlank()) {
            return defaultValue;
        }
        try {
            return Long.parseLong(value.trim());
        } catch (NumberFormatException ex) {
            return defaultValue;
        }
    }

    /** 必填配置；缺失或空白时抛出 {@link IllegalStateException}。 */
    public String require(String key) {
        String value = get(key);
        if (value == null || value.isBlank()) {
            throw new IllegalStateException("Missing config: " + key);
        }
        return value.trim();
    }
}
