package github.jiangbyte.io.iam.modules.account.result;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * 主体（角色/用户组）已关联账号结果：账号 id 列表。
 *
 * Author: Charlie
 */
@Schema(description = "主体（角色/用户组）已关联账号结果：账号 id 列表。")
@Data
public class SysOwnUserResult {
    @Schema(description = "主键ID")

    private String id;
    @Schema(description = "users")
    private List<SysAccountResult> users = new ArrayList<>();
    @Schema(description = "accountIds")
    private List<String> accountIds = new ArrayList<>();
}
