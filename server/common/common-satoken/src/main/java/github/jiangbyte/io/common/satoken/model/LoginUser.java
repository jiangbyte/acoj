package github.jiangbyte.io.common.satoken.model;

import github.jiangbyte.io.common.core.enums.AccountType;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * 登录用户会话快照：账号、类型、角色、权限与数据范围等。
 *
 * Author: Charlie
 */
@Data
public class LoginUser implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private String accountId;
    private String account;
    private AccountType accountType;
    private Set<String> permissions = new HashSet<>();
    private Set<String> roles = new HashSet<>();
    private Set<String> clientPermissions = new HashSet<>();
    private Set<String> clientResources = new HashSet<>();
    private List<String> roleIds = new ArrayList<>();
    private List<String> deptIds = new ArrayList<>();
    private List<String> groupIds = new ArrayList<>();
    private List<String> resourceIds = new ArrayList<>();
    private List<String> buttonCodes = new ArrayList<>();
    private List<PermissionGrant> permissionGrants = new ArrayList<>();
    private boolean rememberMe = true;
    private boolean passwordExpired;
    private String clientIp;
    private String userAgent;
    private String deviceLabel;

    @Data
    public static class PermissionGrant implements Serializable {
        @Serial
        private static final long serialVersionUID = 1L;
        private String permissionKey;
        private String dataScope;
        private List<String> customScopeDeptIds = new ArrayList<>();
        private String sourceType;
        private String sourceId;
    }
}
