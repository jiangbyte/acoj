package github.jiangbyte.io.common.security.web;

/**
 * Author: Charlie
 **/

import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletResponse;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;

class SessionCookiePathResponseWrapperTest {

    @Test
    void rewritesAddCookiePathAndClearsLegacy() {
        MockHttpServletResponse response = new MockHttpServletResponse();
        SessionCookiePathResponseWrapper wrapped =
                new SessionCookiePathResponseWrapper(response, "Authorization", "/api/v1/admin");

        Cookie cookie = new Cookie("Authorization", "tok-1");
        cookie.setPath("/");
        cookie.setHttpOnly(true);
        wrapped.addCookie(cookie);

        boolean pathOk = Arrays.stream(response.getCookies())
                .anyMatch(c -> "Authorization".equals(c.getName())
                        && "tok-1".equals(c.getValue())
                        && "/api/v1/admin".equals(c.getPath()));
        assertTrue(pathOk);
        List<String> setCookies = response.getHeaders("Set-Cookie");
        assertTrue(setCookies.stream().anyMatch(h -> h.contains("Authorization=") && h.contains("Path=/") && h.contains("Max-Age=0")));
        assertTrue(setCookies.stream().anyMatch(h -> h.contains("hei_session=") && h.contains("Max-Age=0")));
    }

    @Test
    void rewritesSetCookieHeader() {
        MockHttpServletResponse response = new MockHttpServletResponse();
        SessionCookiePathResponseWrapper wrapped =
                new SessionCookiePathResponseWrapper(response, "Authorization", "/api/v1/portal");

        wrapped.addHeader("Set-Cookie", "Authorization=abc; Path=/; HttpOnly; SameSite=Lax");

        List<String> setCookies = response.getHeaders("Set-Cookie");
        assertTrue(setCookies.stream().anyMatch(h -> h.contains("Authorization=abc") && h.contains("Path=/api/v1/portal")));
        assertTrue(setCookies.stream().noneMatch(h -> h.contains("Authorization=abc") && h.matches(".*Path=/([;].*)?$")));
    }
}
