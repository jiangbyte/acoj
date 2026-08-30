package github.jiangbyte.io.common.core.security;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Author: Charlie
 */
class SafeUrlValidatorTest {

    @Test
    void rejectsPrivateAndDangerous() {
        assertThrows(IllegalArgumentException.class, () -> SafeUrlValidator.validate("http://127.0.0.1/x"));
        assertThrows(IllegalArgumentException.class, () -> SafeUrlValidator.validate("https://127.0.0.1/x"));
        assertThrows(IllegalArgumentException.class, () -> SafeUrlValidator.validate("http://169.254.169.254/latest"));
        assertThrows(IllegalArgumentException.class, () -> SafeUrlValidator.validate("file:///etc/passwd"));
        assertThrows(IllegalArgumentException.class, () -> SafeUrlValidator.validate("https://user:pass@example.com/"));
        assertThrows(IllegalArgumentException.class, () -> SafeUrlValidator.validate(""));
        assertThrows(IllegalArgumentException.class, () -> SafeUrlValidator.validate("http://example.com/hook", false));
    }

    @Test
    void allowsPublicHttps() {
        assertDoesNotThrow(() -> SafeUrlValidator.validate("https://example.com/webhook"));
    }

    @Test
    void allowsHttpWhenEnabled() {
        assertDoesNotThrow(() -> SafeUrlValidator.validate("http://example.com/webhook", true));
    }
}
