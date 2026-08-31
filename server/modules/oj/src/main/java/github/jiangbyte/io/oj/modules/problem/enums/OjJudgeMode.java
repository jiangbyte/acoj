package github.jiangbyte.io.oj.modules.problem.enums;

/**
 * 判题模式；P0 固定 STANDARD。
 * <p>
 * Author: Charlie
 */
public enum OjJudgeMode {
    STANDARD;

    public static OjJudgeMode fromCode(String code) {
        if (code == null || code.isBlank()) {
            return null;
        }
        try {
            return OjJudgeMode.valueOf(code.trim().toUpperCase());
        } catch (IllegalArgumentException ex) {
            return null;
        }
    }

    public boolean matches(String code) {
        return name().equalsIgnoreCase(code);
    }
}
