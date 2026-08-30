package github.jiangbyte.io.iam.modules.group.param;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.Map;

/**
 * 编辑用户组入参。
 *
 * Author: Charlie
 */
@Schema(description = "编辑用户组入参。")
@Data
public class SysGroupEditParam {

    @NotBlank
    @Size(max = 64)
    @Schema(description = "主键ID")
    private String id;

    @NotBlank
    @Schema(description = "用户组名称")
    private String name;
    @Schema(description = "所属部门ID（数据权限范围）")
    private String ownerDeptId;
    @Schema(description = "用户组描述")
    private String description;
    @Schema(description = "用户组状态：ENABLED/DISABLED")
    private String status = "ENABLED";
    @Schema(description = "扩展信息（JSON）")
    private Map<String, Object> extra = Map.of();
}
