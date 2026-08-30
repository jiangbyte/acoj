package github.jiangbyte.io.common.log.format;

/**
 * Author: Charlie
 **/

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.spi.LoggingEvent;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ansi.AnsiColor;
import org.springframework.boot.ansi.AnsiOutput;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class HeiKeyValueStructuredLogFormatterTest {

    @Test
    void formatsKeyValuesAndOmitsEmptyMdc() {
        LoggerContext context = (LoggerContext) LoggerFactory.getILoggerFactory();
        Logger logger = context.getLogger("access");
        LoggingEvent event = new LoggingEvent("fqcn", logger, Level.INFO, "http.access", null, null);
        Map<String, String> mdc = new HashMap<>();
        mdc.put("request_id", "abc");
        mdc.put("path", "/api/v1/x");
        mdc.put("empty", "");
        mdc.put("dash", "-");
        event.setMDCPropertyMap(mdc);

        String line = new HeiKeyValueStructuredLogFormatter().format(event);

        assertTrue(line.contains("http.access"));
        assertTrue(line.contains("request_id=abc"));
        assertTrue(line.contains("path=/api/v1/x"));
        assertFalse(line.contains("empty="));
        assertFalse(line.contains("dash=-"));
        assertTrue(line.contains("[info]"));
    }

    @Test
    void consoleFormatterColorsLevelWhenAnsiEnabled() {
        AnsiOutput.setEnabled(AnsiOutput.Enabled.ALWAYS);
        try {
            LoggerContext context = (LoggerContext) LoggerFactory.getILoggerFactory();
            Logger logger = context.getLogger("access");
            LoggingEvent event = new LoggingEvent("fqcn", logger, Level.INFO, "hello", null, null);
            String line = new HeiConsoleKeyValueStructuredLogFormatter().format(event);
            assertTrue(line.contains(AnsiOutput.toString(AnsiColor.GREEN, "info")));
        } finally {
            AnsiOutput.setEnabled(AnsiOutput.Enabled.DETECT);
        }
    }
}
