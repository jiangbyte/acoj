package github.jiangbyte.io.iam.account;

import github.jiangbyte.io.common.satoken.model.LoginUser;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * 跨模块鉴权用的轻量权限授予 DTO，字段对齐 {@link LoginUser.PermissionGrant}，
 * 并提供与 Session 模型之间的双向转换。
 *
 * Author: Charlie
 */
@Data
public class PermissionGrantInfo {
    private String permissionKey;
    private String dataScope;
    private List<String> customScopeDeptIds = new ArrayList<>();
    private String sourceType;
    private String sourceId;

    /** 从 LoginUser 权限授予转换；入参 null 时返回 null。 */
    public static PermissionGrantInfo fromLoginGrant(LoginUser.PermissionGrant grant) {
        if (grant == null) {
            return null;
        }
        PermissionGrantInfo info = new PermissionGrantInfo();
        info.setPermissionKey(grant.getPermissionKey());
        info.setDataScope(grant.getDataScope());
        info.setCustomScopeDeptIds(grant.getCustomScopeDeptIds());
        info.setSourceType(grant.getSourceType());
        info.setSourceId(grant.getSourceId());
        return info;
    }

    /** 转为 LoginUser 权限授予。 */
    public LoginUser.PermissionGrant toLoginGrant() {
        LoginUser.PermissionGrant grant = new LoginUser.PermissionGrant();
        grant.setPermissionKey(permissionKey);
        grant.setDataScope(dataScope);
        grant.setCustomScopeDeptIds(customScopeDeptIds);
        grant.setSourceType(sourceType);
        grant.setSourceId(sourceId);
        return grant;
    }

    /** 批量转为 LoginUser 权限授予列表。 */
    public static List<LoginUser.PermissionGrant> toLoginGrants(List<PermissionGrantInfo> grants) {
        List<LoginUser.PermissionGrant> result = new ArrayList<>();
        if (grants == null) {
            return result;
        }
        for (PermissionGrantInfo grant : grants) {
            if (grant != null) {
                result.add(grant.toLoginGrant());
            }
        }
        return result;
    }

    /** 批量从 LoginUser 权限授予转换。 */
    public static List<PermissionGrantInfo> fromLoginGrants(List<LoginUser.PermissionGrant> grants) {
        List<PermissionGrantInfo> result = new ArrayList<>();
        if (grants == null) {
            return result;
        }
        for (LoginUser.PermissionGrant grant : grants) {
            PermissionGrantInfo info = fromLoginGrant(grant);
            if (info != null) {
                result.add(info);
            }
        }
        return result;
    }
}
