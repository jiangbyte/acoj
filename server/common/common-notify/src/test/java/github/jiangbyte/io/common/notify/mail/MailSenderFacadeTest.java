package github.jiangbyte.io.common.notify.mail;

/**
 * Author: Charlie
 **/

import github.jiangbyte.io.common.core.exception.BizException;
import github.jiangbyte.io.common.notify.NotifyConfigSource;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MailSenderFacadeTest {

    @Test
    void aliyunRequiresAccessKey() {
        Map<String, String> values = new HashMap<>();
        values.put("DEFAULT_EMAIL_ENGINE", "ALIYUN");
        MailSenderFacade facade = new MailSenderFacade(mapSource(values));
        BizException ex = assertThrows(BizException.class, () -> facade.send("a@b.com", "s", "b"));
        assertTrue(ex.getMessage().contains("MAIL_ALIYUN_ACCESS_KEY_ID"));
    }

    private static NotifyConfigSource mapSource(Map<String, String> values) {
        return new NotifyConfigSource() {
            @Override
            public String get(String key) {
                return values.get(key);
            }

            @Override
            public String get(String key, String def) {
                String v = values.get(key);
                return v == null || v.isBlank() ? def : v;
            }

            @Override
            public boolean getBoolean(String key, boolean def) {
                String v = values.get(key);
                if (v == null || v.isBlank()) {
                    return def;
                }
                return "true".equalsIgnoreCase(v.trim()) || "1".equals(v.trim());
            }

            @Override
            public int getInt(String key, int def) {
                String v = values.get(key);
                if (v == null || v.isBlank()) {
                    return def;
                }
                return Integer.parseInt(v.trim());
            }
        };
    }
}
