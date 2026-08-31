package github.jiangbyte.io.oj.modules.judge.enums;

/**
 * 执行机运行态。
 * <p>
 * Author: Charlie
 */
public enum OjJudgeRuntimeStatus {
    ONLINE,
    OFFLINE,
    UNHEALTHY;

    public static OjJudgeRuntimeStatus fromCode(String code) {
        if (code == null || code.isBlank()) {
            return null;
        }
        try {
            return OjJudgeRuntimeStatus.valueOf(code.trim().toUpperCase());
        } catch (IllegalArgumentException ex) {
            return null;
        }
    }

    public boolean matches(String code) {
        return name().equalsIgnoreCase(code);
    }
}
