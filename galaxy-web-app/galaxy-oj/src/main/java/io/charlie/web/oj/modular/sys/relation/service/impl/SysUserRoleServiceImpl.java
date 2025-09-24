package io.charlie.web.oj.modular.sys.relation.service.impl;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import io.charlie.web.oj.modular.sys.menu.entity.SysMenu;
import io.charlie.web.oj.modular.sys.menu.mapper.SysMenuMapper;
import io.charlie.web.oj.modular.sys.relation.entity.SysRoleMenu;
import io.charlie.web.oj.modular.sys.relation.entity.SysUserRole;
import io.charlie.web.oj.modular.sys.relation.mapper.SysRoleMenuMapper;
import io.charlie.web.oj.modular.sys.relation.mapper.SysUserRoleMapper;
import io.charlie.web.oj.modular.sys.relation.service.SysUserRoleService;
import io.charlie.web.oj.modular.sys.role.entity.SysRole;
import io.charlie.web.oj.modular.sys.role.mapper.SysRoleMapper;
import io.charlie.web.oj.modular.sys.role.utils.RoleLevelTool;
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
public class SysUserRoleServiceImpl extends ServiceImpl<SysUserRoleMapper, SysUserRole> implements SysUserRoleService {
    private final SysRoleMapper sysRoleMapper;
    private final SysRoleMenuMapper sysRoleMenuMapper;
    private final SysMenuMapper sysMenuMapper;

    @Override
    public void assignRoles(String userId, List<String> roleIds) {
        // 先清空
        this.remove(new LambdaQueryWrapper<SysUserRole>()
                .eq(SysUserRole::getUserId, userId)
        );

        // 再分配
        for (String roleId : roleIds) {
            SysUserRole sysUserRole = new SysUserRole();
            sysUserRole.setUserId(userId);
            sysUserRole.setRoleId(roleId);
            this.save(sysUserRole);
        }
    }

    @Override
    public SysRole getHeightLevelRole(String userId) {
        // 先查询全部的角色
        List<SysUserRole> list = this.list(new LambdaQueryWrapper<SysUserRole>()
                .eq(SysUserRole::getUserId, userId)
        );
        if (ObjectUtil.isEmpty(list)) {
            return null;
        }

        List<String> stringList = list.stream().map(SysUserRole::getRoleId).toList();
        // 查询全部的角色实体
        List<SysRole> sysRoles = sysRoleMapper.selectByIds(stringList);
        return RoleLevelTool.getHighLevelRole(sysRoles);
    }

    @Override
    public Boolean canAdmin(String userId) {
        // 先查询全部的角色
        List<SysUserRole> list = this.list(new LambdaQueryWrapper<SysUserRole>()
                .eq(SysUserRole::getUserId, userId)
        );
        log.info("用户角色映射 {}", JSONUtil.toJsonStr(list));
        // 无角色不能
        if (ObjectUtil.isEmpty(list)) {
            return false;
        }

        List<String> stringList = list.stream().map(SysUserRole::getRoleId).toList();
        // 查询全部的角色实体
        List<SysRole> sysRoles = sysRoleMapper.selectByIds(stringList);
        log.info("角色列表 {}", JSONUtil.toJsonStr(sysRoles));
        SysRole highLevelRole = RoleLevelTool.getHighLevelRole(sysRoles);
        log.info("最高角色 {}", JSONUtil.toJsonStr(highLevelRole));

        List<SysRoleMenu> sysRoleMenus = sysRoleMenuMapper.selectList(new LambdaQueryWrapper<SysRoleMenu>()
                .eq(SysRoleMenu::getRoleId, highLevelRole.getId())
        );
        log.info("角色菜单映射 {}", JSONUtil.toJsonStr(sysRoleMenus));
        // 角色菜单空不能
        if (ObjectUtil.isEmpty(sysRoleMenus)) {
            return false;
        }

        return true;
//        List<String> stringList1 = sysRoleMenus.stream().map(SysRoleMenu::getMenuId).toList();
//        List<SysMenu> sysMenus = sysMenuMapper.selectByIds(stringList1);
//        return !ObjectUtil.isEmpty(sysMenus);
    }
}