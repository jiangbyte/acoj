package github.jiangbyte.io.common.web.log;

import org.slf4j.MDC;

/**
 * 请求日志 MDC 键名与写入辅助，统一访问日志上下文字段。
 *
 * Author: Charlie
 */
public final class RequestLogMdc {

    public static final String REQUEST_ID = "request_id";
    public static final String TRACE_ID = "trace_id";
    public static final String SPAN_ID = "span_id";
    public static final String METHOD = "method";
    public static final String PATH = "path";
    public static final String CLIENT_IP = "client_ip";
    public static final String USER_AGENT = "user_agent";
    public static final String ACCOUNT_ID = "account_id";
    public static final String ACCOUNT_TYPE = "account_type";
    public static final String STATUS_CODE = "status_code";
    public static final String DURATION_MS = "duration_ms";

    private RequestLogMdc() {
    }

    /** 值非空时写入 MDC。 */
    public static void putIfHasText(String key, String value) {
        if (key == null || value == null || value.isBlank() || "-".equals(value)) {
            return;
        }
        MDC.put(key, value);
    }

    /** 清理本次请求相关的 MDC 字段。 */
    public static void clearRequestFields() {
        MDC.remove(REQUEST_ID);
        MDC.remove(TRACE_ID);
        MDC.remove(SPAN_ID);
        MDC.remove(METHOD);
        MDC.remove(PATH);
        MDC.remove(CLIENT_IP);
        MDC.remove(USER_AGENT);
        MDC.remove(ACCOUNT_ID);
        MDC.remove(ACCOUNT_TYPE);
        MDC.remove(STATUS_CODE);
        MDC.remove(DURATION_MS);
    }

    /** 清理账号相关的 MDC 字段。 */
    public static void clearAccountFields() {
        MDC.remove(ACCOUNT_ID);
        MDC.remove(ACCOUNT_TYPE);
    }

    /**
     * 尽力将当前 OpenTelemetry span 同步到 MDC（类路径可选）。
     */
    public static void syncOtelSpan() {
        try {
            Class<?> spanClass = Class.forName("io.opentelemetry.api.trace.Span");
            Object span = spanClass.getMethod("current").invoke(null);
            Object ctx = spanClass.getMethod("getSpanContext").invoke(span);
            Boolean valid = (Boolean) ctx.getClass().getMethod("isValid").invoke(ctx);
            if (!Boolean.TRUE.equals(valid)) {
                return;
            }
            String traceId = (String) ctx.getClass().getMethod("getTraceId").invoke(ctx);
            String spanId = (String) ctx.getClass().getMethod("getSpanId").invoke(ctx);
            putIfHasText(TRACE_ID, traceId);
            putIfHasText(SPAN_ID, spanId);
        } catch (ReflectiveOperationException | LinkageError ignored) {
            // 类路径无 OTel API 或不可用
        }
    }
}
