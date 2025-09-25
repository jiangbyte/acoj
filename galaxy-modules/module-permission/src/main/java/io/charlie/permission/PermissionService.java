package io.charlie.permission;

import java.util.List;

/**
 * @author ZhangJiangHu
 * @version v1.0
 * @date 25/09/2025
 * @description 权限服务
 */
public interface PermissionService {
    List<String> getPermissionList(String loginId, String loginType);
}
