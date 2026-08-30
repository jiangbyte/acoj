package github.jiangbyte.io.common.log.aspect;

import github.jiangbyte.io.common.core.param.IdParam;
import github.jiangbyte.io.common.core.param.IdsParam;
import github.jiangbyte.io.common.core.trace.TraceIdHolder;
import github.jiangbyte.io.common.log.annotation.OperationAudit;
import github.jiangbyte.io.common.log.audit.AuditContext;
import github.jiangbyte.io.common.log.audit.AuditEventMessage;
import github.jiangbyte.io.common.log.audit.AuditEventPublisher;
import github.jiangbyte.io.common.log.audit.AuditLabelCatalog;
import github.jiangbyte.io.common.log.audit.AuditOutboxWriter;
import github.jiangbyte.io.common.log.audit.AuditSkipCatalog;
import github.jiangbyte.io.common.log.audit.AuditSnapshots;
import github.jiangbyte.io.common.satoken.model.LoginUser;
import github.jiangbyte.io.common.satoken.utils.LoginHelper;
import github.jiangbyte.io.common.web.log.RequestLogMdc;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.MDC;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.util.StringUtils;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.multipart.MultipartFile;

import java.lang.reflect.Method;
import java.time.Instant;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 操作审计切面：拦截 @OperationAudit，组装可读操作内容并经 outbox 或 AuditEventPublisher 发布。
 *
 * Author: Charlie
 */
@Slf4j
@Aspect
public class OperationAuditAspect {

    private final AuditEventPublisher auditEventPublisher;
    private final ObjectProvider<AuditOutboxWriter> auditOutboxWriter;

    public OperationAuditAspect(
            AuditEventPublisher auditEventPublisher,
            ObjectProvider<AuditOutboxWriter> auditOutboxWriter) {
        this.auditEventPublisher = auditEventPublisher;
        this.auditOutboxWriter = auditOutboxWriter;
    }

    /** 记录并发布操作审计事件。 */
    @Around("@annotation(operationAudit)")
    public Object recordOperationAudit(ProceedingJoinPoint joinPoint, OperationAudit operationAudit) throws Throwable {
        HttpServletRequest request = currentRequest();
        long startNs = System.nanoTime();
        boolean success = true;
        String errorMessage = null;
        try {
            Object result = joinPoint.proceed();
            return result;
        } catch (Throwable ex) {
            success = false;
            errorMessage = ex.getMessage();
            throw ex;
        } finally {
            long durationMs = Math.max(0L, (System.nanoTime() - startNs) / 1_000_000L);
            try {
                publishAuditEvent(operationAudit, request, joinPoint.getArgs(), durationMs, success, errorMessage);
            } catch (Exception ex) {
                log.warn("Failed to publish operation audit: {}", ex.getMessage());
            } finally {
                AuditContext.clear();
            }
        }
    }

    private void publishAuditEvent(
            OperationAudit operationAudit,
            HttpServletRequest request,
            Object[] args,
            long durationMs,
            boolean success,
            String errorMessage) {
        String action = operationAudit.action();
        if (action == null || action.isBlank()) {
            action = request != null ? request.getMethod().toLowerCase() : "unknown";
        }
        if (AuditSkipCatalog.shouldSkip(operationAudit.resourceType(), action)) {
            return;
        }
        LoginUser loginUser = LoginHelper.currentUser().orElse(null);
        String resourceType = operationAudit.resourceType();
        String actionName = AuditLabelCatalog.actionName(resourceType, action, operationAudit.name());
        String actionType = AuditLabelCatalog.actionType(action, operationAudit.actionType());
        String moduleLabel = AuditLabelCatalog.moduleLabel(resourceType);
        String path = request != null ? request.getRequestURI() : null;

        Map<String, Object> beforeData = new LinkedHashMap<>(AuditContext.getBefore());
        Map<String, Object> afterData = new LinkedHashMap<>(AuditContext.getAfter());
        if (afterData.isEmpty()) {
            Map<String, Object> fromArgs = extractRequestPayload(args);
            if (!fromArgs.isEmpty()) {
                // 无业务快照时：create 用空→请求体；其余仅记录请求体到 after 便于摘要
                if (isCreateLike(action) && beforeData.isEmpty()) {
                    afterData.putAll(fromArgs);
                } else if (!isDeleteLike(action)) {
                    afterData.putAll(fromArgs);
                }
            }
        }

        String subject = AuditContext.getSubject();
        if (!StringUtils.hasText(subject)) {
            subject = resolveSubject(action, args, beforeData, afterData, loginUser);
        }
        String resourceId = AuditContext.getResourceId();
        if (!StringUtils.hasText(resourceId)) {
            resourceId = resolveResourceId(args, request);
        }

        String summary = AuditLabelCatalog.buildContent(
                action, resourceType, actionName, subject, success, beforeData, afterData);

        String operatorName = null;
        if (loginUser != null) {
            operatorName = StringUtils.hasText(loginUser.getAccount())
                    ? loginUser.getAccount()
                    : loginUser.getAccountId();
        } else if (StringUtils.hasText(subject) && isLoginLike(action)) {
            operatorName = subject;
        }

        String accountId = loginUser != null ? loginUser.getAccountId() : null;
        String accountType = loginUser != null && loginUser.getAccountType() != null
                ? loginUser.getAccountType().name().toLowerCase()
                : null;
        // logout 在业务层会先清 session，finally 中 currentUser 为空，用 resourceId 兜底
        if (!StringUtils.hasText(accountId) && "logout".equalsIgnoreCase(action) && StringUtils.hasText(resourceId)) {
            accountId = resourceId;
        }

        AuditEventMessage message = AuditEventMessage.builder()
                .resourceType(resourceType)
                .resourceId(resourceId)
                .action(action)
                .actionName(actionName)
                .actionType(actionType)
                .moduleLabel(moduleLabel)
                .operatorName(operatorName)
                .summary(summary)
                .beforeData(beforeData.isEmpty() ? null : beforeData)
                .afterData(afterData.isEmpty() ? null : afterData)
                .durationMs((int) Math.min(durationMs, Integer.MAX_VALUE))
                .method(request != null ? request.getMethod() : null)
                .path(path)
                .statusCode(success ? 200 : 500)
                .accountId(accountId)
                .accountType(accountType)
                .requestId(resolveRequestId())
                .ip(request != null ? request.getRemoteAddr() : null)
                .userAgent(request != null ? request.getHeader("User-Agent") : null)
                .occurredAt(Instant.now())
                .build();

        AuditOutboxWriter writer = auditOutboxWriter.getIfAvailable();
        if (writer != null) {
            writer.write(message);
        } else {
            auditEventPublisher.publish(message);
        }
    }

    private static boolean isCreateLike(String action) {
        String act = action == null ? "" : action.trim().toLowerCase();
        return "create".equals(act) || "submit".equals(act) || "register".equals(act) || "upload".equals(act);
    }

    private static boolean isDeleteLike(String action) {
        String act = action == null ? "" : action.trim().toLowerCase();
        return "delete".equals(act) || "cancel".equals(act);
    }

    private static boolean isLoginLike(String action) {
        String act = action == null ? "" : action.trim().toLowerCase();
        return "login".equals(act) || "register".equals(act) || act.startsWith("oauth");
    }

    private static String resolveSubject(
            String action,
            Object[] args,
            Map<String, Object> beforeData,
            Map<String, Object> afterData,
            LoginUser loginUser) {
        if (isLoginLike(action)) {
            String fromArgs = readStringProperty(args, "account", "email", "phone", "username");
            if (StringUtils.hasText(fromArgs)) {
                return fromArgs;
            }
        }
        for (String key : new String[]{"name", "title", "label", "account", "code", "originalName", "original_name"}) {
            Object value = afterData.getOrDefault(key, beforeData.get(key));
            if (value != null && StringUtils.hasText(String.valueOf(value))) {
                return String.valueOf(value);
            }
        }
        if (loginUser != null && StringUtils.hasText(loginUser.getAccount())) {
            return loginUser.getAccount();
        }
        return null;
    }

    private static Map<String, Object> extractRequestPayload(Object[] args) {
        Map<String, Object> result = new LinkedHashMap<>();
        if (args == null) {
            return result;
        }
        for (Object arg : args) {
            if (arg == null
                    || arg instanceof HttpServletRequest
                    || arg instanceof MultipartFile
                    || arg instanceof MultipartFile[]
                    || arg instanceof IdParam
                    || arg instanceof IdsParam) {
                continue;
            }
            if (arg instanceof CharSequence || arg instanceof Number || arg instanceof Boolean) {
                continue;
            }
            Map<String, Object> map = AuditSnapshots.toMap(arg);
            if (!map.isEmpty()) {
                result.putAll(map);
            }
        }
        return result;
    }

    private static String readStringProperty(Object[] args, String... keys) {
        if (args == null) {
            return null;
        }
        for (Object arg : args) {
            if (arg == null) {
                continue;
            }
            Map<String, Object> map = AuditSnapshots.toMap(arg);
            for (String key : keys) {
                Object value = map.get(key);
                if (value != null && StringUtils.hasText(String.valueOf(value))) {
                    return String.valueOf(value);
                }
            }
        }
        return null;
    }

    private static String resolveResourceId(Object[] args, HttpServletRequest request) {
        if (args != null) {
            for (Object arg : args) {
                if (arg == null) {
                    continue;
                }
                if (arg instanceof IdParam idParam && StringUtils.hasText(idParam.getId())) {
                    return idParam.getId();
                }
                if (arg instanceof IdsParam idsParam && idsParam.getIds() != null && !idsParam.getIds().isEmpty()) {
                    return String.join(",", idsParam.getIds());
                }
                String fromBean = readIdProperty(arg);
                if (StringUtils.hasText(fromBean)) {
                    return fromBean;
                }
            }
        }
        if (request != null) {
            String id = request.getParameter("id");
            if (StringUtils.hasText(id)) {
                return id;
            }
        }
        return null;
    }

    private static String readIdProperty(Object arg) {
        if (arg instanceof Map<?, ?> map) {
            Object id = map.get("id");
            if (id == null) {
                id = map.get("ids");
            }
            if (id instanceof Collection<?> col && !col.isEmpty()) {
                return String.valueOf(col.iterator().next());
            }
            return id == null ? null : String.valueOf(id);
        }
        try {
            Method getter = arg.getClass().getMethod("getId");
            Object value = getter.invoke(arg);
            return value == null ? null : String.valueOf(value);
        } catch (ReflectiveOperationException ignored) {
            return null;
        }
    }

    private HttpServletRequest currentRequest() {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        return attributes != null ? attributes.getRequest() : null;
    }

    private static String resolveRequestId() {
        String fromMdc = MDC.get(RequestLogMdc.REQUEST_ID);
        if (StringUtils.hasText(fromMdc)) {
            return fromMdc;
        }
        return TraceIdHolder.get();
    }
}
