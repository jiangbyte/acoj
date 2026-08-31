package github.jiangbyte.io.oj.modules.problem.enums;

/**
 * 题目难度。
 * <p>
 * Author: Charlie
 */
public enum OjDifficulty {
    EASY,
    MEDIUM,
    HARD;

    public static OjDifficulty fromCode(String code) {
        if (code == null || code.isBlank()) {
            return null;
        }
        try {
            return OjDifficulty.valueOf(code.trim().toUpperCase());
        } catch (IllegalArgumentException ex) {
            return null;
        }
    }
}
