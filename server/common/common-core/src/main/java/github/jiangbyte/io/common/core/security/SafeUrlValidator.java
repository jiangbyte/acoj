package github.jiangbyte.io.common.core.security;

import java.net.InetAddress;
import java.net.URI;
import java.net.UnknownHostException;
import java.util.Locale;
import java.util.Set;

/**
 * 校验出站 HTTP(S) URL，缓解 SSRF（禁止私网/元数据地址等）。
 *
 * Author: Charlie
 */
public final class SafeUrlValidator {

    private static final Set<String> BLOCKED_HOSTS = Set.of(
            "localhost",
            "metadata.google.internal",
            "metadata");

    private SafeUrlValidator() {
    }

    /** 默认仅允许 https。 */
    public static void validate(String rawUrl) {
        validate(rawUrl, false);
    }

    /**
     * @param allowHttp true 时允许 http（默认仅 https）
     */
    public static void validate(String rawUrl, boolean allowHttp) {
        if (rawUrl == null || rawUrl.isBlank()) {
            throw new IllegalArgumentException("empty url");
        }
        URI uri;
        try {
            uri = URI.create(rawUrl.trim());
        } catch (IllegalArgumentException ex) {
            throw new IllegalArgumentException("invalid url", ex);
        }
        String scheme = uri.getScheme() == null ? "" : uri.getScheme().toLowerCase(Locale.ROOT);
        if ("https".equals(scheme)) {
            // ok
        } else if ("http".equals(scheme)) {
            if (!allowHttp) {
                throw new IllegalArgumentException("http scheme not allowed");
            }
        } else {
            throw new IllegalArgumentException("scheme not allowed: " + uri.getScheme());
        }
        if (uri.getUserInfo() != null && !uri.getUserInfo().isEmpty()) {
            throw new IllegalArgumentException("url userinfo not allowed");
        }
        String host = uri.getHost();
        if (host == null || host.isBlank()) {
            throw new IllegalArgumentException("missing host");
        }
        String hostLower = host.toLowerCase(Locale.ROOT);
        if (BLOCKED_HOSTS.contains(hostLower)) {
            throw new IllegalArgumentException("blocked host: " + host);
        }
        InetAddress[] addresses;
        try {
            addresses = InetAddress.getAllByName(host);
        } catch (UnknownHostException ex) {
            throw new IllegalArgumentException("dns lookup failed: " + host, ex);
        }
        if (addresses == null || addresses.length == 0) {
            throw new IllegalArgumentException("dns lookup returned no addresses");
        }
        for (InetAddress address : addresses) {
            if (isBlocked(address)) {
                throw new IllegalArgumentException("blocked address: " + address.getHostAddress());
            }
        }
    }

    static boolean isBlocked(InetAddress address) {
        if (address == null) {
            return true;
        }
        if (address.isAnyLocalAddress()
                || address.isLoopbackAddress()
                || address.isLinkLocalAddress()
                || address.isMulticastAddress()
                || address.isSiteLocalAddress()) {
            return true;
        }
        byte[] raw = address.getAddress();
        if (raw.length == 4) {
            int b0 = raw[0] & 0xff;
            int b1 = raw[1] & 0xff;
            int b2 = raw[2] & 0xff;
            // 100.64.0.0/10 CGNAT
            if (b0 == 100 && b1 >= 64 && b1 <= 127) {
                return true;
            }
            // 192.0.0.0/24
            if (b0 == 192 && b1 == 0 && b2 == 0) {
                return true;
            }
            // TEST-NET
            if (b0 == 192 && b1 == 0 && b2 == 2) {
                return true;
            }
            if (b0 == 198 && (b1 == 18 || b1 == 19)) {
                return true;
            }
            if (b0 == 198 && b1 == 51 && b2 == 100) {
                return true;
            }
            if (b0 == 203 && b1 == 0 && b2 == 113) {
                return true;
            }
        }
        if (raw.length == 16) {
            // IPv6 ULA fc00::/7
            if ((raw[0] & 0xfe) == 0xfc) {
                return true;
            }
        }
        return false;
    }
}
