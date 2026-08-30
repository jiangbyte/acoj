package github.jiangbyte.io.common.log.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 操作审计注解：标记需异步发布审计事件的业务方法（资源类型与动作）。
 *
 * Author: Charlie
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface OperationAudit {

    /** 审计资源类型标识。 */
    String resourceType();

    /** 审计动作；为空时可由切面推导。 */
    String action() default "";

    /** 操作名（展示）；为空时由 catalog 推导。 */
    String name() default "";

    /**
     * 操作类型：CREATE/UPDATE/DELETE/QUERY/EXPORT/LOGIN/LOGOUT/OTHER；
     * 为空时由 action 推断。
     */
    String actionType() default "";
}
