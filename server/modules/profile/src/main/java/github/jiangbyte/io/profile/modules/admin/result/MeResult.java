package github.jiangbyte.io.profile.modules.admin.result;

import io.swagger.v3.oas.annotations.media.Schema;
import github.jiangbyte.io.common.core.enums.AccountType;
import github.jiangbyte.io.profile.modules.identity.result.IdentityStatusResult;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * 管理端当前用户 me 响应：账号会话摘要、展示信息、组织名称与完整资料。
 *
 * Author: Charlie
 */
@Schema(description = "管理端当前用户 me 响应：账号会话摘要、展示信息、组织名称与完整资料。")
@Data
public class MeResult {
    @Schema(description = "账户ID")
    private String accountId;
    @Schema(description = "登录账号/用户名")
    private String account;
    @Schema(description = "账户类型：ADMIN（管理端）/ PORTAL（门户端）")
    private AccountType accountType;
    @Schema(description = "nickname")
    private String nickname;
    @Schema(description = "avatar")
    private String avatar;
    @Schema(description = "identity")
    private IdentityStatusResult identity;
    @Schema(description = "roleIds")
    private List<String> roleIds = new ArrayList<>();
    @Schema(description = "deptIds")
    private List<String> deptIds = new ArrayList<>();
    @Schema(description = "groupIds")
    private List<String> groupIds = new ArrayList<>();
    @Schema(description = "roleIdNames")
    private List<RoleIdNameResult> roleIdNames = new ArrayList<>();
    @Schema(description = "deptIdNames")
    private List<DeptIdNameResult> deptIdNames = new ArrayList<>();
    @Schema(description = "groupIdNames")
    private List<GroupIdNameResult> groupIdNames = new ArrayList<>();
    @Schema(description = "permissionKeys")
    private Set<String> permissionKeys;
    @Schema(description = "passwordExpired")
    private Boolean passwordExpired = false;
    @Schema(description = "是否需强制绑定邮箱（硬拦截）")
    /** 是否需强制绑定邮箱（硬拦截） */
    private Boolean forceBindEmail = false;
    @Schema(description = "是否需强制绑定手机（硬拦截）")
    /** 是否需强制绑定手机（硬拦截） */
    private Boolean forceBindPhone = false;
    @Schema(description = "是否需强制完成实名认证（硬拦截）")
    /** 是否需强制完成实名认证（硬拦截） */
    private Boolean forceBindIdentity = false;
    @Schema(description = "profile")
    private UserProfileResult profile;
}
