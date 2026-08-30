package github.jiangbyte.io.sys.modules.audit.mapper;

import github.jiangbyte.io.common.mybatis.dialect.DbDialectHolder;

import java.util.Map;

/**
 * 审计 outbox 认领 SQL Provider。
 *
 * Author: Charlie
 */
public final class SysOperationAuditOutboxSqlProvider {

    private SysOperationAuditOutboxSqlProvider() {
    }

    public static String claimBatchReturning(Map<String, Object> params) {
        return DbDialectHolder.get().outboxClaimReturningSql();
    }

    public static String selectClaimIds(Map<String, Object> params) {
        return DbDialectHolder.get().outboxSelectClaimIdsSql();
    }

    public static String markClaimed(Map<String, Object> params) {
        return "<script>\n" + DbDialectHolder.get().outboxMarkClaimedSql() + "\n</script>";
    }
}
