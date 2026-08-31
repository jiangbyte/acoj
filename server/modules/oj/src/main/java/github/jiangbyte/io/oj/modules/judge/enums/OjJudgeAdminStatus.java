package github.jiangbyte.io.oj.modules.judge.enums;

/**
 * 执行机管理态。
 * <p>
 * Author: Charlie
 */
public enum OjJudgeAdminStatus {
    ENABLED,
    DISABLED,
    DRAINING;

    public static OjJudgeAdminStatus fromCode(String code) {
        if (code == null || code.isBlank()) {
            return null;
        }
        try {
            return OjJudgeAdminStatus.valueOf(code.trim().toUpperCase());
        } catch (IllegalArgumentException ex) {
            return null;
        }
    }

    public boolean matches(String code) {
        return name().equalsIgnoreCase(code);
    }
}
