package github.jiangbyte.io.iam.modules.group.param;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.Map;

/**
 * 创建用户组入参。
 *
 * Author: Charlie
 */
@Schema(description = "创建用户组入参。")
@Data
public class SysGroupAddParam {

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
