package github.jiangbyte.io.common.mybatis.dialect;

/**
 * 受支持的数据库厂商（部署时二选一）。
 *
 * Author: Charlie
 */
public enum DbVendor {
    POSTGRESQL,
    MYSQL;

    /** Freemarker / 文档用小写标识。 */
    public String code() {
        return name().toLowerCase();
    }
}
