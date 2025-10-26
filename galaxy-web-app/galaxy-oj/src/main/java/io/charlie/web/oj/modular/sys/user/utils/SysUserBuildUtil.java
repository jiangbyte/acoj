package io.charlie.web.oj.modular.sys.user.utils;

import cn.hutool.core.collection.CollectionUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import io.charlie.web.oj.modular.sys.relation.entity.SysUserRole;
import io.charlie.web.oj.modular.sys.relation.mapper.SysUserRoleMapper;
import io.charlie.web.oj.modular.sys.user.entity.SysUser;
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
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class SysUserBuildUtil {
    private final SysUserRoleMapper sysUserRoleMapper;

    /**
     * 批量设置用户角色
     */
    public void buildUsers(List<SysUser> userList) {
        if (CollectionUtil.isEmpty(userList)) {
            return;
        }

        // 获取所有用户ID
        List<String> userIds = userList.stream()
                .map(SysUser::getId)
                .distinct()
                .collect(Collectors.toList());

        // 批量查询用户角色关系
        List<SysUserRole> userRoles = sysUserRoleMapper.selectList(
                new LambdaQueryWrapper<SysUserRole>()
                        .in(SysUserRole::getUserId, userIds)
        );

        // 按用户ID分组
        Map<String, List<SysUserRole>> userRoleMap = userRoles.stream()
                .collect(Collectors.groupingBy(SysUserRole::getUserId));

        // 批量设置角色
        userList.forEach(user -> {
            List<SysUserRole> roles = userRoleMap.get(user.getId());
            List<String> roleIds = roles != null ?
                    roles.stream().map(SysUserRole::getRoleId).collect(Collectors.toList()) :
                    Collections.emptyList();
            user.setAssignRoles(roleIds);
        });
    }

    public void buildUser(SysUser user) {
        buildUsers(Collections.singletonList(user));
    }

}
