package github.jiangbyte.io.sys.modules.notice.mapper;

import github.jiangbyte.io.common.mybatis.dialect.DbDialect;
import github.jiangbyte.io.common.mybatis.dialect.DbDialectHolder;

/**
 * 通知可见性 SQL Provider（按当前 DbDialect 拼 JSON 谓词）。
 *
 * Author: Charlie
 */
public final class SysNoticeSqlProvider {

    private SysNoticeSqlProvider() {
    }

    public static String countUnread(java.util.Map<String, Object> params) {
        return visibilitySelect("SELECT COUNT(1)", false);
    }

    public static String listVisiblePublishedIdsPage(java.util.Map<String, Object> params) {
        return visibilitySelect("SELECT n.id", true);
    }

    public static String listVisiblePublishedIds(java.util.Map<String, Object> params) {
        return visibilitySelect("SELECT n.id", false);
    }

    private static String visibilitySelect(String selectClause, boolean page) {
        DbDialect dialect = DbDialectHolder.get();
        String typesPred = dialect.jsonArrayEmptyOrContainsNamed("n.target_account_types", "accountType");
        String idsPred = dialect.jsonArrayContainsNamed("n.target_account_ids", "accountId");
        StringBuilder sql = new StringBuilder();
        sql.append("<script>\n");
        sql.append(selectClause).append('\n');
        sql.append("""
                FROM sys_notice n
                WHERE n.status = 'PUBLISHED'
                  AND (n.kind &lt;&gt; 'ANNOUNCEMENT' OR n.expire_at IS NULL OR n.expire_at &gt; #{now})
                  AND (
                        (
                            n.target_scope IN ('ALL', 'ACCOUNT_TYPE')
                            AND (
                                n.target_account_types IS NULL
                                OR %s
                            )
                        )
                        <if test="accountId != null and accountId != ''">
                        OR (
                            n.target_scope = 'SPECIFIC'
                            AND %s
                        )
                        </if>
                  )
                """.formatted(typesPred, idsPred));
        if (selectClause.contains("COUNT")) {
            sql.append("""
                      AND NOT EXISTS (
                            SELECT 1
                            FROM sys_notice_read r
                            WHERE r.notice_id = n.id
                              AND r.account_type = #{accountType}
                              AND r.account_id = #{accountId}
                      )
                    """);
        }
        sql.append("""
                  <if test="kind != null and kind != ''">
                  AND n.kind = #{kind}
                  </if>
                """);
        if (page) {
            sql.append("""
                    ORDER BY n.id
                    LIMIT #{limit} OFFSET #{offset}
                    """);
        }
        sql.append("</script>");
        return sql.toString();
    }
}
