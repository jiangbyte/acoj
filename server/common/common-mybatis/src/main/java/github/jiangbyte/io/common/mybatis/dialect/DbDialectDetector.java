package github.jiangbyte.io.common.mybatis.dialect;

import org.springframework.util.StringUtils;

/**
 * 从 JDBC URL 或 DatabaseMetaData 产品名识别数据库厂商。
 *
 * Author: Charlie
 */
public final class DbDialectDetector {

    private DbDialectDetector() {
    }

    /**
     * 解析 JDBC URL；无法识别时返回 {@code null}。
     */
    public static DbVendor fromJdbcUrl(String jdbcUrl) {
        if (!StringUtils.hasText(jdbcUrl)) {
            return null;
        }
        String url = jdbcUrl.trim().toLowerCase();
        if (url.startsWith("jdbc:postgresql:") || url.startsWith("jdbc:pgsql:")) {
            return DbVendor.POSTGRESQL;
        }
        if (url.startsWith("jdbc:mysql:") || url.startsWith("jdbc:mariadb:")) {
            return DbVendor.MYSQL;
        }
        return null;
    }

    /**
     * 解析 {@link java.sql.DatabaseMetaData#getDatabaseProductName()}。
     */
    public static DbVendor fromProductName(String productName) {
        if (!StringUtils.hasText(productName)) {
            return null;
        }
        String name = productName.trim().toLowerCase();
        if (name.contains("postgresql") || name.contains("postgres")) {
            return DbVendor.POSTGRESQL;
        }
        if (name.contains("mysql") || name.contains("mariadb")) {
            return DbVendor.MYSQL;
        }
        return null;
    }

    /**
     * 先 URL 后产品名；都无法识别则抛出。
     */
    public static DbVendor require(String jdbcUrl, String productName) {
        DbVendor vendor = fromJdbcUrl(jdbcUrl);
        if (vendor == null) {
            vendor = fromProductName(productName);
        }
        if (vendor == null) {
            throw new IllegalStateException(
                    "Unsupported database: cannot detect dialect from jdbcUrl="
                            + jdbcUrl + ", productName=" + productName
                            + ". Use jdbc:postgresql: or jdbc:mysql:.");
        }
        return vendor;
    }
}
