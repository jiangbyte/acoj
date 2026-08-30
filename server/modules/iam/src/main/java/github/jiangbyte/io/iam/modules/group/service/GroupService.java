package github.jiangbyte.io.iam.modules.group.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import github.jiangbyte.io.common.core.param.IdsParam;
import github.jiangbyte.io.iam.modules.account.result.SysOwnUserResult;
import github.jiangbyte.io.iam.modules.group.entity.SysGroup;
import github.jiangbyte.io.iam.modules.group.param.SysGroupAddParam;
import github.jiangbyte.io.iam.modules.group.param.SysGroupEditParam;
import github.jiangbyte.io.iam.modules.group.param.SysGroupGrantResourceParam;
import github.jiangbyte.io.iam.modules.group.param.SysGroupGrantRoleParam;
import github.jiangbyte.io.iam.modules.group.param.SysGroupGrantUserParam;
import github.jiangbyte.io.iam.modules.group.param.SysGroupPageParam;
import github.jiangbyte.io.iam.modules.group.result.SysGroupOwnRoleResult;
import github.jiangbyte.io.iam.modules.resource.result.SysResourceOwnResult;

/**
 * 用户组服务接口：CRUD、成员/角色/资源授权与查询。
 *
 * Author: Charlie
 */
public interface GroupService extends IService<SysGroup> {

    /** 创建用户组。 */
    void create(SysGroupAddParam param);

    /** 更新用户组。 */
    void update(SysGroupEditParam param);

    /** 批量删除用户组。 */
    void delete(IdsParam param);

    /** 查询用户组详情。 */
    SysGroup detail(String id);

    /** 分页查询用户组。 */
    Page<SysGroup> page(SysGroupPageParam param);

    /** 查询用户组成员账号。 */
    SysOwnUserResult ownUsers(String id);

    /** 全量替换用户组成员。 */
    void grantUsers(SysGroupGrantUserParam param);

    /** 查询用户组已拥有角色。 */
    SysGroupOwnRoleResult ownRoles(String id, String accountType);

    /** 全量替换用户组角色授权。 */
    void grantRoles(SysGroupGrantRoleParam param);

    /** 查询用户组已拥有管理端资源。 */
    SysResourceOwnResult ownResources(String id, String accountType);

    /** 全量替换用户组管理端资源授权。 */
    void grantResources(SysGroupGrantResourceParam param);

    /** 查询用户组已拥有客户端资源。 */
    SysResourceOwnResult ownClientResources(String id, String accountType);

    /** 全量替换用户组客户端资源授权。 */
    void grantClientResources(SysGroupGrantResourceParam param);
}
