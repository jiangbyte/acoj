package github.jiangbyte.io.common.security.ratelimit;

import github.jiangbyte.io.common.satoken.StpKit;
import github.jiangbyte.io.common.security.config.HeiSecurityProperties;
import github.jiangbyte.io.common.web.util.ClientIpResolver;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.redisson.api.RRateLimiter;
import org.redisson.api.RateIntervalUnit;
import org.redisson.api.RateType;
import org.redisson.api.RedissonClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.util.StringUtils;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;

/**
 * {@link RateLimit} 切面：基于 Redisson RateLimiter。
 *
 * Author: Charlie
 */
@Aspect
public class RateLimitAspect {

    private static final Logger log = LoggerFactory.getLogger(RateLimitAspect.class);

    private final RedissonClient redissonClient;
    private final RateLimitProperties properties;
    private final HeiSecurityProperties securityProperties;

    public RateLimitAspect(
            RedissonClient redissonClient,
            RateLimitProperties properties,
            HeiSecurityProperties securityProperties) {
        this.redissonClient = redissonClient;
        this.properties = properties;
        this.securityProperties = securityProperties;
    }

    @Around("@annotation(rateLimit)")
    public Object around(ProceedingJoinPoint point, RateLimit rateLimit) throws Throwable {
        if (!properties.isEnabled() || redissonClient == null) {
            return point.proceed();
        }

        String key = buildKey(point, rateLimit);
        RRateLimiter limiter = redissonClient.getRateLimiter("hei:rl:" + key);
        limiter.trySetRate(
                RateType.OVERALL,
                rateLimit.permits(),
                Math.max(1, rateLimit.windowSeconds()),
                RateIntervalUnit.SECONDS);

        if (!limiter.tryAcquire()) {
            log.warn("Rate limited key={}", key);
            writeTooManyRequests(rateLimit.windowSeconds());
            return null;
        }
        return point.proceed();
    }

    private String buildKey(ProceedingJoinPoint point, RateLimit rateLimit) {
        String base = rateLimit.key();
        if (!StringUtils.hasText(base)) {
            Method method = ((MethodSignature) point.getSignature()).getMethod();
            base = method.getDeclaringClass().getSimpleName() + "." + method.getName();
        }
        String subject = resolveSubject();
        return subject + ":" + base;
    }

    private String resolveSubject() {
        ServletRequestAttributes attrs = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attrs == null) {
            return "anon";
        }
        HttpServletRequest request = attrs.getRequest();
        Object loginId = null;
        try {
            if (StpKit.ADMIN.isLogin()) {
                loginId = StpKit.ADMIN.getLoginIdDefaultNull();
            } else if (StpKit.PORTAL.isLogin()) {
                loginId = StpKit.PORTAL.getLoginIdDefaultNull();
            }
        } catch (Exception ignored) {
            // optional
        }
        if (loginId != null && StringUtils.hasText(String.valueOf(loginId))) {
            return "u:" + loginId;
        }
        String ip = ClientIpResolver.resolve(request, securityProperties.isTrustForwardedHeaders());
        return "ip:" + ip;
    }

    private void writeTooManyRequests(int windowSeconds) throws Exception {
        ServletRequestAttributes attrs = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attrs == null) {
            return;
        }
        HttpServletResponse response = attrs.getResponse();
        if (response == null || response.isCommitted()) {
            return;
        }
        response.setStatus(429);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setHeader("Retry-After", String.valueOf(Math.max(1, windowSeconds)));
        response.getWriter().write("{\"code\":\"429\",\"message\":\"Too Many Requests\",\"data\":null}");
    }
}
