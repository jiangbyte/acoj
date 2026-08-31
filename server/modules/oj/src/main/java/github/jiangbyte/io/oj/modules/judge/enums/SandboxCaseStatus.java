package github.jiangbyte.io.oj.modules.judge.enums;

/**
 * SparkSandbox 测例级状态（外部协议，映射到 {@link github.jiangbyte.io.oj.modules.submission.enums.OjVerdict}）。
 * <p>
 * Author: Charlie
 */
public enum SandboxCaseStatus {
    SUCCEEDED("succeeded"),
    TIME_LIMIT_EXCEEDED("time_limit_exceeded"),
    MEMORY_LIMIT_EXCEEDED("memory_limit_exceeded"),
    OUTPUT_LIMIT_EXCEEDED("output_limit_exceeded"),
    RUNTIME_ERROR("runtime_error"),
    SECURITY_VIOLATION("security_violation"),
    INTERNAL_ERROR("internal_error"),
    COMPILE_FAILED("compile_failed");

    private final String wire;

    SandboxCaseStatus(String wire) {
        this.wire = wire;
    }

    public String wire() {
        return wire;
    }

    public static SandboxCaseStatus fromWire(String status) {
        if (status == null || status.isBlank()) {
            return null;
        }
        String key = status.trim().toLowerCase();
        for (SandboxCaseStatus value : values()) {
            if (value.wire.equals(key)) {
                return value;
            }
        }
        return null;
    }

    public boolean matches(String status) {
        return wire.equalsIgnoreCase(status);
    }
}
