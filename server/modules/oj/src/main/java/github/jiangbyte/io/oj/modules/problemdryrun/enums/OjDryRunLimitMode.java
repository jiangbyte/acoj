package github.jiangbyte.io.oj.modules.problemdryrun.enums;

/**
 * 试跑限额模式：题目限额或宽松摸底。
 * <p>
 * Author: Charlie
 */
public enum OjDryRunLimitMode {
    PROBLEM,
    RELAXED;

    public static OjDryRunLimitMode fromCode(String code) {
        if (code == null || code.isBlank()) {
            return null;
        }
        try {
            return OjDryRunLimitMode.valueOf(code.trim().toUpperCase());
        } catch (IllegalArgumentException ex) {
            return null;
        }
    }

    public boolean matches(String code) {
        return name().equalsIgnoreCase(code);
    }
}
