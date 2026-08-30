package github.jiangbyte.io.common.log.audit;

import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeanWrapperImpl;
import org.springframework.util.StringUtils;

import java.beans.PropertyDescriptor;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/**
 * 业务侧写入审计前后快照的便捷方法。
 *
 * Author: Charlie
 */
public final class AuditSnapshots {

    private static final Set<String> IGNORE_KEYS = Set.of(
            "class", "serialversionuid", "password", "passwordhash", "password_hash",
            "newpassword", "oldpassword", "token", "accesstoken", "refreshtoken",
            "secret", "cryptoKey", "cryptokey", "createdat", "updatedat",
            "createdby", "updatedby", "params", "handler", "hibernateLazyInitializer");

    private static final List<String> SUBJECT_KEYS = List.of(
            "name", "title", "label", "account", "code", "username", "nickname",
            "originalName", "original_name", "fileName", "filename", "key", "id");

    private AuditSnapshots() {
    }

    public static void subject(String subject) {
        if (StringUtils.hasText(subject)) {
            AuditContext.setSubject(subject.trim());
        }
    }

    public static void resourceId(String resourceId) {
        if (StringUtils.hasText(resourceId)) {
            AuditContext.setResourceId(resourceId.trim());
        }
    }

    public static void before(Object entity) {
        Map<String, Object> map = toMap(entity);
        AuditContext.setBefore(map);
        if (!StringUtils.hasText(AuditContext.getSubject())) {
            subject(resolveSubject(map, entity));
        }
        if (!StringUtils.hasText(AuditContext.getResourceId())) {
            resourceId(resolveId(map, entity));
        }
    }

    public static void after(Object entity) {
        Map<String, Object> map = toMap(entity);
        AuditContext.setAfter(map);
        if (!StringUtils.hasText(AuditContext.getSubject())) {
            subject(resolveSubject(map, entity));
        }
        if (!StringUtils.hasText(AuditContext.getResourceId())) {
            resourceId(resolveId(map, entity));
        }
    }

    /** 新建：after 有值，before 视为空，用于生成「从【空】修改为【新】」。 */
    public static void created(Object entity) {
        Map<String, Object> map = toMap(entity);
        AuditContext.setBefore(Map.of());
        AuditContext.setAfter(map);
        subject(resolveSubject(map, entity));
        resourceId(resolveId(map, entity));
    }

    /** 删除：仅 before。 */
    public static void deleted(Object entity) {
        Map<String, Object> map = toMap(entity);
        AuditContext.setBefore(map);
        AuditContext.setAfter(Map.of());
        subject(resolveSubject(map, entity));
        resourceId(resolveId(map, entity));
    }

    /** 批量删除：拼接展示名。 */
    public static void deletedAll(Collection<?> entities) {
        if (entities == null || entities.isEmpty()) {
            return;
        }
        List<String> names = new ArrayList<>();
        List<String> ids = new ArrayList<>();
        Map<String, Object> first = null;
        for (Object entity : entities) {
            Map<String, Object> map = toMap(entity);
            if (first == null) {
                first = map;
            }
            String name = resolveSubject(map, entity);
            if (StringUtils.hasText(name)) {
                names.add(name);
            }
            String id = resolveId(map, entity);
            if (StringUtils.hasText(id)) {
                ids.add(id);
            }
        }
        if (first != null) {
            AuditContext.setBefore(first);
        }
        AuditContext.setAfter(Map.of());
        if (!names.isEmpty()) {
            subject(String.join("，", names));
        }
        if (!ids.isEmpty()) {
            resourceId(String.join(",", ids));
        }
    }

    public static Map<String, Object> toMap(Object source) {
        if (source == null) {
            return new LinkedHashMap<>();
        }
        if (source instanceof Map<?, ?> map) {
            Map<String, Object> result = new LinkedHashMap<>();
            map.forEach((k, v) -> {
                if (k != null) {
                    putIfUseful(result, String.valueOf(k), v);
                }
            });
            return result;
        }
        Map<String, Object> result = new LinkedHashMap<>();
        BeanWrapper wrapper = new BeanWrapperImpl(source);
        for (PropertyDescriptor descriptor : wrapper.getPropertyDescriptors()) {
            String name = descriptor.getName();
            if (name == null || IGNORE_KEYS.contains(name.toLowerCase(Locale.ROOT))) {
                continue;
            }
            if (descriptor.getReadMethod() == null) {
                continue;
            }
            Object value;
            try {
                value = wrapper.getPropertyValue(name);
            } catch (Exception ignored) {
                continue;
            }
            putIfUseful(result, name, value);
        }
        return result;
    }

    private static void putIfUseful(Map<String, Object> target, String key, Object value) {
        if (!StringUtils.hasText(key) || IGNORE_KEYS.contains(key.toLowerCase(Locale.ROOT))) {
            return;
        }
        if (value == null) {
            return;
        }
        if (value instanceof Collection<?> col) {
            target.put(key, col.stream().map(String::valueOf).toList());
            return;
        }
        if (value.getClass().isArray()) {
            return;
        }
        if (isSimpleValue(value)) {
            target.put(key, value);
        }
    }

    private static boolean isSimpleValue(Object value) {
        return value instanceof CharSequence
                || value instanceof Number
                || value instanceof Boolean
                || value instanceof Enum<?>
                || value instanceof java.time.temporal.Temporal
                || value instanceof java.util.Date;
    }

    private static String resolveSubject(Map<String, Object> map, Object entity) {
        for (String key : SUBJECT_KEYS) {
            Object value = map.get(key);
            if (value != null && StringUtils.hasText(String.valueOf(value))) {
                return String.valueOf(value);
            }
        }
        return resolveId(map, entity);
    }

    private static String resolveId(Map<String, Object> map, Object entity) {
        Object id = map.get("id");
        if (id != null && StringUtils.hasText(String.valueOf(id))) {
            return String.valueOf(id);
        }
        try {
            Method getter = entity.getClass().getMethod("getId");
            Object value = getter.invoke(entity);
            return value == null ? null : String.valueOf(value);
        } catch (ReflectiveOperationException ignored) {
            return null;
        }
    }
}
