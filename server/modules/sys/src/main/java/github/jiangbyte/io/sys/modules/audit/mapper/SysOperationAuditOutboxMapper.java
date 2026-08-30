package github.jiangbyte.io.sys.modules.audit.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import github.jiangbyte.io.sys.modules.audit.entity.SysOperationAuditOutbox;
import org.apache.ibatis.annotations.Lang;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.SelectProvider;
import org.apache.ibatis.annotations.UpdateProvider;
import org.apache.ibatis.scripting.xmltags.XMLLanguageDriver;

import java.time.OffsetDateTime;
import java.util.List;

/**
 * 审计 outbox 表 Mapper。
 *
 * Author: Charlie
 */
@Mapper
public interface SysOperationAuditOutboxMapper extends BaseMapper<SysOperationAuditOutbox> {

    /**
     * PostgreSQL：UPDATE … RETURNING 认领一批。
     */
    @SelectProvider(type = SysOperationAuditOutboxSqlProvider.class, method = "claimBatchReturning")
    List<SysOperationAuditOutbox> claimBatchReturning(
            @Param("limit") int limit,
            @Param("staleBefore") OffsetDateTime staleBefore);

    /**
     * MySQL：挑选待认领 id（FOR UPDATE SKIP LOCKED）。
     */
    @SelectProvider(type = SysOperationAuditOutboxSqlProvider.class, method = "selectClaimIds")
    List<String> selectClaimIds(
            @Param("limit") int limit,
            @Param("staleBefore") OffsetDateTime staleBefore);

    /**
     * MySQL：按 id 标记认领。
     */
    @Lang(XMLLanguageDriver.class)
    @UpdateProvider(type = SysOperationAuditOutboxSqlProvider.class, method = "markClaimed")
    int markClaimed(@Param("ids") List<String> ids);

    @Lang(XMLLanguageDriver.class)
    @Select("""
            <script>
            SELECT id, payload, status, attempts, created_at, claimed_at
            FROM sys_operation_audit_outbox
            WHERE id IN
            <foreach collection="ids" item="id" open="(" separator="," close=")">
              #{id}
            </foreach>
            </script>
            """)
    List<SysOperationAuditOutbox> selectClaimedByIds(@Param("ids") List<String> ids);
}
