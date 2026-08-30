package github.jiangbyte.io.common.core.sensitive;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Author: Charlie
 */
class SensitiveKeysTest {

    @Test
    void matchesExactAndSuffixButNotPasswordExpiredOrBareLoginToken() {
        var keys = SensitiveKeys.DEFAULT;

        assertTrue(SensitiveKeys.matches("password", keys));
        assertTrue(SensitiveKeys.matches("user_password", keys));
        assertTrue(SensitiveKeys.matches("accessToken", keys));
        assertTrue(SensitiveKeys.matches("api_token", keys));

        assertFalse(SensitiveKeys.matches("token", keys));
        assertFalse(SensitiveKeys.matches("passwordExpired", keys));
        assertFalse(SensitiveKeys.matches("password_expired", keys));
        assertFalse(SensitiveKeys.matches("passwordExpiryWarningDays", keys));
        assertFalse(SensitiveKeys.matches("accountId", keys));
    }

    @Test
    void loggingCanStillRedactBareTokenWithEndsWithRule() {
        var keys = SensitiveKeys.normalize(java.util.List.of("password", "token"));
        assertTrue(SensitiveKeys.matches("token", keys));
        assertTrue(SensitiveKeys.matches("refresh_token", keys));
        assertFalse(SensitiveKeys.matches("password_expired", keys));
    }
}
