package github.jiangbyte.io.common.security.web;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpServletResponseWrapper;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * 响应包装器：将指定会话 Cookie 的 Path 规范化为约定路径。
 *
 * Author: Charlie
 */
public class SessionCookiePathResponseWrapper extends HttpServletResponseWrapper {

    private final String cookieName;
    private final String clientPath;
    private boolean legacyCleared;

    public SessionCookiePathResponseWrapper(
            HttpServletResponse response, String cookieName, String clientPath) {
        super(response);
        this.cookieName = cookieName;
        this.clientPath = clientPath;
    }

    /** 添加 Cookie，必要时改写 Path。 */
    @Override
    public void addCookie(Cookie cookie) {
        if (cookie == null) {
            return;
        }
        if (cookieName.equals(cookie.getName())) {
            Cookie rewritten = cloneCookie(cookie);
            rewritten.setPath(clientPath);
            super.addCookie(rewritten);
            clearLegacyCookies();
            return;
        }
        super.addCookie(cookie);
    }

    /** 设置响应头；对 Set-Cookie 做 Path 规范化。 */
    @Override
    public void setHeader(String name, String value) {
        if (isSetCookie(name)) {
            List<String> rewritten = rewriteSetCookieHeaders(List.of(value));
            boolean first = true;
            for (String item : rewritten) {
                if (first) {
                    super.setHeader(name, item);
                    first = false;
                } else {
                    super.addHeader(name, item);
                }
            }
            return;
        }
        super.setHeader(name, value);
    }

    /** 添加响应头；对 Set-Cookie 做 Path 规范化。 */
    @Override
    public void addHeader(String name, String value) {
        if (isSetCookie(name)) {
            for (String item : rewriteSetCookieHeaders(List.of(value))) {
                super.addHeader(name, item);
            }
            return;
        }
        super.addHeader(name, value);
    }

    private List<String> rewriteSetCookieHeaders(Collection<String> values) {
        List<String> out = new ArrayList<>();
        boolean touched = false;
        for (String raw : values) {
            if (raw == null) {
                continue;
            }
            if (isNamedCookie(raw, cookieName)) {
                out.add(replacePath(raw, clientPath));
                touched = true;
            } else {
                out.add(raw);
            }
        }
        if (touched) {
            out.addAll(legacyClearSetCookieHeaders());
            legacyCleared = true;
        }
        return out;
    }

    private void clearLegacyCookies() {
        if (legacyCleared) {
            return;
        }
        for (String header : legacyClearSetCookieHeaders()) {
            super.addHeader("Set-Cookie", header);
        }
        legacyCleared = true;
    }

    private List<String> legacyClearSetCookieHeaders() {
        List<String> headers = new ArrayList<>();
        if (!SessionCookiePaths.LEGACY_ROOT_PATH.equals(clientPath)) {
            headers.add(expireCookieHeader(cookieName, SessionCookiePaths.LEGACY_ROOT_PATH));
        }
        headers.add(expireCookieHeader(SessionCookiePaths.LEGACY_COOKIE_NAME, clientPath));
        if (!SessionCookiePaths.LEGACY_ROOT_PATH.equals(clientPath)) {
            headers.add(expireCookieHeader(
                    SessionCookiePaths.LEGACY_COOKIE_NAME, SessionCookiePaths.LEGACY_ROOT_PATH));
        }
        return headers;
    }

    private static Cookie cloneCookie(Cookie source) {
        Cookie copy = new Cookie(source.getName(), source.getValue());
        copy.setMaxAge(source.getMaxAge());
        copy.setHttpOnly(source.isHttpOnly());
        copy.setSecure(source.getSecure());
        if (source.getDomain() != null) {
            copy.setDomain(source.getDomain());
        }
        if (source.getPath() != null) {
            copy.setPath(source.getPath());
        }
        if (source.getAttribute("SameSite") != null) {
            copy.setAttribute("SameSite", source.getAttribute("SameSite"));
        }
        return copy;
    }

    private static boolean isSetCookie(String name) {
        return name != null && "Set-Cookie".equalsIgnoreCase(name);
    }

    private static boolean isNamedCookie(String setCookie, String name) {
        String trimmed = setCookie.trim();
        return trimmed.regionMatches(true, 0, name + "=", 0, name.length() + 1);
    }

    private static String replacePath(String setCookie, String path) {
        String withoutPath = setCookie.replaceAll("(?i);\\s*Path=[^;]*", "");
        if (withoutPath.endsWith(";")) {
            withoutPath = withoutPath.substring(0, withoutPath.length() - 1);
        }
        return withoutPath + "; Path=" + path;
    }

    private static String expireCookieHeader(String name, String path) {
        return name + "=; Max-Age=0; Path=" + path + "; HttpOnly";
    }
}
