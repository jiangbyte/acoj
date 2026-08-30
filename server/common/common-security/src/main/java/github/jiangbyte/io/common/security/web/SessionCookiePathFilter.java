package github.jiangbyte.io.common.security.web;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * 会话 Cookie Path 过滤器：包装响应以改写 Set-Cookie 的 Path。
 *
 * Author: Charlie
 */
public class SessionCookiePathFilter extends OncePerRequestFilter {

    private final boolean cookieAuthEnabled;
    private final String cookieName;

    public SessionCookiePathFilter(boolean cookieAuthEnabled, String cookieName) {
        this.cookieAuthEnabled = cookieAuthEnabled;
        this.cookieName = cookieName == null || cookieName.isBlank() ? "Authorization" : cookieName;
    }

    /** 包装响应以改写会话 Cookie Path。 */
    @Override
    protected void doFilterInternal(
            HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        if (!cookieAuthEnabled) {
            filterChain.doFilter(request, response);
            return;
        }
        String clientPath = SessionCookiePaths.fromRequestPath(request.getRequestURI());
        SessionCookiePathResponseWrapper wrapped =
                new SessionCookiePathResponseWrapper(response, cookieName, clientPath);
        filterChain.doFilter(request, wrapped);
    }
}
