package io.charlie.web.oj.modular.sys.auth.utils;

import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import io.charlie.web.oj.modular.sys.group.entity.SysGroup;
import io.charlie.web.oj.modular.sys.group.mapper.SysGroupMapper;
import io.charlie.web.oj.modular.sys.relation.entity.SysUserRole;
import io.charlie.web.oj.modular.sys.relation.mapper.SysUserRoleMapper;
import io.charlie.web.oj.modular.sys.role.entity.SysRole;
import io.charlie.web.oj.modular.sys.role.mapper.SysRoleMapper;
import io.charlie.web.oj.modular.sys.user.entity.SysUser;
import io.charlie.web.oj.modular.sys.user.mapper.SysUserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

/**
 * @author ZhangJiangHu
 * @version v1.0
 * @date 24/10/2025
 * @description 用户上下文工具
 */
@Component
@RequiredArgsConstructor
public class AuthContextUtil {
    private final SysUserRoleMapper sysUserRoleMapper;
    private final SysRoleMapper sysRoleMapper;
    private final SysUserMapper sysUserMapper;
    private final SysGroupMapper sysGroupMapper;

    // 获取当前用户ID
    public String getUserId() {
        try {
            return StpUtil.getLoginIdAsString();
        } catch (Exception e) {
            return null;
        }
    }


    // 获取当前用户信息
    public SysUser getUserInfo() {
        if (getUserId() != null) {
            return sysUserMapper.selectById(getUserId());
        }
        return null;
    }

    // 获取当前用户平台
    public String getPlatform() {
        try {
            return StpUtil.getLoginDevice();
        } catch (Exception e) {
            return null;
        }
    }

    // 获取当前用户角色列表
    public List<SysRole> getRoles() {
        try {
            String loginIdAsString = StpUtil.getLoginIdAsString();
            // 先查询全部的角色
            List<SysUserRole> list = sysUserRoleMapper.selectList(new LambdaQueryWrapper<SysUserRole>()
                    .eq(SysUserRole::getUserId, loginIdAsString)
            );
            if (ObjectUtil.isEmpty(list)) {
                return null;
            }

            // 获取角色ID
            List<String> roleIds = list.stream().map(SysUserRole::getRoleId).toList();
            if (ObjectUtil.isEmpty(roleIds)) {
                return List.of();
            }
            // 查询全部的角色实体
            return sysRoleMapper.selectByIds(roleIds);
        } catch (Exception e) {
            return List.of();
        }
    }

    // 获取当前用户角色列表的数据权限列表
    public List<String> getRoleDataScopes() {
        return getRoles().stream().map(SysRole::getDataScope).toList();
    }

    // 获取当前用户角色列表中层级最高的角色
    public SysRole getHighLevelRole() {
        List<SysRole> roles = getRoles();
        SysRole highLevelRole = null;

        Integer level = 0;

        for (SysRole role : roles) {
            if (role.getLevel() > level) {
                highLevelRole = role;
                level = role.getLevel();
            }
        }

        return highLevelRole;
    }

    // 获取当前用户角色列表中层级最高的角色
    public String getHighLevelRoleDataScope() {
        return getHighLevelRole().getDataScope();
    }

    // 获取当前用户是否是系统超级用户
    public boolean isSuperUser() {
        SysRole highLevelRole = getHighLevelRole();
        return highLevelRole.getLevel() == 99;
    }

    // 获取当前用户所在用户组
    public SysGroup getUserGroup() {
        try {
            String loginIdAsString = StpUtil.getLoginIdAsString();
            // 查询用户
            SysUser sysUser = sysUserMapper.selectById(loginIdAsString);

            String groupId = sysUser.getGroupId();

            if (ObjectUtil.isEmpty(groupId)) {
                return null;
            }

            return sysGroupMapper.selectById(groupId);
        } catch (Exception e) {
            return null;
        }
    }


    public String getUserGroupId() {
       if (getUserGroup() != null) {
           return getUserGroup().getId();
       }

       return null;
    }

    // 获得当前子组ids（是否包含本身用户组id）
    public List<String> getUserSubGroupIds(boolean includeSelf) {
        if (isSuperUser()) {
            return sysGroupMapper.selectList(new LambdaQueryWrapper<>()).stream().map(SysGroup::getId).toList();
        }

        SysGroup userGroup = getUserGroup();
        if (ObjectUtil.isEmpty(userGroup)) {
            return List.of();
        }

        // 查询所有用户组
        List<SysGroup> allGroups = sysGroupMapper.selectList(new LambdaQueryWrapper<>());
        if (ObjectUtil.isEmpty(allGroups)) {
            return includeSelf ? List.of(userGroup.getId()) : List.of();
        }

        // 使用循环方式获取所有子组ID
        List<String> subGroupIds = new ArrayList<>();
        Queue<String> queue = new LinkedList<>();
        queue.offer(userGroup.getId());

        while (!queue.isEmpty()) {
            String currentGroupId = queue.poll();
            List<SysGroup> children = allGroups.stream()
                    .filter(group -> currentGroupId.equals(group.getParentId()))
                    .toList();

            for (SysGroup child : children) {
                subGroupIds.add(child.getId());
                queue.offer(child.getId());
            }
        }

        if (includeSelf) {
            subGroupIds.add(userGroup.getId());
        }

        return subGroupIds;
    }
}
