package github.jiangbyte.io.common.web.filter;

import github.jiangbyte.io.common.web.log.RequestLogMdc;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * 访问日志过滤器：记录方法、路径、状态码、耗时与 MDC 上下文字段。
 *
 * Author: Charlie
 */
public class AccessLogFilter extends OncePerRequestFilter {

    public static final String LOGGER_NAME = "access";

    private static final Logger ACCESS = LoggerFactory.getLogger(LOGGER_NAME);

    /** 记录访问日志并写入耗时等 MDC 字段。 */
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        long startNs = System.nanoTime();
        int statusCode = 500;
        try {
            filterChain.doFilter(request, response);
            statusCode = response.getStatus();
        } catch (ServletException | IOException | RuntimeException exception) {
            statusCode = response.isCommitted() ? response.getStatus() : 500;
            throw exception;
        } finally {
            RequestLogMdc.syncOtelSpan();
            double durationMs = Math.round((System.nanoTime() - startNs) / 1_000_000.0 * 100.0) / 100.0;
            RequestLogMdc.putIfHasText(RequestLogMdc.METHOD,
                    firstNonBlank(MDC.get(RequestLogMdc.METHOD), request.getMethod()));
            RequestLogMdc.putIfHasText(RequestLogMdc.PATH,
                    firstNonBlank(MDC.get(RequestLogMdc.PATH), request.getRequestURI()));
            RequestLogMdc.putIfHasText(RequestLogMdc.STATUS_CODE, String.valueOf(statusCode));
            RequestLogMdc.putIfHasText(RequestLogMdc.DURATION_MS, String.valueOf(durationMs));
            try {
                ACCESS.info("http.access");
            } finally {
                MDC.remove(RequestLogMdc.STATUS_CODE);
                MDC.remove(RequestLogMdc.DURATION_MS);
            }
        }
    }

    private static String firstNonBlank(String primary, String fallback) {
        if (primary != null && !primary.isBlank()) {
            return primary;
        }
        return fallback;
    }
}
