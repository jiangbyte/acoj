package io.charlie.web.oj.annotation.log;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Log {
    String value() default ""; // 操作描述
    String category() default ""; // 操作分类
    String module() default ""; // 模块名称
    String description() default "";
}