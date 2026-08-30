package github.jiangbyte.io.common.core.trace;

import cn.hutool.core.util.IdUtil;

/**
 * 请求链路 TraceId 的 ThreadLocal 持有器。
 * 供过滤器写入、日志 MDC 与下游透传读取；无值时自动生成。
 *
 * Author: Charlie
 */
public final class TraceIdHolder {

    private static final ThreadLocal<String> TRACE_ID = new ThreadLocal<>();

    private TraceIdHolder() {
    }

    /** 获取当前 TraceId；为空时生成并缓存。 */
    public static String get() {
        String traceId = TRACE_ID.get();
        if (traceId == null || traceId.isBlank()) {
            traceId = IdUtil.simpleUUID();
            TRACE_ID.set(traceId);
        }
        return traceId;
    }

    /** 设置当前线程的 TraceId。 */
    public static void set(String traceId) {
        TRACE_ID.set(traceId);
    }

    /** 清理当前线程的 TraceId，避免线程池串扰。 */
    public static void clear() {
        TRACE_ID.remove();
    }
}
