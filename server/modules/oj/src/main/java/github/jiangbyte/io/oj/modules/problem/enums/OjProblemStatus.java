package github.jiangbyte.io.oj.modules.problem.enums;

/**
 * 题目发布状态。
 * <p>
 * Author: Charlie
 */
public enum OjProblemStatus {
    DRAFT,
    PUBLISHED,
    DISABLED;

    public static OjProblemStatus fromCode(String code) {
        if (code == null || code.isBlank()) {
            return null;
        }
        try {
            return OjProblemStatus.valueOf(code.trim().toUpperCase());
        } catch (IllegalArgumentException ex) {
            return null;
        }
    }

    public boolean matches(String code) {
        return name().equalsIgnoreCase(code);
    }
}
