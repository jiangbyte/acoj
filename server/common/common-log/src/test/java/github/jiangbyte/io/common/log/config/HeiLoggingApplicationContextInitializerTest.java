package github.jiangbyte.io.common.log.config;

/**
 * Author: Charlie
 **/

import github.jiangbyte.io.common.log.format.HeiConsoleKeyValueStructuredLogFormatter;
import github.jiangbyte.io.common.log.format.HeiKeyValueStructuredLogFormatter;
import org.junit.jupiter.api.Test;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.mock.env.MockEnvironment;

import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class HeiLoggingApplicationContextInitializerTest {

    @Test
    void defaultsToJsonWithoutOverridingFormat() {
        GenericApplicationContext context = new GenericApplicationContext();
        context.setEnvironment(new MockEnvironment());
        new HeiLoggingApplicationContextInitializer().initialize(context);
        MockEnvironment env = (MockEnvironment) context.getEnvironment();
        assertNull(env.getProperty("logging.structured.format.console"));
        assertNull(env.getProperty("logging.structured.format.file"));
        assertTrue(HeiLoggingApplicationContextInitializer.resolveJson(env));
    }

    @Test
    void keyValueWhenJsonDisabled() {
        GenericApplicationContext context = new GenericApplicationContext();
        MockEnvironment env = new MockEnvironment();
        env.setProperty("hei.logging.json", "false");
        context.setEnvironment(env);
        new HeiLoggingApplicationContextInitializer().initialize(context);
        assertEquals(
                HeiConsoleKeyValueStructuredLogFormatter.class.getName(),
                env.getProperty("logging.structured.format.console"));
        assertEquals(
                HeiKeyValueStructuredLogFormatter.class.getName(),
                env.getProperty("logging.structured.format.file"));
    }
}
