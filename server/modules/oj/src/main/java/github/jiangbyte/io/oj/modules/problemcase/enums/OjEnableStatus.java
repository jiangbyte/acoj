package github.jiangbyte.io.oj.modules.problemcase.enums;

/**
 * 通用启用/停用状态（测例、参考答案、标签等复用）。
 * <p>
 * Author: Charlie
 */
public enum OjEnableStatus {
    ENABLED,
    DISABLED;

    public static OjEnableStatus fromCode(String code) {
        if (code == null || code.isBlank()) {
            return null;
        }
        try {
            return OjEnableStatus.valueOf(code.trim().toUpperCase());
        } catch (IllegalArgumentException ex) {
            return null;
        }
    }

    public boolean matches(String code) {
        return name().equalsIgnoreCase(code);
    }
}
