package github.jiangbyte.io.iam.modules.group.result;

import io.swagger.v3.oas.annotations.media.Schema;
import github.jiangbyte.io.iam.modules.role.entity.SysRole;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * 用户组已拥有角色结果（角色 id 列表）。
 *
 * Author: Charlie
 */
@Schema(description = "用户组已拥有角色结果（角色 id 列表）。")
@Data
public class SysGroupOwnRoleResult {
    @Schema(description = "主键ID")

    private String id;
    @Schema(description = "roles")
    private List<SysRole> roles = new ArrayList<>();
    @Schema(description = "roleIds")
    private List<String> roleIds = new ArrayList<>();
}
