package github.jiangbyte.io.sys.modules.audit.param;

import io.swagger.v3.oas.annotations.media.Schema;
import github.jiangbyte.io.common.core.domain.PageQuery;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 操作审计分页查询入参。
 *
 * Author: Charlie
 */
@Schema(description = "操作审计分页查询入参。")
@Data
@EqualsAndHashCode(callSuper = true)
public class SysAuditPageParam extends PageQuery {
    @Schema(description = "module")
    private String module;
    @Schema(description = "动作")
    private String action;
    @Schema(description = "排除指定 action（如 login），与 action 互斥优先用 action。")
    /** 排除指定 action（如 login），与 action 互斥优先用 action。 */
    private String excludeAction;
    @Schema(description = "账户ID")
    private String accountId;
    @Schema(description = "是否成功：1 成功 / 0 失败")
    private Boolean success;
}
