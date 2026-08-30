package github.jiangbyte.io.iam.modules.account.support;

import github.jiangbyte.io.common.satoken.model.LoginUser;
import lombok.Data;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * 账号授权聚合模型：角色、部门、用户组、权限与客户端资源等内存视图。
 *
 * Author: Charlie
 */
@Data
public class AccountAuthorization {
    private List<String> roleIds = new ArrayList<>();
    private List<String> roleCodes = new ArrayList<>();
    private List<String> groupIds = new ArrayList<>();
    private List<String> deptIds = new ArrayList<>();
    private List<String> resourceIds = new ArrayList<>();
    private List<String> buttonCodes = new ArrayList<>();
    private List<String> permissionKeys = new ArrayList<>();
    private List<LoginUser.PermissionGrant> permissionGrants = new ArrayList<>();
    private List<String> clientResourceIds = new ArrayList<>();
    private List<String> clientPermissionKeys = new ArrayList<>();

    /** 管理端权限键集合。 */
    public Set<String> permissionSet() {
        return new HashSet<>(permissionKeys);
    }

    /** 角色 id 集合。 */
    public Set<String> roleSet() {
        return new HashSet<>(roleCodes);
    }

    /** 客户端权限键集合。 */
    public Set<String> clientPermissionSet() {
        return new HashSet<>(clientPermissionKeys);
    }

    /** 客户端资源 id 集合。 */
    public Set<String> clientResourceSet() {
        return new HashSet<>(clientResourceIds);
    }
}
