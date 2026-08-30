package github.jiangbyte.io.iam.modules.role.param;

import io.swagger.v3.oas.annotations.media.Schema;
import github.jiangbyte.io.common.core.domain.PageQuery;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 角色分页查询入参。
 *
 * Author: Charlie
 */
@Schema(description = "角色分页查询入参。")
@Data
@EqualsAndHashCode(callSuper = true)
public class SysRolePageParam extends PageQuery {
    @Schema(description = "编码")

    private String code;
    @Schema(description = "名称")
    private String name;
    @Schema(description = "角色分类")
    private String category;
    @Schema(description = "角色作用域：GLOBAL/DEPT 等")
    private String scopeType;
    @Schema(description = "角色状态：ENABLED/DISABLED")
    private String status;
}
