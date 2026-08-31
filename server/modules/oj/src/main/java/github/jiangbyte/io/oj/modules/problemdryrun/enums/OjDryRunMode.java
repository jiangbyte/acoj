package github.jiangbyte.io.oj.modules.problemdryrun.enums;

/**
 * 试跑范围：单测例或全量。
 * <p>
 * Author: Charlie
 */
public enum OjDryRunMode {
    SINGLE,
    ALL;

    public static OjDryRunMode fromCode(String code) {
        if (code == null || code.isBlank()) {
            return null;
        }
        try {
            return OjDryRunMode.valueOf(code.trim().toUpperCase());
        } catch (IllegalArgumentException ex) {
            return null;
        }
    }

    public boolean matches(String code) {
        return name().equalsIgnoreCase(code);
    }
}
