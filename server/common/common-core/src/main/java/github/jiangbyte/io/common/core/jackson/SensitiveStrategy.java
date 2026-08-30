package github.jiangbyte.io.common.core.jackson;

/**
 * HTTP 响应字段脱敏策略。
 *
 * Author: Charlie
 */
public enum SensitiveStrategy {

    /** 整段替换为固定掩码（默认 {@code ***}）。 */
    ALL,

    /**
     * 按 {@link Sensitive#from()} / {@link Sensitive#to()} 区间脱敏，语义同 {@link String#substring(int, int)}：
     * 0 起算、含头不含尾；{@code to &lt; 0} 表示直到末尾。
     */
    RANGE,

    /** 保留前后若干位，中间用掩码字符填充。 */
    KEEP
}
