package github.jiangbyte.io.sys.modules.audit.outbox;

import github.jiangbyte.io.common.mybatis.dialect.DbDialect;
import github.jiangbyte.io.sys.modules.audit.entity.SysOperationAuditOutbox;
import github.jiangbyte.io.sys.modules.audit.mapper.SysOperationAuditOutboxMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.time.OffsetDateTime;
import java.util.List;

/**
 * 审计 outbox 认领适配：按方言选择 RETURNING 或 SELECT+UPDATE。
 *
 * Author: Charlie
 */
@Component
@RequiredArgsConstructor
public class AuditOutboxClaimSupport {

    private final DbDialect dbDialect;
    private final SysOperationAuditOutboxMapper outboxMapper;

    public List<SysOperationAuditOutbox> claimBatch(int limit, OffsetDateTime staleBefore) {
        if (dbDialect.isPostgresql()) {
            return outboxMapper.claimBatchReturning(limit, staleBefore);
        }
        List<String> ids = outboxMapper.selectClaimIds(limit, staleBefore);
        if (CollectionUtils.isEmpty(ids)) {
            return List.of();
        }
        outboxMapper.markClaimed(ids);
        return outboxMapper.selectClaimedByIds(ids);
    }
}
