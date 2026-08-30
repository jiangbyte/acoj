package github.jiangbyte.io.iam.modules.account.result;

import io.swagger.v3.oas.annotations.media.Schema;
import github.jiangbyte.io.iam.modules.role.entity.SysRole;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * 账号已拥有角色结果（角色 id 列表）。
 *
 * Author: Charlie
 */
@Schema(description = "账号已拥有角色结果（角色 id 列表）。")
@Data
public class SysAccountOwnRoleResult {
    @Schema(description = "主键ID")

    private String id;
    @Schema(description = "roles")
    private List<SysRole> roles = new ArrayList<>();
    @Schema(description = "roleIds")
    private List<String> roleIds = new ArrayList<>();
}
