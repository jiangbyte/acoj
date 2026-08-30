package github.jiangbyte.io.common.mybatis.dialect;

import com.baomidou.mybatisplus.annotation.DbType;

/**
 * PostgreSQL 方言（列类型 json，不使用 jsonb）。
 *
 * Author: Charlie
 */
public final class PostgreSqlDialect implements DbDialect {

    @Override
    public DbVendor vendor() {
        return DbVendor.POSTGRESQL;
    }

    @Override
    public DbType mybatisPlusDbType() {
        return DbType.POSTGRE_SQL;
    }

    @Override
    public String jsonArrayContainsApply(String column) {
        String col = SqlSafe.requireIdent(column);
        return "EXISTS (SELECT 1 FROM json_array_elements_text(COALESCE(" + col
                + ", '[]'::json)) AS _hei_json_elem WHERE _hei_json_elem = {0})";
    }

    @Override
    public String jsonArrayEmptyOrContainsApply(String column) {
        String col = SqlSafe.requireIdent(column);
        return "(json_array_length(COALESCE(" + col + ", '[]'::json)) = 0 OR EXISTS ("
                + "SELECT 1 FROM json_array_elements_text(COALESCE(" + col
                + ", '[]'::json)) AS _hei_json_elem WHERE _hei_json_elem = {0}))";
    }

    @Override
    public String jsonArrayEmptyOrContainsNamed(String column, String namedParam) {
        String col = SqlSafe.requireIdent(column);
        return "(json_array_length(COALESCE(" + col + ", '[]'::json)) = 0 OR EXISTS ("
                + "SELECT 1 FROM json_array_elements_text(COALESCE(" + col
                + ", '[]'::json)) AS _hei_json_elem WHERE _hei_json_elem = #{" + namedParam + "}))";
    }

    @Override
    public String jsonArrayContainsNamed(String column, String namedParam) {
        String col = SqlSafe.requireIdent(column);
        return "EXISTS (SELECT 1 FROM json_array_elements_text(COALESCE(" + col
                + ", '[]'::json)) AS _hei_json_elem WHERE _hei_json_elem = #{" + namedParam + "})";
    }

    @Override
    public String outboxClaimReturningSql() {
        return """
                UPDATE sys_operation_audit_outbox
                SET status = 'CLAIMED',
                    claimed_at = now(),
                    attempts = attempts + 1
                WHERE id IN (
                    SELECT id FROM sys_operation_audit_outbox
                    WHERE status = 'PENDING'
                       OR (status = 'CLAIMED' AND claimed_at < #{staleBefore})
                    ORDER BY created_at
                    LIMIT #{limit}
                    FOR UPDATE SKIP LOCKED
                )
                RETURNING id, payload, status, attempts, created_at, claimed_at
                """;
    }

    @Override
    public String outboxSelectClaimIdsSql() {
        throw new UnsupportedOperationException("PostgreSQL uses outboxClaimReturningSql");
    }

    @Override
    public String outboxMarkClaimedSql() {
        throw new UnsupportedOperationException("PostgreSQL uses outboxClaimReturningSql");
    }

    @Override
    public String dailyBucketSelect(String timestampColumn) {
        // 用 timezone() 而非 AT TIME ZONE，避免 Druid Wall 误报语法/注入
        return "TO_CHAR(timezone('Asia/Shanghai', " + timestampColumn + "), 'YYYY-MM-DD') AS day";
    }

    @Override
    public String dailyBucketGroupBy(String timestampColumn) {
        return "TO_CHAR(timezone('Asia/Shanghai', " + timestampColumn + "), 'YYYY-MM-DD')";
    }

    @Override
    public String codegenListTablesSql() {
        return """
                SELECT c.relname AS table_name,
                       COALESCE(obj_description(c.oid), '') AS table_comment
                FROM pg_class c
                JOIN pg_namespace n ON n.oid = c.relnamespace
                WHERE n.nspname = current_schema()
                  AND c.relkind = 'r'
                  AND c.relname NOT IN ('flyway_schema_history', 'sys_codegen_plan', 'sys_codegen_field')
                ORDER BY c.relname
                """;
    }

    @Override
    public String codegenListColumnsSql() {
        return """
                SELECT c.column_name,
                       COALESCE(pgd.description, '') AS column_comment,
                       c.data_type,
                       c.udt_name,
                       c.character_maximum_length AS max_length,
                       c.is_nullable,
                       c.ordinal_position AS sort
                FROM information_schema.columns c
                LEFT JOIN pg_catalog.pg_statio_all_tables st
                  ON st.schemaname = c.table_schema AND st.relname = c.table_name
                LEFT JOIN pg_catalog.pg_description pgd
                  ON pgd.objoid = st.relid AND pgd.objsubid = c.ordinal_position
                WHERE c.table_schema = current_schema()
                  AND c.table_name = #{tableName}
                ORDER BY c.ordinal_position
                """;
    }

    @Override
    public String codegenListPrimaryKeysSql() {
        return """
                SELECT kcu.column_name
                FROM information_schema.table_constraints tc
                JOIN information_schema.key_column_usage kcu
                  ON tc.constraint_name = kcu.constraint_name
                 AND tc.table_schema = kcu.table_schema
                WHERE tc.table_schema = current_schema()
                  AND tc.table_name = #{tableName}
                  AND tc.constraint_type = 'PRIMARY KEY'
                """;
    }
}
