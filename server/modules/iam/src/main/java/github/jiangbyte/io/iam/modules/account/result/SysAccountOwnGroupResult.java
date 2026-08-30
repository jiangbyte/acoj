package github.jiangbyte.io.iam.modules.account.result;

import io.swagger.v3.oas.annotations.media.Schema;
import github.jiangbyte.io.iam.modules.group.entity.SysGroup;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * 账号已拥有用户组结果（用户组 id 列表）。
 *
 * Author: Charlie
 */
@Schema(description = "账号已拥有用户组结果（用户组 id 列表）。")
@Data
public class SysAccountOwnGroupResult {
    @Schema(description = "主键ID")

    private String id;
    @Schema(description = "groups")
    private List<SysGroup> groups = new ArrayList<>();
    @Schema(description = "groupIds")
    private List<String> groupIds = new ArrayList<>();
}
