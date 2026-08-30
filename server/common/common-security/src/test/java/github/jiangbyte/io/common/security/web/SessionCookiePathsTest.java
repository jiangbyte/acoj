package github.jiangbyte.io.common.security.web;

/**
 * Author: Charlie
 **/

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class SessionCookiePathsTest {

    @Test
    void clientRootFromLoginAndNestedPaths() {
        assertEquals("/api/v1/admin", SessionCookiePaths.fromRequestPath("/api/v1/admin/login"));
        assertEquals("/api/v1/admin", SessionCookiePaths.fromRequestPath("/api/v1/admin/auth/refresh"));
        assertEquals("/api/v1/portal", SessionCookiePaths.fromRequestPath("/api/v1/portal/logout"));
        assertEquals("/api/v2/portal", SessionCookiePaths.fromRequestPath("/api/v2/portal/me"));
        assertEquals("/api/v1/admin", SessionCookiePaths.fromRequestPath("/api/v1/admin"));
    }

    @Test
    void rootFallback() {
        assertEquals("/", SessionCookiePaths.fromRequestPath(""));
        assertEquals("/", SessionCookiePaths.fromRequestPath("/"));
    }
}
