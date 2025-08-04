package io.charlie.app.core.annotation;

import java.lang.annotation.*;

/**
 * 用户活跃度记录注解
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface UserActivity {
    /**
     * 活跃度分数（默认1分）
     */
    int score() default 1;
    
    /**
     * 活跃类型（用于区分不同行为）
     */
    String type() default "general";
    
    /**
     * Redis key前缀
     */
    String keyPrefix() default "user:activity:";
    
    /**
     * 过期时间（天），默认30天
     */
    int expireDays() default 30;
}