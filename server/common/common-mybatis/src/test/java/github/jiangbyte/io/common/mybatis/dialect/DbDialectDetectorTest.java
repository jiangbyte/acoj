package github.jiangbyte.io.common.mybatis.dialect;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class DbDialectDetectorTest {

    @Test
    void detectsPostgresqlAndMysqlFromJdbcUrl() {
        assertEquals(DbVendor.POSTGRESQL, DbDialectDetector.fromJdbcUrl("jdbc:postgresql://localhost/hei"));
        assertEquals(DbVendor.MYSQL, DbDialectDetector.fromJdbcUrl("jdbc:mysql://localhost:3306/hei"));
        assertEquals(DbVendor.MYSQL, DbDialectDetector.fromJdbcUrl("jdbc:mariadb://localhost/hei"));
        assertNull(DbDialectDetector.fromJdbcUrl("jdbc:h2:mem:test"));
    }

    @Test
    void detectsFromProductName() {
        assertEquals(DbVendor.POSTGRESQL, DbDialectDetector.fromProductName("PostgreSQL"));
        assertEquals(DbVendor.MYSQL, DbDialectDetector.fromProductName("MySQL"));
    }

    @Test
    void requireFailsWhenUnknown() {
        assertThrows(IllegalStateException.class, () -> DbDialectDetector.require("jdbc:h2:mem:x", "H2"));
    }
}
