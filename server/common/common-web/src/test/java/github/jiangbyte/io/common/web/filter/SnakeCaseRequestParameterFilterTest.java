package github.jiangbyte.io.common.web.filter;

/**
 * Author: Charlie
 **/

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class SnakeCaseRequestParameterFilterTest {

    @Test
    void snakeToCamelConvertsSegments() {
        assertEquals("accountType", SnakeCaseRequestParameterFilter.SnakeCaseParameterRequestWrapper.snakeToCamel("account_type"));
        assertEquals("accountStatus", SnakeCaseRequestParameterFilter.SnakeCaseParameterRequestWrapper.snakeToCamel("account_status"));
        assertEquals("name", SnakeCaseRequestParameterFilter.SnakeCaseParameterRequestWrapper.snakeToCamel("name"));
        assertNull(SnakeCaseRequestParameterFilter.SnakeCaseParameterRequestWrapper.snakeToCamel(null));
    }

    @Test
    void wrapsRequestWithCamelCaseAliases() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setParameter("account_type", "PORTAL");
        request.setParameter("account_status", "ENABLED");
        request.setParameter("name", "alice");

        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain chain = new MockFilterChain();

        new SnakeCaseRequestParameterFilter().doFilter(request, response, chain);

        var wrapped = (jakarta.servlet.http.HttpServletRequest) chain.getRequest();
        assertEquals("PORTAL", wrapped.getParameter("account_type"));
        assertEquals("PORTAL", wrapped.getParameter("accountType"));
        assertEquals("ENABLED", wrapped.getParameter("accountStatus"));
        assertEquals("alice", wrapped.getParameter("name"));
        assertArrayEquals(new String[]{"PORTAL"}, wrapped.getParameterValues("accountType"));
    }
}
