package github.jiangbyte.io.common.core.jackson;

/**
 * 按 {@link Sensitive} 配置对字符串脱敏（纯函数，供 Jackson / 日志等复用）。
 *
 * Author: Charlie
 */
public final class SensitiveMasker {

    private SensitiveMasker() {
    }

    public static String mask(String raw, Sensitive sensitive) {
        if (raw == null) {
            return null;
        }
        if (sensitive == null) {
            return "***";
        }
        return switch (sensitive.strategy()) {
            case ALL -> sensitive.mask() == null || sensitive.mask().isEmpty() ? "***" : sensitive.mask();
            case RANGE -> maskRange(raw, sensitive.from(), sensitive.to(), sensitive.maskChar());
            case KEEP -> maskKeep(raw, sensitive.keepPrefix(), sensitive.keepSuffix(), sensitive.maskChar());
        };
    }

    /**
     * 区间脱敏，语义同 {@link String#substring(int, int)}：含 {@code from}、不含 {@code to}；
     * {@code to < 0} 视为 {@code raw.length()}。越界自动夹紧。
     */
    public static String maskRange(String raw, int from, int to, char maskChar) {
        if (raw.isEmpty()) {
            return raw;
        }
        int len = raw.length();
        int start = Math.max(0, Math.min(from, len));
        int end = to < 0 ? len : Math.max(0, Math.min(to, len));
        if (start >= end) {
            return raw;
        }
        StringBuilder sb = new StringBuilder(len);
        sb.append(raw, 0, start);
        sb.append(String.valueOf(maskChar).repeat(end - start));
        sb.append(raw, end, len);
        return sb.toString();
    }

    public static String maskKeep(String raw, int keepPrefix, int keepSuffix, char maskChar) {
        if (raw.isEmpty()) {
            return raw;
        }
        int len = raw.length();
        int prefix = Math.max(0, keepPrefix);
        int suffix = Math.max(0, keepSuffix);
        if (prefix + suffix >= len) {
            return raw;
        }
        int maskLen = len - prefix - suffix;
        return raw.substring(0, prefix)
                + String.valueOf(maskChar).repeat(maskLen)
                + raw.substring(len - suffix);
    }
}
