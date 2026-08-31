package github.jiangbyte.io.oj.modules.problemcase.enums;

/**
 * 测例输入/输出存储方式。
 * <p>
 * Author: Charlie
 */
public enum OjCaseStorage {
    INLINE,
    OBJECT;

    public static OjCaseStorage fromCode(String code) {
        if (code == null || code.isBlank()) {
            return null;
        }
        try {
            return OjCaseStorage.valueOf(code.trim().toUpperCase());
        } catch (IllegalArgumentException ex) {
            return null;
        }
    }

    public boolean matches(String code) {
        return name().equalsIgnoreCase(code);
    }
}
