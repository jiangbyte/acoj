package github.jiangbyte.io.auth.modules.login.result;

import io.swagger.v3.oas.annotations.media.Schema;
import github.jiangbyte.io.common.core.enums.AccountType;
import lombok.Data;

import java.util.List;
import java.util.Set;

/**
 * 当前登录用户摘要：账号标识、角色权限集合及密码是否过期等。
 *
 * Author: Charlie
 */
@Schema(description = "当前登录用户摘要：账号标识、角色权限集合及密码是否过期等。")
@Data
public class CurrentUserResult {
    @Schema(description = "账户ID")
    private String accountId;
    @Schema(description = "登录账号/用户名")
    private String account;
    @Schema(description = "账户类型：ADMIN（管理端）/ PORTAL（门户端）")
    private AccountType accountType;
    @Schema(description = "roles")
    private Set<String> roles;
    @Schema(description = "permissions")
    private Set<String> permissions;
    @Schema(description = "roleIds")
    private List<String> roleIds;
    @Schema(description = "deptIds")
    private List<String> deptIds;
    @Schema(description = "groupIds")
    private List<String> groupIds;
    @Schema(description = "resourceIds")
    private List<String> resourceIds;
    @Schema(description = "buttonCodes")
    private List<String> buttonCodes;
    @Schema(description = "passwordExpired")
    private Boolean passwordExpired;
}
