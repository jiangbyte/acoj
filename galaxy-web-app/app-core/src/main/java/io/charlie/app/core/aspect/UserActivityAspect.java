package io.charlie.app.core.aspect;

import io.charlie.app.core.annotation.UserActivity;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import cn.dev33.satoken.stp.StpUtil;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.TimeUnit;

/**
 * 用户活跃度切面处理
 */
@Slf4j
@Aspect
@Component
public class UserActivityAspect {
    
    private final RedisTemplate<String, Object> redisTemplate;
    
    public UserActivityAspect(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }
    
    @Pointcut("@annotation(io.charlie.app.core.annotation.UserActivity)")
    public void userActivityPointcut() {}
    
    @Around("userActivityPointcut()")
    public Object recordUserActivity(ProceedingJoinPoint joinPoint) throws Throwable {
        // 获取注解信息
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        UserActivity userActivity = signature.getMethod().getAnnotation(UserActivity.class);
        
        // 执行原方法
        Object result = joinPoint.proceed();
        
        // 记录活跃度
        try {
            if (StpUtil.isLogin()) {
                String userId = StpUtil.getLoginIdAsString();
                String today = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
                log.info("用户 {} 访问 {}", userId, joinPoint.getSignature().getName());

                // 构建每日key
                String dailyKey = userActivity.keyPrefix() + userId + ":" + today;
                String globalRankKey = userActivity.globalRankKey();

                // 记录每日活跃度
                redisTemplate.opsForZSet().incrementScore(
                        dailyKey,
                        userActivity.type(),
                        userActivity.score()
                );
                redisTemplate.expire(dailyKey, userActivity.expireDays(), TimeUnit.DAYS);

                // 同时更新全局排行榜（30天累计）
                redisTemplate.opsForZSet().incrementScore(
                        globalRankKey,
                        userId,
                        userActivity.score()
                );

                // 设置全局排行榜的过期时间
                redisTemplate.expire(globalRankKey, userActivity.expireDays() + 1, TimeUnit.DAYS);

//                // 构建Redis key
//                String key = userActivity.keyPrefix() + userId + ":" + today;
//
//                // 记录活跃度分数
//                redisTemplate.opsForZSet().incrementScore(
//                    key,
//                    userActivity.type(),
//                    userActivity.score()
//                );
//
//                // 设置过期时间
//                redisTemplate.expire(key, userActivity.expireDays(), TimeUnit.DAYS);
            }
        } catch (Exception e) {
            // 活跃度记录失败不应影响主流程
            e.printStackTrace();
        }
        
        return result;
    }
}