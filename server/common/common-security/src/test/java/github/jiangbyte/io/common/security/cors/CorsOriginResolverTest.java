package github.jiangbyte.io.common.security.cors;

/**
 * Author: Charlie
 **/

import github.jiangbyte.io.common.security.config.HeiSecurityProperties;
import org.junit.jupiter.api.Test;
import org.springframework.mock.env.MockEnvironment;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CorsOriginResolverTest {

    @Test
    void defaultsIncludePortalAndAdminPorts() {
        HeiSecurityProperties props = new HeiSecurityProperties();
        MockEnvironment env = new MockEnvironment();
        List<String> origins = CorsOriginResolver.resolve(props, env);
        assertTrue(origins.contains("http://localhost:5173"));
        assertTrue(origins.contains("http://localhost:5174"));
        assertTrue(origins.contains("http://127.0.0.1:5163"));
    }

    @Test
    void appendsFrontendBaseUrl() {
        HeiSecurityProperties props = new HeiSecurityProperties();
        MockEnvironment env = new MockEnvironment()
                .withProperty("hei.app.frontend-base-url", "https://admin.example.com");
        List<String> origins = CorsOriginResolver.resolve(props, env);
        assertTrue(origins.contains("https://admin.example.com"));
        assertTrue(origins.contains("http://localhost:5174"));
    }

    @Test
    void configuredListWinsButStillAppendsFrontend() {
        HeiSecurityProperties props = new HeiSecurityProperties();
        props.setCorsAllowedOrigins(List.of("https://a.example.com"));
        MockEnvironment env = new MockEnvironment()
                .withProperty("hei.app.frontend-base-url", "https://b.example.com");
        List<String> origins = CorsOriginResolver.resolve(props, env);
        assertTrue(origins.contains("https://a.example.com"));
        assertTrue(origins.contains("https://b.example.com"));
        assertFalse(origins.contains("http://localhost:5173"));
    }

    @Test
    void starAllowsAnyAndDisablesCredentialMode() {
        assertTrue(CorsOriginResolver.allowsAny(List.of("*")));
        assertTrue(CorsOriginResolver.isAllowed("http://evil.example", List.of("*")));
        assertFalse(CorsOriginResolver.allowsAny(List.of("http://localhost:5173")));
    }

    @Test
    void exactAndPrefixPattern() {
        List<String> allowed = List.of("http://localhost:5173", "http://127.0.0.1:*");
        assertTrue(CorsOriginResolver.isAllowed("http://localhost:5173", allowed));
        assertTrue(CorsOriginResolver.isAllowed("http://127.0.0.1:9999", allowed));
        assertFalse(CorsOriginResolver.isAllowed("http://localhost:5174", allowed));
    }
}
