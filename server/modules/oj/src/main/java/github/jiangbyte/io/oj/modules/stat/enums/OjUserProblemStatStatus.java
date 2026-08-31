package github.jiangbyte.io.oj.modules.stat.enums;

/**
 * 用户 × 题做题统计状态。
 * <p>
 * Author: Charlie
 */
public enum OjUserProblemStatStatus {
    ATTEMPTED,
    ACCEPTED;

    public static OjUserProblemStatStatus fromCode(String code) {
        if (code == null || code.isBlank()) {
            return null;
        }
        try {
            return OjUserProblemStatStatus.valueOf(code.trim().toUpperCase());
        } catch (IllegalArgumentException ex) {
            return null;
        }
    }

    public boolean matches(String code) {
        return name().equalsIgnoreCase(code);
    }
}
