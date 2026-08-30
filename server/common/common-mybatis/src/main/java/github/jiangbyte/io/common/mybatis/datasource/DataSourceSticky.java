package github.jiangbyte.io.common.mybatis.datasource;

/**
 * 请求级写后粘主：本请求后续 {@link ReadDataSource} 读不再切从库。
 *
 * Author: Charlie
 */
public final class DataSourceSticky {

    private static final ThreadLocal<Boolean> STICKY = new ThreadLocal<>();

    private DataSourceSticky() {
    }

    /** 标记本请求后续读走主库。 */
    public static void mark() {
        STICKY.set(Boolean.TRUE);
    }

    /** 是否已粘主。 */
    public static boolean isSticky() {
        return Boolean.TRUE.equals(STICKY.get());
    }

    /** 请求结束清理。 */
    public static void clear() {
        STICKY.remove();
    }
}
