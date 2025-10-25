package io.charlie.web.oj.modular.sys.role.utils;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import io.charlie.web.oj.modular.sys.relation.entity.SysRoleMenu;
import io.charlie.web.oj.modular.sys.relation.mapper.SysRoleMenuMapper;
import io.charlie.web.oj.modular.sys.role.entity.SysRole;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author ZhangJiangHu
 * @version v1.0
 * @date 25/10/2025
 * @description TODO
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class SysRoleBuildUtil {
    private final SysRoleMenuMapper sysRoleMenuMapper;

    /**
     * 批量设置角色菜单资源
     */
    public void buildRoles(List<SysRole> roleList) {
        if (CollectionUtil.isEmpty(roleList)) {
            return;
        }

        // 获取所有角色ID
        List<String> roleIds = roleList.stream()
                .map(SysRole::getId)
                .distinct()
                .collect(Collectors.toList());

        // 批量查询角色菜单关系
        List<SysRoleMenu> roleMenus = sysRoleMenuMapper.selectList(
                new LambdaQueryWrapper<SysRoleMenu>()
                        .in(SysRoleMenu::getRoleId, roleIds)
        );

        // 按角色ID分组
        Map<String, List<SysRoleMenu>> roleMenuMap = roleMenus.stream()
                .collect(Collectors.groupingBy(SysRoleMenu::getRoleId));

        // 批量设置菜单资源
        roleList.forEach(role -> {
            List<SysRoleMenu> menus = roleMenuMap.get(role.getId());
            List<String> menuIds = menus != null ?
                    menus.stream().map(SysRoleMenu::getMenuId).collect(Collectors.toList()) :
                    Collections.emptyList();
            if (ObjectUtil.isNotEmpty(menuIds)) {
                role.setAssignResource(menuIds);
            }
        });
    }

    public void buildRole(SysRole role) {
        buildRoles(Collections.singletonList(role));
    }
}
