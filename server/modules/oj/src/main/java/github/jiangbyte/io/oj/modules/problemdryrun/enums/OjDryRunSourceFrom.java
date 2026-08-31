package github.jiangbyte.io.oj.modules.problemdryrun.enums;

/**
 * 试跑源码来源：已存参考答案或本次覆盖。
 * <p>
 * Author: Charlie
 */
public enum OjDryRunSourceFrom {
    STORED,
    OVERRIDE;

    public static OjDryRunSourceFrom fromCode(String code) {
        if (code == null || code.isBlank()) {
            return null;
        }
        try {
            return OjDryRunSourceFrom.valueOf(code.trim().toUpperCase());
        } catch (IllegalArgumentException ex) {
            return null;
        }
    }

    public boolean matches(String code) {
        return name().equalsIgnoreCase(code);
    }
}
