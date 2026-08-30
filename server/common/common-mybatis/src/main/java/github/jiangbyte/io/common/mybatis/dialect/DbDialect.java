package github.jiangbyte.io.common.mybatis.dialect;

import com.baomidou.mybatisplus.annotation.DbType;

/**
 * 数据库方言：分页类型、JSON 谓词片段、以及各热点原生 SQL。
 *
 * Author: Charlie
 */
public interface DbDialect {

    DbVendor vendor();

    DbType mybatisPlusDbType();

    /**
     * MyBatis-Plus {@code .apply(sql, arg)} 用：JSON 字符串数组是否包含占位符 {@code {0}}。
     *
     * @param column 列名（可带表别名，如 {@code n.target_account_types}）
     */
    String jsonArrayContainsApply(String column);

    /**
     * 列为空数组或包含 {@code {0}}（用于账号类型可见性）。
     */
    String jsonArrayEmptyOrContainsApply(String column);

    /** 脚本 SQL 中：列为空数组或包含命名参数（如 {@code #{accountType}}）。 */
    String jsonArrayEmptyOrContainsNamed(String column, String namedParam);

    /** 脚本 SQL 中：列包含命名参数。 */
    String jsonArrayContainsNamed(String column, String namedParam);

    /** Outbox 认领（PostgreSQL 单条 RETURNING；MySQL 由调用方走多步时此 SQL 可为空）。 */
    String outboxClaimReturningSql();

    /** MySQL outbox：挑选待认领 id。 */
    String outboxSelectClaimIdsSql();

    /** MySQL outbox：按 id 列表标记认领。 */
    String outboxMarkClaimedSql();

    /** 按日聚合表达式：SELECT 列表中的 day 列（别名 day）。 */
    String dailyBucketSelect(String timestampColumn);

    /** 按日聚合 GROUP BY 表达式（与 {@link #dailyBucketSelect} 一致）。 */
    String dailyBucketGroupBy(String timestampColumn);

    /** Codegen：列出当前 schema 下用户表。 */
    String codegenListTablesSql();

    /** Codegen：列出表列。 */
    String codegenListColumnsSql();

    /** Codegen：列出主键列。 */
    String codegenListPrimaryKeysSql();

    default boolean isPostgresql() {
        return vendor() == DbVendor.POSTGRESQL;
    }

    default boolean isMysql() {
        return vendor() == DbVendor.MYSQL;
    }
}
