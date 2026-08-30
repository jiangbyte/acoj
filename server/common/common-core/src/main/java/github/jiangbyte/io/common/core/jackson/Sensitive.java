package github.jiangbyte.io.common.core.jackson;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 标记 HTTP JSON 响应中需脱敏的字段。
 *
 * <p>示例：
 * <pre>{@code
 * @Sensitive                              // 整段 → ***
 * @Sensitive(strategy = RANGE, from = 3, to = 7)  // 同 substring(3,7)
 * @Sensitive(strategy = KEEP, keepPrefix = 3, keepSuffix = 4)
 * }</pre>
 *
 * Author: Charlie
 */
@Documented
@Target({ElementType.FIELD, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface Sensitive {

    /** 脱敏策略，默认整段掩码。 */
    SensitiveStrategy strategy() default SensitiveStrategy.ALL;

    /**
     * {@link SensitiveStrategy#RANGE}：起始下标（含），同 {@link String#substring(int, int)}。
     */
    int from() default 0;

    /**
     * {@link SensitiveStrategy#RANGE}：结束下标（不含）；{@code < 0} 表示直到字符串末尾。
     */
    int to() default -1;

    /** {@link SensitiveStrategy#KEEP}：保留前缀长度。 */
    int keepPrefix() default 0;

    /** {@link SensitiveStrategy#KEEP}：保留后缀长度。 */
    int keepSuffix() default 0;

    /** 区间/保留策略使用的掩码字符。 */
    char maskChar() default '*';

    /** {@link SensitiveStrategy#ALL} 时的整段替换文本。 */
    String mask() default "***";
}
