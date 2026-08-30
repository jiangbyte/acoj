package github.jiangbyte.io.common.security.ratelimit;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 方法级限流（Redisson 滑动窗口 / 简易计数）。
 *
 * Author: Charlie
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface RateLimit {

    /** 限流业务键；空则使用类名.方法名。 */
    String key() default "";

    /** 窗口内最大请求数。 */
    int permits() default 60;

    /** 窗口秒数。 */
    int windowSeconds() default 60;
}
