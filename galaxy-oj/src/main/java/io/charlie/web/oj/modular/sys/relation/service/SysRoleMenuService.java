package io.charlie.web.oj.modular.sys.relation.service;

import com.baomidou.mybatisplus.extension.service.IService;
import io.charlie.web.oj.modular.sys.relation.entity.SysRoleMenu;
import io.charlie.web.oj.modular.sys.relation.entity.SysUserRole;

import java.util.List;

/**
* @author Charlie Zhang
* @version v1.0
* @date 2025-07-05
* @description 用户-角色 关联表(1-N) 服务类
*/
public interface SysRoleMenuService extends IService<SysRoleMenu> {
   void assignMenus(String roleId, List<String> menuIds);

   List<String> findMenuIdsByRoleId(String roleId);
}