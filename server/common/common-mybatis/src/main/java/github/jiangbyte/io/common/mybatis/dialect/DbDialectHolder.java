package github.jiangbyte.io.common.mybatis.dialect;

/**
 * 供 MyBatis Provider / TypeHandler 等非 Spring 托管入口读取当前方言。
 *
 * Author: Charlie
 */
public final class DbDialectHolder {

    private static volatile DbDialect dialect;

    private DbDialectHolder() {
    }

    public static void set(DbDialect value) {
        dialect = value;
    }

    public static DbDialect get() {
        DbDialect current = dialect;
        if (current == null) {
            throw new IllegalStateException("DbDialect has not been initialized");
        }
        return current;
    }

    public static boolean isReady() {
        return dialect != null;
    }
}
