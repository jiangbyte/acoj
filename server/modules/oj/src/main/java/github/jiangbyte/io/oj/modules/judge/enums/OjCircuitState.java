package github.jiangbyte.io.oj.modules.judge.enums;

/**
 * 执行机熔断状态。
 * <p>
 * Author: Charlie
 */
public enum OjCircuitState {
    CLOSED,
    OPEN,
    HALF_OPEN;

    public static OjCircuitState fromCode(String code) {
        if (code == null || code.isBlank()) {
            return null;
        }
        try {
            return OjCircuitState.valueOf(code.trim().toUpperCase());
        } catch (IllegalArgumentException ex) {
            return null;
        }
    }

    public boolean matches(String code) {
        return name().equalsIgnoreCase(code);
    }
}
