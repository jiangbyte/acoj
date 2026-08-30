package github.jiangbyte.io.iam.modules.dept.param;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.Map;

/**
 * 编辑部门入参。
 *
 * Author: Charlie
 */
@Schema(description = "编辑部门入参。")
@Data
public class SysDeptEditParam {

    @NotBlank
    @Size(max = 64)
    @Schema(description = "主键ID")
    private String id;

    @NotBlank
    @Schema(description = "部门名称")
    private String name;

    @NotBlank
    @Schema(description = "部门类别/层级类型")
    private String category;
    @Schema(description = "父级ID")

    private String parentId;
    @Schema(description = "部门主管账户ID")
    private String masterId;
    @Schema(description = "部门副主管账户ID")
    private String deputyMasterId;
    @Schema(description = "排序号（越小越靠前）")
    private Integer sort = 99;
    @Schema(description = "是否虚拟组织：1 虚拟 / 0 实体")
    private Boolean isVirtual = false;
    @Schema(description = "部门状态：ENABLED/DISABLED")
    private String status = "ENABLED";
    @Schema(description = "扩展信息（JSON）")
    private Map<String, Object> extra = Map.of();
}
