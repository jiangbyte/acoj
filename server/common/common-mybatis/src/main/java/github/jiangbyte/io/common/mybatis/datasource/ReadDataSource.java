package github.jiangbyte.io.common.mybatis.datasource;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 读库路由注解：标记方法走从库（只读）数据源。
 *
 * Author: Charlie
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface ReadDataSource {
}
