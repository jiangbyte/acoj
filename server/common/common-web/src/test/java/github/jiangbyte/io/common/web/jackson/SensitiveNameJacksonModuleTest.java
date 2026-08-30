package github.jiangbyte.io.common.web.jackson;

import github.jiangbyte.io.common.core.jackson.Sensitive;
import github.jiangbyte.io.common.core.jackson.SensitiveStrategy;
import lombok.Data;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.json.JsonMapper;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Author: Charlie
 */
class SensitiveNameJacksonModuleTest {

    private JsonMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = JsonMapper.builder().addModule(new SensitiveNameJacksonModule()).build();
    }

    @Test
    void masksOnlyAnnotatedProperties() {
        Sample sample = new Sample();
        sample.token = "sess-plain";
        sample.passwordExpired = true;
        sample.secret = "top-secret";
        sample.phone = "13812345678";
        sample.idCard = "110101199001011234";
        sample.note = "ok";

        String json = mapper.writeValueAsString(sample);

        assertTrue(json.contains("\"token\":\"sess-plain\""));
        assertTrue(json.contains("\"passwordExpired\":true") || json.contains("\"password_expired\":true"));
        assertTrue(json.contains("\"secret\":\"***\""));
        assertTrue(json.contains("\"phone\":\"138****5678\""));
        assertTrue(json.contains("\"idCard\":\"110***********1234\"") || json.contains("\"id_card\":\"110***********1234\""));
        assertTrue(json.contains("\"note\":\"ok\""));
        assertFalse(json.contains("top-secret"));
        assertFalse(json.contains("13812345678"));
    }

    @Data
    static class Sample {
        private String token;
        private boolean passwordExpired;
        @Sensitive
        private String secret;
        @Sensitive(strategy = SensitiveStrategy.KEEP, keepPrefix = 3, keepSuffix = 4)
        private String phone;
        @Sensitive(strategy = SensitiveStrategy.RANGE, from = 3, to = 14)
        private String idCard;
        private String note;
    }
}
