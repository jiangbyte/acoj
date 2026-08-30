package github.jiangbyte.io.common.security.web;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpMethod;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.security.SecureRandom;
import java.util.HexFormat;

/**
 * Cookie 双提交 CSRF：会话 Cookie 存在时校验 HEI_CSRF 与 X-HEI-CSRF。
 *
 * Author: Charlie
 */
public class CsrfDoubleSubmitFilter extends OncePerRequestFilter {

    public static final String CSRF_COOKIE = "HEI_CSRF";
    public static final String CSRF_HEADER = "X-HEI-CSRF";

    private final boolean cookieAuthEnabled;
    private final String sessionCookieName;

    public CsrfDoubleSubmitFilter(boolean cookieAuthEnabled, String sessionCookieName) {
        this.cookieAuthEnabled = cookieAuthEnabled;
        this.sessionCookieName = StringUtils.hasText(sessionCookieName) ? sessionCookieName : "Authorization";
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        if (!cookieAuthEnabled) {
            filterChain.doFilter(request, response);
            return;
        }
        String method = request.getMethod();
        if (HttpMethod.GET.matches(method)
                || HttpMethod.HEAD.matches(method)
                || HttpMethod.OPTIONS.matches(method)
                || HttpMethod.TRACE.matches(method)) {
            filterChain.doFilter(request, response);
            return;
        }
        String path = request.getRequestURI() == null ? "" : request.getRequestURI().toLowerCase();
        if (path.contains("/oauth/") && path.contains("/callback")) {
            filterChain.doFilter(request, response);
            return;
        }
        if (path.endsWith("/health") || path.endsWith("/ready") || path.endsWith("/actuator/health")) {
            filterChain.doFilter(request, response);
            return;
        }
        String sessionCookie = readCookie(request, sessionCookieName);
        if (!StringUtils.hasText(sessionCookie)) {
            filterChain.doFilter(request, response);
            return;
        }
        String csrfCookie = readCookie(request, CSRF_COOKIE);
        String header = request.getHeader(CSRF_HEADER);
        if (!StringUtils.hasText(csrfCookie) || !csrfCookie.equals(header)) {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write("{\"code\":403,\"message\":\"CSRF token mismatch\",\"data\":null}");
            return;
        }
        filterChain.doFilter(request, response);
    }

    public static void issueCsrfCookie(HttpServletResponse response, boolean secure, String sameSite, int maxAge) {
        String token = HexFormat.of().formatHex(randomBytes(32));
        StringBuilder header = new StringBuilder();
        header.append(CSRF_COOKIE).append('=').append(token)
                .append("; Path=/; SameSite=").append(sameSite == null || sameSite.isBlank() ? "Lax" : sameSite);
        if (secure) {
            header.append("; Secure");
        }
        if (maxAge > 0) {
            header.append("; Max-Age=").append(maxAge);
        }
        response.addHeader("Set-Cookie", header.toString());
    }

    public static void clearCsrfCookie(HttpServletResponse response) {
        Cookie cookie = new Cookie(CSRF_COOKIE, "");
        cookie.setPath("/");
        cookie.setMaxAge(0);
        cookie.setHttpOnly(false);
        response.addCookie(cookie);
    }

    private static String readCookie(HttpServletRequest request, String name) {
        Cookie[] cookies = request.getCookies();
        if (cookies == null) {
            return null;
        }
        for (Cookie cookie : cookies) {
            if (name.equals(cookie.getName())) {
                return cookie.getValue();
            }
        }
        return null;
    }

    private static byte[] randomBytes(int n) {
        byte[] buf = new byte[n];
        new SecureRandom().nextBytes(buf);
        return buf;
    }
}
