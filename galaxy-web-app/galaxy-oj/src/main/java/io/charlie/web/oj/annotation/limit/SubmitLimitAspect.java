package io.charlie.web.oj.annotation.limit;

import cn.dev33.satoken.stp.StpUtil;
import io.charlie.galaxy.exception.SubmitLimitException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.lang.reflect.Method;
import java.util.concurrent.TimeUnit;

@Aspect
@Component
@Slf4j
public class SubmitLimitAspect {

    private final RedisTemplate<String, Object> redisTemplate;
    private final ExpressionParser parser = new SpelExpressionParser();

    public SubmitLimitAspect(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @Around("@annotation(submitLimit)")
    public Object around(ProceedingJoinPoint joinPoint, SubmitLimit submitLimit) throws Throwable {
        String limitKey = generateLimitKey(joinPoint, submitLimit);

        Boolean canSubmit = redisTemplate.opsForValue().setIfAbsent(limitKey, "1", submitLimit.limit(), TimeUnit.SECONDS);

        if (Boolean.FALSE.equals(canSubmit)) {
            log.warn("提交过于频繁，key: {}", limitKey);
            throw new SubmitLimitException(submitLimit.message());
        }

        try {
            return joinPoint.proceed();
        } catch (Exception e) {
            // 如果执行出错，删除限制键，允许重新提交
            redisTemplate.delete(limitKey);
            throw e;
        }
    }

    private String generateLimitKey(ProceedingJoinPoint joinPoint, SubmitLimit submitLimit) {
        StringBuilder keyBuilder = new StringBuilder("submit_limit:");

        // 获取方法信息
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        keyBuilder.append(method.getDeclaringClass().getName())
                .append(".")
                .append(method.getName());

        // 如果配置了自定义key，使用SpEL表达式
        if (!submitLimit.key().isEmpty()) {
            String dynamicKey = parseKey(submitLimit.key(), joinPoint);
            keyBuilder.append(":").append(dynamicKey);
        } else {
            // 默认使用用户ID作为key，如果未登录则使用IP
            String userIdentifier = getUserIdentifier();
            keyBuilder.append(":").append(userIdentifier);
        }

        return keyBuilder.toString();
    }

    private String parseKey(String keySpel, ProceedingJoinPoint joinPoint) {
        try {
            StandardEvaluationContext context = new StandardEvaluationContext();

            // 设置参数
            MethodSignature signature = (MethodSignature) joinPoint.getSignature();
            String[] parameterNames = signature.getParameterNames();
            Object[] args = joinPoint.getArgs();

            for (int i = 0; i < parameterNames.length; i++) {
                context.setVariable(parameterNames[i], args[i]);
            }

            // 设置其他变量
            context.setVariable("methodName", signature.getMethod().getName());
            context.setVariable("ip", getClientIp());
            context.setVariable("userId", getUserId());
            context.setVariable("userIdentifier", getUserIdentifier());

            Expression expression = parser.parseExpression(keySpel);
            return expression.getValue(context, String.class);
        } catch (Exception e) {
            log.warn("解析SpEL表达式失败，使用默认key", e);
            return getUserIdentifier();
        }
    }

    /**
     * 获取用户标识符：优先使用用户ID，未登录时使用IP
     */
    private String getUserIdentifier() {
        try {
            // 检查用户是否登录
            if (StpUtil.isLogin()) {
                // 获取用户ID，转换为字符串
                String loginId = StpUtil.getLoginIdAsString();
                return "user_" + loginId;
            } else {
                // 未登录用户使用IP地址
                return "ip_" + getClientIp();
            }
        } catch (Exception e) {
            log.warn("获取用户标识符失败，使用IP地址", e);
            return "ip_" + getClientIp();
        }
    }

    /**
     * 获取用户ID（仅当用户已登录时）
     */
    private String getUserId() {
        try {
            if (StpUtil.isLogin()) {
                Object loginId = StpUtil.getLoginId();
                return loginId != null ? loginId.toString() : null;
            }
            return null;
        } catch (Exception e) {
            log.warn("获取用户ID失败", e);
            return null;
        }
    }

    private String getClientIp() {
        try {
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attributes == null) {
                return "unknown";
            }

            HttpServletRequest request = attributes.getRequest();
            String ip = request.getHeader("X-Forwarded-For");
            if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
                ip = request.getHeader("Proxy-Client-IP");
            }
            if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
                ip = request.getHeader("WL-Proxy-Client-IP");
            }
            if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
                ip = request.getRemoteAddr();
            }
            return ip;
        } catch (Exception e) {
            return "unknown";
        }
    }
}