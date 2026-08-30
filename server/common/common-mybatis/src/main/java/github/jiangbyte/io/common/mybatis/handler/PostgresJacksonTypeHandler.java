package github.jiangbyte.io.common.mybatis.handler;

import java.lang.reflect.Field;

/**
 * 兼容旧名；请改用 {@link JacksonJsonTypeHandler}。
 *
 * Author: Charlie
 * @deprecated 使用 {@link JacksonJsonTypeHandler}
 */
@Deprecated
public class PostgresJacksonTypeHandler extends JacksonJsonTypeHandler {

    public PostgresJacksonTypeHandler(Class<?> type) {
        super(type);
    }

    public PostgresJacksonTypeHandler(Class<?> type, Field field) {
        super(type, field);
    }
}
