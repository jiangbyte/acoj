package github.jiangbyte.io.common.security.satoken;

import cn.dev33.satoken.stp.StpInterface;
import github.jiangbyte.io.common.satoken.model.LoginUser;
import github.jiangbyte.io.common.satoken.utils.LoginHelper;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Sa-Token 权限/角色提供者：从会话 LoginUser 读取权限与角色列表。
 *
 * Author: Charlie
 */
public class SessionPermissionProvider implements StpInterface {

    /** 返回会话中的权限码列表。 */
    @Override
    public List<String> getPermissionList(Object loginId, String loginType) {
        return LoginHelper.currentUser()
                .map(LoginUser::getPermissions)
                .map(SessionPermissionProvider::copy)
                .orElseGet(List::of);
    }

    /** 返回会话中的角色码列表。 */
    @Override
    public List<String> getRoleList(Object loginId, String loginType) {
        return LoginHelper.currentUser()
                .map(LoginUser::getRoles)
                .map(SessionPermissionProvider::copy)
                .orElseGet(List::of);
    }

    private static List<String> copy(Set<String> values) {
        if (values == null || values.isEmpty()) {
            return List.of();
        }
        return new ArrayList<>(values);
    }
}
