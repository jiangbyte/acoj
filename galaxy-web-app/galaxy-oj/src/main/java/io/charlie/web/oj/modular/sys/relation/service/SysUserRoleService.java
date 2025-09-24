package io.charlie.web.oj.modular.sys.relation.service;

import com.baomidou.mybatisplus.extension.service.IService;
import io.charlie.web.oj.modular.sys.relation.entity.SysUserRole;
import io.charlie.web.oj.modular.sys.role.entity.SysRole;

import java.util.List;

/**
* @author Charlie Zhang
* @version v1.0
* @date 2025-07-05
* @description 用户-角色 关联表(1-N) 服务类
*/
public interface SysUserRoleService extends IService<SysUserRole> {
    void assignRoles(String userId, List<String> roleIds);

    // 获得某个用户最大的角色
    SysRole getHeightLevelRole(String userId);

    // 判断用户是否能够进入管理后台
    Boolean canAdmin(String userId);
}