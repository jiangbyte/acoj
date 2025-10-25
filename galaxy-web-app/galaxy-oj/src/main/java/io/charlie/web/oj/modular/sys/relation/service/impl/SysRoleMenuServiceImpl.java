package io.charlie.web.oj.modular.sys.relation.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import io.charlie.web.oj.modular.sys.relation.entity.SysRoleMenu;
import io.charlie.web.oj.modular.sys.relation.entity.SysUserRole;
import io.charlie.web.oj.modular.sys.relation.mapper.SysRoleMenuMapper;
import io.charlie.web.oj.modular.sys.relation.mapper.SysUserRoleMapper;
import io.charlie.web.oj.modular.sys.relation.service.SysRoleMenuService;
import io.charlie.web.oj.modular.sys.relation.service.SysUserRoleService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author Charlie Zhang
 * @version v1.0
 * @date 2025-07-05
 * @description 用户-角色 关联表(1-N) 服务实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SysRoleMenuServiceImpl extends ServiceImpl<SysRoleMenuMapper, SysRoleMenu> implements SysRoleMenuService {

    @Override
    public void assignMenus(String roleId, List<String> menuIds) {
        // 先清除全部的
        this.remove(new LambdaQueryWrapper<SysRoleMenu>()
                .eq(SysRoleMenu::getRoleId, roleId)
        );

        // 再分配
        List<SysRoleMenu> sysRoleMenus = menuIds.stream().map(menuId -> {
            SysRoleMenu sysRoleMenu = new SysRoleMenu();
            sysRoleMenu.setRoleId(roleId);
            sysRoleMenu.setMenuId(menuId);
            return sysRoleMenu;
        }).toList();

//        for (String menuId : menuIds) {
//            SysRoleMenu sysRoleMenu = new SysRoleMenu();
//            sysRoleMenu.setRoleId(roleId);
//            sysRoleMenu.setMenuId(menuId);
//            this.save(sysRoleMenu);
//        }
        this.saveBatch(sysRoleMenus);
    }

    @Override
    public List<String> findMenuIdsByRoleId(String roleId) {
        return this.list(new LambdaQueryWrapper<SysRoleMenu>()
                .eq(SysRoleMenu::getRoleId, roleId)
        ).stream().map(SysRoleMenu::getMenuId).toList();
    }
}