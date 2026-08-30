package github.jiangbyte.io.sys.modules.audit.entity;

import io.swagger.v3.oas.annotations.media.Schema;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import github.jiangbyte.io.common.mybatis.handler.JacksonJsonTypeHandler;
import github.jiangbyte.io.common.core.domain.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Map;

/**
 * 操作审计日志实体，对应表 sys_operation_audit_log。
 *
 * Author: Charlie
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName(
        value = "sys_operation_audit_log",
        autoResultMap = true,
        excludeProperty = {"createdBy", "updatedAt", "updatedBy"})
@Schema(description = "操作审计日志")
public class SysOperationAuditLog extends BaseEntity {
    @Schema(description = "业务模块编码（如 sys、iam）")
    private String module;
    @Schema(description = "资源类型编码（如 SysAccount）")
    private String resourceType;
    @Schema(description = "被操作资源主键ID")
    private String resourceId;
    @Schema(description = "动作")
    private String action;
    @Schema(description = "操作内容（可读摘要）")
    /** 操作内容（可读摘要） */
    private String summary;
    @TableField(typeHandler = JacksonJsonTypeHandler.class)
    @Schema(description = "变更前数据（JSON）")
    private Map<String, Object> beforeData;
    @TableField(typeHandler = JacksonJsonTypeHandler.class)
    @Schema(description = "变更后数据（JSON）")
    private Map<String, Object> afterData;
    @Schema(description = "操作人账户ID")
    private String accountId;
    @Schema(description = "操作人账户类型：ADMIN/PORTAL")
    private String accountType;
    @Schema(description = "请求链路 ID（Trace）")
    private String requestId;
    @Schema(description = "客户端/实例 IP 地址")
    private String ip;
    @Schema(description = "客户端 User-Agent")
    private String userAgent;
    @Schema(description = "是否成功：1 成功 / 0 失败")
    private Boolean success;
    @Schema(description = "错误信息")
    private String errorMessage;
    @Schema(description = "操作人昵称快照（写入时落库。")
    /** 操作人昵称快照（写入时落库；历史数据查询时批量回显） */
    private String operatorName;
    @Schema(description = "操作名")
    /** 操作名 */
    private String actionName;
    @Schema(description = "操作类型：CREATE/UPDATE/DELETE/QUERY/EXPORT/LOGIN/LOGOUT/OTHER")
    /** 操作类型：CREATE/UPDATE/DELETE/QUERY/EXPORT/LOGIN/LOGOUT/OTHER */
    private String actionType;
    @Schema(description = "操作模块展示名")
    /** 操作模块展示名 */
    private String moduleLabel;
    @Schema(description = "执行时长（毫秒）")
    /** 执行时长（毫秒） */
    private Integer durationMs;
}
