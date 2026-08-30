package github.jiangbyte.io.common.web.jackson;

/** Author: Charlie **/

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.json.JsonMapper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class StringlyTypedJacksonModuleTest {

    private JsonMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = JsonMapper.builder().addModule(new StringlyTypedJacksonModule()).build();
    }

    @Test
    void serializesScalarsAsJsonStrings() throws Exception {
        Sample sample = new Sample();
        sample.code = 200;
        sample.enabled = true;
        sample.count = 12L;
        String json = mapper.writeValueAsString(sample);
        assertTrue(json.contains("\"code\":\"200\""));
        assertTrue(json.contains("\"enabled\":\"true\""));
        assertTrue(json.contains("\"count\":\"12\""));
        assertFalse(json.contains("\"code\":200"));
        assertFalse(json.contains("\"enabled\":true"));
    }

    @Test
    void deserializesScalarsFromJsonStrings() throws Exception {
        Sample sample = mapper.readValue(
                "{\"code\":\"401\",\"enabled\":\"false\",\"count\":\"99\"}", Sample.class);
        assertEquals(401, sample.code);
        assertFalse(sample.enabled);
        assertEquals(99L, sample.count);
    }

    @Test
    void deserializesScalarsFromNativeJsonTokens() throws Exception {
        Sample sample = mapper.readValue(
                "{\"code\":200,\"enabled\":true,\"count\":12}", Sample.class);
        assertEquals(200, sample.code);
        assertTrue(sample.enabled);
        assertEquals(12L, sample.count);
    }

    static class Sample {
        public int code;
        public boolean enabled;
        public long count;
    }
}
