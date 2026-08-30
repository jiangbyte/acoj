package github.jiangbyte.io.iam.modules.resource.param;

import io.swagger.v3.oas.annotations.media.Schema;
import github.jiangbyte.io.common.core.domain.PageQuery;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 管理端按钮资源分页查询入参。
 *
 * Author: Charlie
 */
@Schema(description = "管理端按钮资源分页查询入参。")
@Data
@EqualsAndHashCode(callSuper = true)
public class SysResourceButtonPageParam extends PageQuery {

    @NotBlank
    @Schema(description = "父级ID")
    private String parentId;
    @Schema(description = "编码")
    private String code;
    @Schema(description = "名称")
    private String name;
    @Schema(description = "状态")
    private String status;
}
