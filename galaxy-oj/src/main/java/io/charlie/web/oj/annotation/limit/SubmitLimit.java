package io.charlie.web.oj.annotation.limit;

import java.lang.annotation.*;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface SubmitLimit {
    
    /**
     * 限制时间，单位：秒
     */
    int limit() default 1;
    
    /**
     * 提示信息
     */
    String message() default "操作过于频繁，请稍后再试";
    
    /**
     * 限流键，支持SpEL表达式
     */
    String key() default "";
}