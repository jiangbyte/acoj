package github.jiangbyte.io.iam.modules.client.param;

import io.swagger.v3.oas.annotations.media.Schema;
import github.jiangbyte.io.common.core.domain.PageQuery;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 客户端资源分页查询入参。
 *
 * Author: Charlie
 */
@Schema(description = "客户端资源分页查询入参。")
@Data
@EqualsAndHashCode(callSuper = true)
public class SysClientResourcePageParam extends PageQuery {
    @Schema(description = "编码")

    private String code;
    @Schema(description = "名称")
    private String name;
    @Schema(description = "资源类型：MENU/BUTTON/API 等")
    private String resourceType;
    @Schema(description = "所属客户端模块ID")
    private String moduleId;
    @Schema(description = "父级客户端资源ID")
    private String parentId;
    @Schema(description = "资源状态：ENABLED/DISABLED")
    private String status;
}
