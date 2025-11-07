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
    @Override
    public void assignRoles(String userId, List<String> roleIds) {
        // 先清空全部
        this.remove(new LambdaQueryWrapper<SysUserRole>()
                .eq(SysUserRole::getUserId, userId)
        );

        // 再分配
        List<SysUserRole> sysUserRoles = roleIds.stream().map(roleId -> {
            SysUserRole sysUserRole = new SysUserRole();
            sysUserRole.setUserId(userId);
            sysUserRole.setRoleId(roleId);
            return sysUserRole;
        }).toList();

//        // 再分配
//        for (String roleId : roleIds) {
//            SysUserRole sysUserRole = new SysUserRole();
//            sysUserRole.setUserId(userId);
//            sysUserRole.setRoleId(roleId);
//            this.save(sysUserRole);
//        }

        this.saveBatch(sysUserRoles);
    }
}