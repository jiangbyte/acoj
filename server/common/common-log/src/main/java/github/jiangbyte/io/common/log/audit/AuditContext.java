package github.jiangbyte.io.common.log.audit;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 当前请求线程内的审计快照（由业务写入，切面读取后清理）。
 *
 * Author: Charlie
 */
public final class AuditContext {

    private static final ThreadLocal<State> HOLDER = ThreadLocal.withInitial(State::new);

    private AuditContext() {
    }

    public static void setSubject(String subject) {
        HOLDER.get().subject = subject;
    }

    public static void setResourceId(String resourceId) {
        HOLDER.get().resourceId = resourceId;
    }

    public static void setBefore(Map<String, Object> before) {
        HOLDER.get().before = copy(before);
    }

    public static void setAfter(Map<String, Object> after) {
        HOLDER.get().after = copy(after);
    }

    public static void mergeAfter(Map<String, Object> after) {
        if (after == null || after.isEmpty()) {
            return;
        }
        State state = HOLDER.get();
        if (state.after == null) {
            state.after = new LinkedHashMap<>();
        }
        state.after.putAll(after);
    }

    public static String getSubject() {
        return HOLDER.get().subject;
    }

    public static String getResourceId() {
        return HOLDER.get().resourceId;
    }

    public static Map<String, Object> getBefore() {
        Map<String, Object> before = HOLDER.get().before;
        return before == null ? Collections.emptyMap() : before;
    }

    public static Map<String, Object> getAfter() {
        Map<String, Object> after = HOLDER.get().after;
        return after == null ? Collections.emptyMap() : after;
    }

    public static boolean hasSnapshot() {
        State state = HOLDER.get();
        return (state.before != null && !state.before.isEmpty())
                || (state.after != null && !state.after.isEmpty())
                || (state.subject != null && !state.subject.isBlank());
    }

    public static void clear() {
        HOLDER.remove();
    }

    private static Map<String, Object> copy(Map<String, Object> source) {
        if (source == null || source.isEmpty()) {
            return new LinkedHashMap<>();
        }
        return new LinkedHashMap<>(source);
    }

    private static final class State {
        private String subject;
        private String resourceId;
        private Map<String, Object> before;
        private Map<String, Object> after;
    }
}
