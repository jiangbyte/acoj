package github.jiangbyte.io.common.web.filter;

/**
 * Author: Charlie
 **/

import github.jiangbyte.io.common.web.log.RequestLogMdc;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TraceIdFilterTest {

    @AfterEach
    void clearMdc() {
        MDC.clear();
    }

    @Test
    void generatesRequestIdAndPopulatesMdc() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/v1/admin/sys/file/page");
        request.addHeader("User-Agent", "junit");
        MockHttpServletResponse response = new MockHttpServletResponse();

        new TraceIdFilter().doFilter(request, response, (req, res) -> {
            assertNotNull(MDC.get(RequestLogMdc.REQUEST_ID));
            assertFalse(MDC.get(RequestLogMdc.REQUEST_ID).isBlank());
            assertEquals("GET", MDC.get(RequestLogMdc.METHOD));
            assertEquals("/api/v1/admin/sys/file/page", MDC.get(RequestLogMdc.PATH));
            assertEquals("junit", MDC.get(RequestLogMdc.USER_AGENT));
            assertNotNull(MDC.get(RequestLogMdc.CLIENT_IP));
            assertNotNull(MDC.get(RequestLogMdc.TRACE_ID));
        });

        String echoed = response.getHeader(TraceIdFilter.REQUEST_ID_HEADER);
        assertNotNull(echoed);
        assertTrue(echoed.matches("[0-9a-f]{32}"));
        assertNull(MDC.get(RequestLogMdc.REQUEST_ID));
    }

    @Test
    void honorsInboundRequestId() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/x");
        request.addHeader(TraceIdFilter.REQUEST_ID_HEADER, "rid-fixed-001");
        MockHttpServletResponse response = new MockHttpServletResponse();

        new TraceIdFilter().doFilter(request, response, (req, res) ->
                assertEquals("rid-fixed-001", MDC.get(RequestLogMdc.REQUEST_ID)));

        assertEquals("rid-fixed-001", response.getHeader(TraceIdFilter.REQUEST_ID_HEADER));
    }
}
