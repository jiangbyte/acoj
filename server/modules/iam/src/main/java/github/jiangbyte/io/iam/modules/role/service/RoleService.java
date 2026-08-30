package github.jiangbyte.io.iam.modules.role.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import github.jiangbyte.io.common.core.param.IdsParam;
import github.jiangbyte.io.iam.modules.account.result.SysOwnUserResult;
import github.jiangbyte.io.iam.modules.resource.result.SysResourceOwnResult;
import github.jiangbyte.io.iam.modules.role.entity.SysRole;
import github.jiangbyte.io.iam.modules.role.param.SysRoleAddParam;
import github.jiangbyte.io.iam.modules.role.param.SysRoleEditParam;
import github.jiangbyte.io.iam.modules.role.param.SysRoleGrantResourceParam;
import github.jiangbyte.io.iam.modules.role.param.SysRoleGrantUserParam;
import github.jiangbyte.io.iam.modules.role.param.SysRolePageParam;

/**
 * 角色服务接口：CRUD、资源授权与成员授权。
 *
 * Author: Charlie
 */
public interface RoleService extends IService<SysRole> {

    /** 创建角色。 */
    void create(SysRoleAddParam param);

    /** 更新角色。 */
    void update(SysRoleEditParam param);

    /** 批量删除角色。 */
    void delete(IdsParam param);

    /** 查询角色详情。 */
    SysRole detail(String id);

    /** 分页查询角色。 */
    Page<SysRole> page(SysRolePageParam param);

    /** 查询角色已拥有管理端资源。 */
    SysResourceOwnResult ownResources(String id, String accountType);

    /** 全量替换角色管理端资源授权。 */
    void grantResources(SysRoleGrantResourceParam param);

    /** 查询角色已拥有客户端资源。 */
    SysResourceOwnResult ownClientResources(String id, String accountType);

    /** 全量替换角色客户端资源授权。 */
    void grantClientResources(SysRoleGrantResourceParam param);

    /** 查询角色成员账号。 */
    SysOwnUserResult ownUsers(String id);

    /** 全量替换角色成员。 */
    void grantUsers(SysRoleGrantUserParam param);
}
