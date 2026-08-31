package github.jiangbyte.io.oj.modules.judge.enums;

/**
 * 派发审计 outcome。
 * <p>
 * Author: Charlie
 */
public enum OjDispatchOutcome {
    STARTED,
    SUCCESS_RESULT,
    TRANSPORT_FAIL,
    SANDBOX_INTERNAL,
    CANCELLED_LEASE,
    TIMEOUT;

    public static OjDispatchOutcome fromCode(String code) {
        if (code == null || code.isBlank()) {
            return null;
        }
        try {
            return OjDispatchOutcome.valueOf(code.trim().toUpperCase());
        } catch (IllegalArgumentException ex) {
            return null;
        }
    }

    public boolean matches(String code) {
        return name().equalsIgnoreCase(code);
    }
}
