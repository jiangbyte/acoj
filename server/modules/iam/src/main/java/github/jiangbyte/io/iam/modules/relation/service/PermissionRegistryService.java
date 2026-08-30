package github.jiangbyte.io.iam.modules.relation.service;

import github.jiangbyte.io.iam.modules.relation.result.SysRegisteredPermissionResult;

import java.util.List;

/**
 * 权限注册表服务接口：同步 Redis、列出已注册权限、确保权限键已登记。
 *
 * Author: Charlie
 */
public interface PermissionRegistryService {

    /** 将已注册权限同步到 Redis。 */
    void syncToRedis();

    /** 列出全部已注册权限。 */
    List<SysRegisteredPermissionResult> listRegisteredPermissions();

    /** 确保权限键已登记（不存在则注册）。 */
    void ensureRegistered(String permissionKey);

}
