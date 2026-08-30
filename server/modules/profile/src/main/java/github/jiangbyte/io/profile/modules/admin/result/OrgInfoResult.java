package github.jiangbyte.io.profile.modules.admin.result;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * 管理端组织信息响应：当前用户角色/部门/用户组的 ID-名称列表。
 *
 * Author: Charlie
 */
@Schema(description = "管理端组织信息响应：当前用户角色/部门/用户组的 ID-名称列表。")
@Data
public class OrgInfoResult {
    @Schema(description = "roleIdNames")
    private List<RoleIdNameResult> roleIdNames = new ArrayList<>();
    @Schema(description = "deptIdNames")
    private List<DeptIdNameResult> deptIdNames = new ArrayList<>();
    @Schema(description = "groupIdNames")
    private List<GroupIdNameResult> groupIdNames = new ArrayList<>();
}
