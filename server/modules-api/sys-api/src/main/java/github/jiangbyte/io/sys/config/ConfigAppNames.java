package github.jiangbyte.io.sys.config;

import org.springframework.util.StringUtils;

/**
 * 从系统配置解析应用展示名称。
 *
 * Author: Charlie
 */
public final class ConfigAppNames {

    private ConfigAppNames() {
    }

    /** 优先 APP_NAME，其次 COPYRIGHT_TEXT，最后默认 HEI。 */
    public static String resolve(ConfigApi config) {
        if (config == null) {
            return "HEI";
        }
        String name = nullToEmpty(config.getValue("APP_NAME", "")).trim();
        if (StringUtils.hasText(name)) {
            return name;
        }
        name = nullToEmpty(config.getValue("COPYRIGHT_TEXT", "")).trim();
        return StringUtils.hasText(name) ? name : "HEI";
    }

    private static String nullToEmpty(String value) {
        return value == null ? "" : value;
    }
}
