package github.jiangbyte.io.workspace.modules.overview.entity;

import io.swagger.v3.oas.annotations.media.Schema;
import com.baomidou.mybatisplus.annotation.TableName;
import github.jiangbyte.io.common.core.domain.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 工作台只读审计日志投影，对应表 sys_operation_audit_log。
 *
 * Author: Charlie
 */
@Schema(description = "工作台只读审计日志投影，对应表 sys_operation_audit_log。")
@Data
@EqualsAndHashCode(callSuper = true)
@TableName(value = "sys_operation_audit_log", excludeProperty = {"createdBy", "updatedAt", "updatedBy"})
public class WorkspaceAuditLog extends BaseEntity {
    @Schema(description = "业务模块编码（如 sys、iam）")
    private String module;
    @Schema(description = "动作")
    private String action;
    @Schema(description = "操作内容可读摘要")
    private String summary;
    @Schema(description = "操作人账户ID")
    private String accountId;
    @Schema(description = "操作人账户类型：ADMIN/PORTAL")
    private String accountType;
    @Schema(description = "客户端/实例 IP 地址")
    private String ip;
    @Schema(description = "客户端 User-Agent")
    private String userAgent;
    @Schema(description = "是否成功：1 成功 / 0 失败")
    private Boolean success;
    @Schema(description = "操作人昵称快照（写入时落库）")
    private String operatorName;
    @Schema(description = "操作名称（前端展示）")
    private String actionName;
    @Schema(description = "操作类型：CREATE/UPDATE/DELETE/QUERY/EXPORT/LOGIN/LOGOUT/OTHER")
    private String actionType;
    @Schema(description = "操作模块中文展示名")
    private String moduleLabel;
    @Schema(description = "耗时（毫秒）")
    private Integer durationMs;
    @Schema(description = "被操作资源主键ID")
    private String resourceId;
}
