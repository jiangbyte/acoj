package github.jiangbyte.io.sys.modules.audit.entity;

import io.swagger.v3.oas.annotations.media.Schema;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.OffsetDateTime;

/**
 * 审计 outbox 实体，对应表 sys_operation_audit_outbox。
 *
 * Author: Charlie
 */
@Schema(description = "审计 outbox 实体，对应表 sys_operation_audit_outbox。")
@Data
@TableName("sys_operation_audit_outbox")
public class SysOperationAuditOutbox {

    @TableId(type = IdType.ASSIGN_ID)
    @Schema(description = "主键ID")
    private String id;

    @Schema(description = "事件 JSON")
    /** 事件 JSON */
    private String payload;

    @Schema(description = "PENDING | CLAIMED | DONE | DEAD")
    /** PENDING | CLAIMED | DONE | DEAD */
    private String status;
    @Schema(description = "重试次数")

    private Integer attempts;
    @Schema(description = "创建时间")

    private OffsetDateTime createdAt;
    @Schema(description = "消费者认领时间")

    private OffsetDateTime claimedAt;
}
