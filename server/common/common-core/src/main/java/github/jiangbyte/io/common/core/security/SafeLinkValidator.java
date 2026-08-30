package github.jiangbyte.io.common.core.security;

import java.net.URI;
import java.util.Locale;
import java.util.regex.Pattern;

/**
 * 校验面向浏览器的链接（Banner 等），拒绝危险 scheme。
 *
 * Author: Charlie
 */
public final class SafeLinkValidator {

    private static final Pattern DANGEROUS_SCHEME =
            Pattern.compile("(?i)^\\s*(javascript|data|vbscript|blob)\\s*:");

    private SafeLinkValidator() {
    }

    public static void validateBannerLink(String linkType, String rawUrl) {
        String lt = linkType == null ? "" : linkType.trim().toUpperCase(Locale.ROOT);
        if (lt.isEmpty()) {
            lt = "URL";
        }
        String u = rawUrl == null ? "" : rawUrl.trim();
        switch (lt) {
            case "NONE" -> {
                // ok
            }
            case "ROUTE" -> {
                if (u.isEmpty()) {
                    throw new IllegalArgumentException("route link requires path");
                }
                validateRelativePath(u);
            }
            case "URL" -> {
                if (!u.isEmpty()) {
                    validatePublicHref(u);
                }
            }
            default -> throw new IllegalArgumentException("unsupported link_type: " + linkType);
        }
    }

    public static void validatePublicHref(String raw) {
        String u = raw == null ? "" : raw.trim();
        if (u.isEmpty()) {
            throw new IllegalArgumentException("empty url");
        }
        if (DANGEROUS_SCHEME.matcher(u).find()) {
            throw new IllegalArgumentException("dangerous url scheme");
        }
        if (u.startsWith("/")) {
            validateRelativePath(u);
            return;
        }
        URI uri;
        try {
            uri = URI.create(u);
        } catch (IllegalArgumentException ex) {
            throw new IllegalArgumentException("invalid url", ex);
        }
        String scheme = uri.getScheme() == null ? "" : uri.getScheme().toLowerCase(Locale.ROOT);
        if (!"http".equals(scheme) && !"https".equals(scheme)) {
            throw new IllegalArgumentException("scheme not allowed: " + uri.getScheme());
        }
        if (uri.getHost() == null || uri.getHost().isBlank()) {
            throw new IllegalArgumentException("missing host");
        }
    }

    private static void validateRelativePath(String u) {
        if (!u.startsWith("/")) {
            throw new IllegalArgumentException("path must start with /");
        }
        if (u.startsWith("//")) {
            throw new IllegalArgumentException("protocol-relative url not allowed");
        }
        if (DANGEROUS_SCHEME.matcher(u).find()) {
            throw new IllegalArgumentException("dangerous url scheme");
        }
        if (u.indexOf('\r') >= 0 || u.indexOf('\n') >= 0 || u.indexOf('\0') >= 0) {
            throw new IllegalArgumentException("invalid path characters");
        }
    }
}
