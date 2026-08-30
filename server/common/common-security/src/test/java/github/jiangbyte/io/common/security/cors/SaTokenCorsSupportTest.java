package github.jiangbyte.io.common.security.cors;

/**
 * Author: Charlie
 **/

import cn.dev33.satoken.context.model.SaRequest;
import cn.dev33.satoken.context.model.SaResponse;
import cn.dev33.satoken.exception.BackResultException;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class SaTokenCorsSupportTest {

    @Test
    void starOriginWithoutCredentials() {
        SaRequest req = mock(SaRequest.class);
        SaResponse res = mock(SaResponse.class);
        when(req.getHeader("Origin")).thenReturn("http://anywhere.example");
        when(req.getMethod()).thenReturn("GET");
        when(res.setHeader(org.mockito.ArgumentMatchers.anyString(), org.mockito.ArgumentMatchers.anyString()))
                .thenReturn(res);

        SaTokenCorsSupport.apply(req, res, List.of("*"), true);

        verify(res).setHeader("Access-Control-Allow-Origin", "*");
        verify(res, never()).setHeader(eq("Access-Control-Allow-Credentials"), org.mockito.ArgumentMatchers.anyString());
    }

    @Test
    void listedOriginWithCredentials() {
        SaRequest req = mock(SaRequest.class);
        SaResponse res = mock(SaResponse.class);
        when(req.getHeader("Origin")).thenReturn("http://localhost:5174");
        when(req.getMethod()).thenReturn("GET");
        when(res.setHeader(org.mockito.ArgumentMatchers.anyString(), org.mockito.ArgumentMatchers.anyString()))
                .thenReturn(res);

        SaTokenCorsSupport.apply(req, res, List.of("http://localhost:5173", "http://localhost:5174"), false);

        verify(res).setHeader(eq("Access-Control-Allow-Origin"), eq("http://localhost:5174"));
        verify(res).setHeader(eq("Access-Control-Allow-Credentials"), eq("true"));
    }

    @Test
    void optionsBacksWithEmptyBody() {
        SaRequest req = mock(SaRequest.class);
        SaResponse res = mock(SaResponse.class);
        when(req.getHeader("Origin")).thenReturn("http://localhost:5173");
        when(req.getMethod()).thenReturn("OPTIONS");
        when(res.setHeader(org.mockito.ArgumentMatchers.anyString(), org.mockito.ArgumentMatchers.anyString()))
                .thenReturn(res);
        when(res.setStatus(200)).thenReturn(res);

        assertThrows(
                BackResultException.class,
                () -> SaTokenCorsSupport.apply(
                        req, res, List.of("http://localhost:5173"), false));
    }
}
