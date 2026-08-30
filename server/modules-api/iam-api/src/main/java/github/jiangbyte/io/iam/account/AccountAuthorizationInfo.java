package github.jiangbyte.io.iam.account;

import lombok.Data;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * 跨模块账号授权快照：角色、部门、资源与权限键的运行时视图，供鉴权与 Session 填充。
 * 非 HTTP 结果，亦非持久化实体。
 *
 * Author: Charlie
 */
@Data
public class AccountAuthorizationInfo {
    private List<String> roleIds = new ArrayList<>();
    private List<String> roleCodes = new ArrayList<>();
    private List<String> groupIds = new ArrayList<>();
    private List<String> deptIds = new ArrayList<>();
    private List<String> resourceIds = new ArrayList<>();
    private List<String> buttonCodes = new ArrayList<>();
    private List<String> permissionKeys = new ArrayList<>();
    private List<PermissionGrantInfo> permissionGrants = new ArrayList<>();
    private List<String> clientResourceIds = new ArrayList<>();
    private List<String> clientPermissionKeys = new ArrayList<>();

    /** 权限键集合视图。 */
    public Set<String> permissionSet() {
        return new HashSet<>(permissionKeys);
    }

    /** 角色编码集合视图。 */
    public Set<String> roleSet() {
        return new HashSet<>(roleCodes);
    }

    /** 客户端权限键集合视图。 */
    public Set<String> clientPermissionSet() {
        return new HashSet<>(clientPermissionKeys);
    }

    /** 客户端资源 id 集合视图。 */
    public Set<String> clientResourceSet() {
        return new HashSet<>(clientResourceIds);
    }
}
