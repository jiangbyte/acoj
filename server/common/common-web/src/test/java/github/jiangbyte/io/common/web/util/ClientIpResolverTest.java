package github.jiangbyte.io.common.web.util;

/** Author: Charlie **/

import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ClientIpResolverTest {

    @Test
    void ignoresForwardedHeadersWhenNotTrusted() {
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getHeader("X-Forwarded-For")).thenReturn("1.2.3.4");
        when(request.getRemoteAddr()).thenReturn("10.0.0.8");

        assertEquals("10.0.0.8", ClientIpResolver.resolve(request, false));
    }

    @Test
    void usesFirstForwardedIpWhenTrusted() {
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getHeader("X-Forwarded-For")).thenReturn("1.2.3.4, 10.0.0.1");
        when(request.getRemoteAddr()).thenReturn("10.0.0.8");

        assertEquals("1.2.3.4", ClientIpResolver.resolve(request, true));
    }
}
