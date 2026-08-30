package github.jiangbyte.io.iam.modules.group.param;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * 用户组授权角色入参。
 *
 * Author: Charlie
 */
@Schema(description = "用户组授权角色入参。")
@Data
public class SysGroupGrantRoleParam {

    @NotBlank
    @Schema(description = "主键ID")
    private String id;
    @Schema(description = "账户类型：ADMIN（管理端）/ PORTAL（门户端）")
    private String accountType = "ADMIN";
    @Schema(description = "roleIds")
    private List<String> roleIds = new ArrayList<>();
}
