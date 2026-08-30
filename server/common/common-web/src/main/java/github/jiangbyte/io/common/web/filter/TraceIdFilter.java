package github.jiangbyte.io.common.web.filter;

import github.jiangbyte.io.common.core.trace.TraceIdHolder;
import github.jiangbyte.io.common.web.log.RequestLogMdc;
import github.jiangbyte.io.common.web.util.ClientIpResolver;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.MDC;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import cn.hutool.core.util.IdUtil;

import java.io.IOException;

/**
 * 链路 TraceId 过滤器：解析/生成 TraceId，写入 MDC 与响应头，请求结束清理。
 *
 * Author: Charlie
 */
public class TraceIdFilter extends OncePerRequestFilter {

    public static final String TRACE_ID_HEADER = "X-Trace-Id";
    public static final String REQUEST_ID_HEADER = "X-Request-Id";

    /**
     * @deprecated 请使用 {@link RequestLogMdc#REQUEST_ID}
     */
    @Deprecated
    public static final String MDC_REQUEST_ID = RequestLogMdc.REQUEST_ID;
    /**
     * @deprecated 请使用 {@link RequestLogMdc#TRACE_ID}
     */
    @Deprecated
    public static final String MDC_TRACE_ID = RequestLogMdc.TRACE_ID;

    private final boolean trustForwardedHeaders;

    public TraceIdFilter() {
        this(false);
    }

    public TraceIdFilter(boolean trustForwardedHeaders) {
        this.trustForwardedHeaders = trustForwardedHeaders;
    }

    /** 解析/生成 TraceId 并写入 MDC 与响应头。 */
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String requestId = request.getHeader(REQUEST_ID_HEADER);
        if (!StringUtils.hasText(requestId)) {
            requestId = IdUtil.simpleUUID();
        }

        String traceId = request.getHeader(TRACE_ID_HEADER);
        if (!StringUtils.hasText(traceId)) {
            traceId = TraceIdHolder.get();
        }
        TraceIdHolder.set(traceId);

        RequestLogMdc.putIfHasText(RequestLogMdc.REQUEST_ID, requestId);
        RequestLogMdc.putIfHasText(RequestLogMdc.TRACE_ID, traceId);
        RequestLogMdc.putIfHasText(RequestLogMdc.METHOD, request.getMethod());
        RequestLogMdc.putIfHasText(RequestLogMdc.PATH, request.getRequestURI());
        RequestLogMdc.putIfHasText(RequestLogMdc.CLIENT_IP, ClientIpResolver.resolve(request, trustForwardedHeaders));
        RequestLogMdc.putIfHasText(RequestLogMdc.USER_AGENT, request.getHeader("User-Agent"));
        RequestLogMdc.syncOtelSpan();

        response.setHeader(REQUEST_ID_HEADER, requestId);
        response.setHeader(TRACE_ID_HEADER, traceId);
        try {
            filterChain.doFilter(request, response);
        } finally {
            RequestLogMdc.clearRequestFields();
            // 清理可能残留的旧版 camelCase 键
            MDC.remove("requestId");
            MDC.remove("traceId");
            TraceIdHolder.clear();
        }
    }
}
