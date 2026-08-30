package github.jiangbyte.io.iam.modules.role.param;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.Map;

/**
 * 编辑角色入参。
 *
 * Author: Charlie
 */
@Schema(description = "编辑角色入参。")
@Data
public class SysRoleEditParam {

    @NotBlank
    @Size(max = 64)
    @Schema(description = "主键ID")
    private String id;

    @NotBlank
    @Schema(description = "编码")
    private String code;

    @NotBlank
    @Schema(description = "名称")
    private String name;
    @Schema(description = "角色分类")

    private String category;
    @Schema(description = "角色作用域：GLOBAL/DEPT 等")
    private String scopeType;
    @Schema(description = "所属部门ID（数据权限范围）")
    private String ownerDeptId;
    @Schema(description = "排序号（越小越靠前）")
    private Integer sort = 99;
    @Schema(description = "角色状态：ENABLED/DISABLED")
    private String status = "ENABLED";
    @Schema(description = "是否内置角色：1 内置 / 0 自定义")
    private Boolean isBuiltin = false;
    @Schema(description = "角色描述")
    private String description;
    @Schema(description = "扩展信息（JSON）")
    private Map<String, Object> extra = Map.of();
}
