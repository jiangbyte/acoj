package github.jiangbyte.io.iam.modules.relation.service.impl;

import github.jiangbyte.io.iam.modules.relation.service.PermissionRegistryService;

import cn.dev33.satoken.annotation.SaCheckPermission;
import github.jiangbyte.io.common.core.exception.BizException;
import github.jiangbyte.io.iam.modules.relation.result.SysRegisteredPermissionResult;
import org.redisson.api.RBucket;
import org.redisson.api.RedissonClient;
import org.redisson.client.codec.StringCodec;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;

/**
 * 权限注册表服务实现：扫描注解权限、落库/缓存同步与列表查询。
 *
 * Author: Charlie
 */
@Service
@RequiredArgsConstructor
public class PermissionRegistryServiceImpl implements PermissionRegistryService {

    private static final Logger log = LoggerFactory.getLogger(PermissionRegistryServiceImpl.class);
    private static final String RESOURCE_CACHE_KEY = "Cache:permission-resource";
    private static final String METHOD_CACHE_KEY = "Cache:permission-resource-method";
    private static final String OPERATION_ANNOTATION = "io.swagger.v3.oas.annotations.Operation";

    private final RedissonClient redissonClient;
    private final ObjectMapper objectMapper;
    private final RequestMappingHandlerMapping requestMappingHandlerMapping;

    @EventListener(ContextRefreshedEvent.class)
    public void onContextRefreshed(ContextRefreshedEvent event) {
        if (event.getApplicationContext().getParent() != null) {
            return;
        }
        syncToRedis();
    }

    @Override
    public void syncToRedis() {
        // 扫描注解 → 空结果拒绝覆盖 → 写入资源列表与 HTTP 方法映射
        ScanResult scan = scanPermissions();
        if (scan.resourceTexts().isEmpty()) {
            log.warn("Permission registry scan returned empty result; refusing to overwrite Redis");
            return;
        }
        try {
            RBucket<String> resourceBucket = redissonClient.getBucket(RESOURCE_CACHE_KEY, StringCodec.INSTANCE);
            RBucket<String> methodBucket = redissonClient.getBucket(METHOD_CACHE_KEY, StringCodec.INSTANCE);
            resourceBucket.set(objectMapper.writeValueAsString(scan.resourceTexts()));
            methodBucket.set(objectMapper.writeValueAsString(scan.methodMap()));
            log.info("Wrote permission registry to Redis: count={}", scan.resourceTexts().size());
        } catch (Exception ex) {
            throw new BizException(500, "Failed to write permission registry to Redis: " + ex.getMessage());
        }
    }

    @Override
    public List<SysRegisteredPermissionResult> listRegisteredPermissions() {
        // 优先读 Redis；缓存空则现场扫描
        List<String> resourceTexts = readResourceTextsFromRedis();
        if (resourceTexts.isEmpty()) {
            resourceTexts = scanPermissions().resourceTexts();
        }
        return resourceTexts.stream()
                .map(this::toResult)
                .sorted(Comparator.comparing(SysRegisteredPermissionResult::permissionKey))
                .toList();
    }

    @Override
    public void ensureRegistered(String permissionKey) {
        if (!StringUtils.hasText(permissionKey)) {
            return;
        }
        Set<String> registered = listRegisteredPermissionKeys();
        if (!registered.contains(permissionKey)) {
            throw new BizException("Permission is not registered in Redis: " + permissionKey);
        }
    }

    private Set<String> listRegisteredPermissionKeys() {
        List<String> resourceTexts = readResourceTextsFromRedis();
        if (resourceTexts.isEmpty()) {
            resourceTexts = scanPermissions().resourceTexts();
        }
        return resourceTexts.stream()
                .map(this::extractPermissionKey)
                .filter(StringUtils::hasText)
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    private List<String> readResourceTextsFromRedis() {
        try {
            RBucket<String> bucket = redissonClient.getBucket(RESOURCE_CACHE_KEY, StringCodec.INSTANCE);
            String raw = bucket.get();
            if (!StringUtils.hasText(raw)) {
                return List.of();
            }
            List<String> values = objectMapper.readValue(raw, new TypeReference<List<String>>() {
            });
            return values == null ? List.of() : values;
        } catch (Exception ex) {
            log.warn("Failed to read permission registry from Redis: {}", ex.getMessage());
            return List.of();
        }
    }

    private ScanResult scanPermissions() {
        // 遍历全部 HandlerMethod，收集 @SaCheckPermission（方法优先于类）
        Map<String, ScannedPermission> map = new LinkedHashMap<>();
        Map<RequestMappingInfo, HandlerMethod> handlerMethods = requestMappingHandlerMapping.getHandlerMethods();
        for (Map.Entry<RequestMappingInfo, HandlerMethod> entry : handlerMethods.entrySet()) {
            HandlerMethod handlerMethod = entry.getValue();
            SaCheckPermission annotation = handlerMethod.getMethodAnnotation(SaCheckPermission.class);
            if (annotation == null) {
                annotation = handlerMethod.getBeanType().getAnnotation(SaCheckPermission.class);
            }
            if (annotation == null) {
                continue;
            }
            String name = resolveName(handlerMethod);
            String method = resolveHttpMethod(entry.getKey());
            // 同 permissionKey 首次出现保留（去重）
            for (String permissionKey : annotation.value()) {
                if (!StringUtils.hasText(permissionKey) || map.containsKey(permissionKey)) {
                    continue;
                }
                map.put(permissionKey, new ScannedPermission(permissionKey, name, method));
            }
        }

        // 序列化为 key[name] 文本列表 + HTTP 方法映射
        List<String> resourceTexts = new ArrayList<>();
        Map<String, String> methodMap = new LinkedHashMap<>();
        map.values().stream()
                .sorted(Comparator.comparing(ScannedPermission::permissionKey))
                .forEach(item -> {
                    String text = item.permissionKey() + "[" + item.name() + "]";
                    resourceTexts.add(text);
                    methodMap.put(text, item.method());
                });
        return new ScanResult(resourceTexts, methodMap);
    }

    private String resolveHttpMethod(RequestMappingInfo mappingInfo) {
        Set<RequestMethod> methods = mappingInfo.getMethodsCondition().getMethods();
        if (methods == null || methods.isEmpty()) {
            return "GET";
        }
        return methods.stream().findFirst().map(Enum::name).orElse("GET");
    }

    private String resolveName(HandlerMethod handlerMethod) {
        Annotation operation = findAnnotation(handlerMethod.getMethod(), OPERATION_ANNOTATION);
        if (operation != null) {
            try {
                Object summary = operation.annotationType().getMethod("summary").invoke(operation);
                if (summary instanceof String s && StringUtils.hasText(s)) {
                    return s;
                }
            } catch (ReflectiveOperationException ignored) {
                // 继续向下
            }
        }
        return handlerMethod.getMethod().getName();
    }

    private Annotation findAnnotation(Method method, String annotationClassName) {
        return Arrays.stream(method.getAnnotations())
                .filter(a -> annotationClassName.equals(a.annotationType().getName()))
                .findFirst()
                .orElse(null);
    }

    private SysRegisteredPermissionResult toResult(String resourceText) {
        String key = extractPermissionKey(resourceText);
        String name = "";
        int idx = resourceText.indexOf('[');
        if (idx > 0 && resourceText.endsWith("]")) {
            name = resourceText.substring(idx + 1, resourceText.length() - 1);
        }
        String[] parts = key.split(":");
        String module = parts.length > 0 ? parts[0] : "";
        String resource = parts.length > 1 ? parts[1] : "";
        String action = parts.length > 2 ? parts[2] : "";
        return new SysRegisteredPermissionResult(key, name, module, resource, action);
    }

    private String extractPermissionKey(String resourceText) {
        if (!StringUtils.hasText(resourceText)) {
            return "";
        }
        int idx = resourceText.indexOf('[');
        return idx > 0 ? resourceText.substring(0, idx) : resourceText;
    }

    private record ScannedPermission(String permissionKey, String name, String method) {
    }

    private record ScanResult(List<String> resourceTexts, Map<String, String> methodMap) {
    }
}
