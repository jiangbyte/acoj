package github.jiangbyte.io.oj.modules.submission.enums;

/**
 * OJ 提交 / 试跑可见裁决状态（含排队与判题中）。
 * <p>
 * Author: Charlie
 */
public enum OjVerdict {
    PENDING,
    JUDGING,
    AC,
    WA,
    TLE,
    MLE,
    OLE,
    RE,
    CE,
    SE;

    /**
     * 解析码；非法返回 null。
     */
    public static OjVerdict fromCode(String code) {
        if (code == null || code.isBlank()) {
            return null;
        }
        try {
            return OjVerdict.valueOf(code.trim().toUpperCase());
        } catch (IllegalArgumentException ex) {
            return null;
        }
    }

    public boolean matches(String code) {
        return name().equalsIgnoreCase(code);
    }
}
