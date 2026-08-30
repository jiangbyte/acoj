package github.jiangbyte.io.common.web.filter;

/**
 * Author: Charlie
 **/

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import github.jiangbyte.io.common.web.log.RequestLogMdc;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;

class AccessLogFilterTest {

    @AfterEach
    void detach() {
        Logger logger = (Logger) LoggerFactory.getLogger(AccessLogFilter.LOGGER_NAME);
        logger.detachAndStopAllAppenders();
        MDC.clear();
    }

    @Test
    void emitsHttpAccessWithStatusAndDurationInMdc() throws Exception {
        Logger logger = (Logger) LoggerFactory.getLogger(AccessLogFilter.LOGGER_NAME);
        ListAppender<ILoggingEvent> appender = new ListAppender<>();
        appender.start();
        logger.addAppender(appender);
        logger.setLevel(ch.qos.logback.classic.Level.INFO);

        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/health");
        MockHttpServletResponse response = new MockHttpServletResponse();

        new AccessLogFilter().doFilter(request, response, (req, res) ->
                ((MockHttpServletResponse) res).setStatus(204));

        assertFalse(appender.list.isEmpty());
        ILoggingEvent event = appender.list.getLast();
        assertEquals("http.access", event.getFormattedMessage());
        assertEquals("204", event.getMDCPropertyMap().get(RequestLogMdc.STATUS_CODE));
        assertEquals("GET", event.getMDCPropertyMap().get(RequestLogMdc.METHOD));
        assertEquals("/health", event.getMDCPropertyMap().get(RequestLogMdc.PATH));
        // 日志输出后已清理
        assertNull(MDC.get(RequestLogMdc.STATUS_CODE));
    }
}
