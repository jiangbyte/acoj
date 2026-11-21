package io.charlie.web.modular.llm.config;

/**
 * 当前流式对话的工具轨迹上下文（ThreadLocal）。
 */
public final class LlmToolTraceContext {

    private static final ThreadLocal<Holder> HOLDER = new ThreadLocal<>();

    private LlmToolTraceContext() {
    }

    public static void set(String conversationId, String userId, String problemId) {
        Holder holder = new Holder();
        holder.conversationId = conversationId;
        holder.userId = userId;
        holder.problemId = problemId;
        HOLDER.set(holder);
    }

    public static Holder get() {
        return HOLDER.get();
    }

    public static void clear() {
        HOLDER.remove();
    }

    public static final class Holder {
        public String conversationId;
        public String userId;
        public String problemId;
    }
}
