package github.jiangbyte.io.admin.dialect;

import github.jiangbyte.io.common.mybatis.dialect.DbDialect;
import github.jiangbyte.io.common.mybatis.dialect.DbDialectDetector;
import github.jiangbyte.io.common.mybatis.dialect.DbDialectHolder;
import github.jiangbyte.io.common.mybatis.dialect.DbVendor;
import github.jiangbyte.io.common.mybatis.dialect.MysqlDialect;
import github.jiangbyte.io.common.mybatis.dialect.PostgreSqlDialect;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 方言冒烟：需真实库。CI 通过 {@code -Dhei.dialect.smoke=true} 与 DB_* 环境变量启用。
 *
 * Author: Charlie
 */
@EnabledIfSystemProperty(named = "hei.dialect.smoke", matches = "true")
class DialectSmokeIT {

    private static Connection connection;
    private static DbDialect dialect;

    @BeforeAll
    static void open() throws Exception {
        String url = requiredEnv("DB_WRITE_URL");
        String user = envOr("DB_WRITE_USERNAME", "postgres");
        String password = envOr("DB_WRITE_PASSWORD", "123456");
        DbVendor vendor = DbDialectDetector.require(url, null);
        dialect = vendor == DbVendor.MYSQL ? new MysqlDialect() : new PostgreSqlDialect();
        DbDialectHolder.set(dialect);
        connection = DriverManager.getConnection(url, user, password);
    }

    @AfterAll
    static void close() throws Exception {
        if (connection != null) {
            connection.close();
        }
    }

    @Test
    void dialectMatchesJdbcUrl() throws Exception {
        String product = connection.getMetaData().getDatabaseProductName();
        assertEquals(dialect.vendor(), DbDialectDetector.require(requiredEnv("DB_WRITE_URL"), product));
    }

    @Test
    void connectivityAndCodegenListTables() throws Exception {
        try (Statement st = connection.createStatement();
             ResultSet rs = st.executeQuery("SELECT 1")) {
            assertTrue(rs.next());
        }
        try (Statement st = connection.createStatement();
             ResultSet rs = st.executeQuery(dialect.codegenListTablesSql())) {
            assertTrue(rs.next(), "expected at least one user table");
            assertTrue(rs.getString("table_name") != null && !rs.getString("table_name").isBlank());
        }
    }

    @Test
    void codegenListColumnsForAccount() throws Exception {
        String sql = dialect.codegenListColumnsSql().replace("#{tableName}", "'sys_account'");
        try (Statement st = connection.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            assertTrue(rs.next());
        }
    }

    @Test
    void workspaceDailyBucketSql() throws Exception {
        String sql = """
                SELECT %s, COUNT(*) AS cnt
                FROM sys_account
                WHERE created_at >= ?
                GROUP BY %s
                ORDER BY day
                """.formatted(
                dialect.dailyBucketSelect("created_at"),
                dialect.dailyBucketGroupBy("created_at"));
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setTimestamp(1, Timestamp.from(OffsetDateTime.now(ZoneOffset.UTC).minusDays(7).toInstant()));
            try (ResultSet rs = ps.executeQuery()) {
                // may be empty; just ensure SQL parses/executes
                assertFalse(rs.isClosed());
            }
        }
    }

    @Test
    void noticeJsonVisibilityPredicate() throws Exception {
        String types = dialect.jsonArrayEmptyOrContainsNamed("n.target_account_types", "accountType")
                .replace("#{accountType}", "?");
        String ids = dialect.jsonArrayContainsNamed("n.target_account_ids", "accountId")
                .replace("#{accountId}", "?");
        String sql = """
                SELECT COUNT(1) AS c
                FROM sys_notice n
                WHERE n.status = 'PUBLISHED'
                  AND (
                        (n.target_scope IN ('ALL', 'ACCOUNT_TYPE') AND (n.target_account_types IS NULL OR %s))
                        OR (n.target_scope = 'SPECIFIC' AND %s)
                  )
                """.formatted(types, ids);
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, "ADMIN");
            ps.setString(2, "1");
            try (ResultSet rs = ps.executeQuery()) {
                assertTrue(rs.next());
                assertTrue(rs.getLong(1) >= 0);
            }
        }
    }

    @Test
    void outboxClaimSql() throws Exception {
        if (dialect.isPostgresql()) {
            String sql = dialect.outboxClaimReturningSql()
                    .replace("#{staleBefore}", "?")
                    .replace("#{limit}", "?");
            try (PreparedStatement ps = connection.prepareStatement(sql)) {
                ps.setTimestamp(1, Timestamp.from(OffsetDateTime.now(ZoneOffset.UTC).minusDays(1).toInstant()));
                ps.setInt(2, 1);
                try (ResultSet rs = ps.executeQuery()) {
                    assertFalse(rs.isClosed());
                }
            }
            return;
        }
        String selectIds = dialect.outboxSelectClaimIdsSql()
                .replace("#{staleBefore}", "?")
                .replace("#{limit}", "?");
        connection.setAutoCommit(false);
        try (PreparedStatement ps = connection.prepareStatement(selectIds)) {
            ps.setTimestamp(1, Timestamp.from(OffsetDateTime.now(ZoneOffset.UTC).minusDays(1).toInstant()));
            ps.setInt(2, 1);
            try (ResultSet rs = ps.executeQuery()) {
                assertFalse(rs.isClosed());
            }
        } finally {
            connection.rollback();
            connection.setAutoCommit(true);
        }
    }

    private static String requiredEnv(String key) {
        String value = System.getenv(key);
        if (value == null || value.isBlank()) {
            throw new IllegalStateException("Missing env " + key);
        }
        return value;
    }

    private static String envOr(String key, String defaultValue) {
        String value = System.getenv(key);
        return value == null || value.isBlank() ? defaultValue : value;
    }
}
