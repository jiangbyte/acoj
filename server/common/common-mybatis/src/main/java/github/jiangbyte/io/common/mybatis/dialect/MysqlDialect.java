package github.jiangbyte.io.common.mybatis.dialect;

import com.baomidou.mybatisplus.annotation.DbType;

/**
 * MySQL 8 方言。
 *
 * Author: Charlie
 */
public final class MysqlDialect implements DbDialect {

    @Override
    public DbVendor vendor() {
        return DbVendor.MYSQL;
    }

    @Override
    public DbType mybatisPlusDbType() {
        return DbType.MYSQL;
    }

    @Override
    public String jsonArrayContainsApply(String column) {
        String col = SqlSafe.requireIdent(column);
        // JSON 字符串数组包含：JSON_CONTAINS(col, '"value"', '$')
        return "JSON_CONTAINS(COALESCE(" + col + ", JSON_ARRAY()), JSON_QUOTE({0}), '$')";
    }

    @Override
    public String jsonArrayEmptyOrContainsApply(String column) {
        String col = SqlSafe.requireIdent(column);
        return "(JSON_LENGTH(COALESCE(" + col + ", JSON_ARRAY())) = 0 OR JSON_CONTAINS(COALESCE("
                + col + ", JSON_ARRAY()), JSON_QUOTE({0}), '$'))";
    }

    @Override
    public String jsonArrayEmptyOrContainsNamed(String column, String namedParam) {
        String col = SqlSafe.requireIdent(column);
        return "(JSON_LENGTH(COALESCE(" + col + ", JSON_ARRAY())) = 0 OR JSON_CONTAINS(COALESCE("
                + col + ", JSON_ARRAY()), JSON_QUOTE(#{" + namedParam + "}), '$'))";
    }

    @Override
    public String jsonArrayContainsNamed(String column, String namedParam) {
        String col = SqlSafe.requireIdent(column);
        return "JSON_CONTAINS(COALESCE(" + col + ", JSON_ARRAY()), JSON_QUOTE(#{" + namedParam + "}), '$')";
    }

    @Override
    public String outboxClaimReturningSql() {
        throw new UnsupportedOperationException("MySQL uses multi-step outbox claim");
    }

    @Override
    public String outboxSelectClaimIdsSql() {
        return """
                SELECT id FROM sys_operation_audit_outbox
                WHERE status = 'PENDING'
                   OR (status = 'CLAIMED' AND claimed_at < #{staleBefore})
                ORDER BY created_at
                LIMIT #{limit}
                FOR UPDATE SKIP LOCKED
                """;
    }

    @Override
    public String outboxMarkClaimedSql() {
        return """
                UPDATE sys_operation_audit_outbox
                SET status = 'CLAIMED',
                    claimed_at = UTC_TIMESTAMP(6),
                    attempts = attempts + 1
                WHERE id IN
                <foreach collection="ids" item="id" open="(" separator="," close=")">
                  #{id}
                </foreach>
                """;
    }

    @Override
    public String dailyBucketSelect(String timestampColumn) {
        return "DATE_FORMAT(CONVERT_TZ(" + timestampColumn + ", '+00:00', '+08:00'), '%Y-%m-%d') AS day";
    }

    @Override
    public String dailyBucketGroupBy(String timestampColumn) {
        return "DATE_FORMAT(CONVERT_TZ(" + timestampColumn + ", '+00:00', '+08:00'), '%Y-%m-%d')";
    }

    @Override
    public String codegenListTablesSql() {
        return """
                SELECT t.table_name AS table_name,
                       COALESCE(t.table_comment, '') AS table_comment
                FROM information_schema.tables t
                WHERE t.table_schema = DATABASE()
                  AND t.table_type = 'BASE TABLE'
                  AND t.table_name NOT IN ('flyway_schema_history', 'sys_codegen_plan', 'sys_codegen_field')
                ORDER BY t.table_name
                """;
    }

    @Override
    public String codegenListColumnsSql() {
        return """
                SELECT c.column_name AS column_name,
                       COALESCE(c.column_comment, '') AS column_comment,
                       c.data_type AS data_type,
                       c.column_type AS udt_name,
                       c.character_maximum_length AS max_length,
                       c.is_nullable AS is_nullable,
                       c.ordinal_position AS sort
                FROM information_schema.columns c
                WHERE c.table_schema = DATABASE()
                  AND c.table_name = #{tableName}
                ORDER BY c.ordinal_position
                """;
    }

    @Override
    public String codegenListPrimaryKeysSql() {
        return """
                SELECT kcu.column_name AS column_name
                FROM information_schema.table_constraints tc
                JOIN information_schema.key_column_usage kcu
                  ON tc.constraint_name = kcu.constraint_name
                 AND tc.table_schema = kcu.table_schema
                 AND tc.table_name = kcu.table_name
                WHERE tc.table_schema = DATABASE()
                  AND tc.table_name = #{tableName}
                  AND tc.constraint_type = 'PRIMARY KEY'
                ORDER BY kcu.ordinal_position
                """;
    }
}
