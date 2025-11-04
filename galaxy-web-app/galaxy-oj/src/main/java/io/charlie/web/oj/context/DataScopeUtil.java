package io.charlie.web.oj.context;

import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import io.charlie.galaxy.exception.BusinessException;
import io.charlie.web.oj.modular.sys.group.entity.SysGroup;
import io.charlie.web.oj.modular.sys.group.mapper.SysGroupMapper;
import io.charlie.web.oj.modular.sys.menu.entity.SysMenu;
import io.charlie.web.oj.modular.sys.menu.mapper.SysMenuMapper;
import io.charlie.web.oj.modular.sys.relation.entity.SysRoleMenu;
import io.charlie.web.oj.modular.sys.relation.entity.SysUserRole;
import io.charlie.web.oj.modular.sys.relation.mapper.SysRoleMenuMapper;
import io.charlie.web.oj.modular.sys.relation.mapper.SysUserRoleMapper;
import io.charlie.web.oj.modular.sys.role.entity.SysRole;
import io.charlie.web.oj.modular.sys.role.mapper.SysRoleMapper;
import io.charlie.web.oj.modular.sys.user.entity.SysUser;
import io.charlie.web.oj.modular.sys.user.mapper.SysUserMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class DataScopeUtil {
    private final SysUserMapper sysUserMapper;
    private final SysRoleMapper sysRoleMapper;
    private final SysUserRoleMapper sysUserRoleMapper;
    private final SysGroupMapper sysGroupMapper;
    private final SysRoleMenuMapper sysRoleMenuMapper;
    private final SysMenuMapper sysMenuMapper;

    private final DataScopeCacheService dataScopeCacheService;


    public DataScopeContext getDataScopeContext() {
        if (!StpUtil.isLogin()) {
            throw new BusinessException("用户未登录");
        }

        try {
            String userId = StpUtil.getLoginIdAsString();
//            log.info("开始获取用户数据权限上下文：userId={}", userId);
//
//            // 首先尝试从缓存获取
//            DataScopeContext cachedContext = dataScopeCacheService.get(userId);
//            if (cachedContext != null) {
//                cachedContext.printDataScopeContext();
//                log.info("使用缓存的数据权限上下文：userId={}", userId);
//                return cachedContext;
//            }

            // 缓存中没有，重新构建
            DataScopeContext dataScopeContext = buildDataScopeContext(userId);

            // 放入缓存
//            dataScopeCacheService.put(userId, dataScopeContext);
            dataScopeContext.printDataScopeContext();
            return dataScopeContext;
        } catch (Exception e) {
            log.error("获取数据权限上下文失败", e);
//            throw new BusinessException("获取数据权限失败");
            e.printStackTrace();
        }

        return DataScopeContext.builder().build();
    }

    /**
     * 刷新指定用户的数据权限上下文
     */
    public void refreshUserDataScope(String userId) {
        log.info("刷新用户数据权限上下文：userId={}", userId);
        // 清除缓存
        dataScopeCacheService.evict(userId);

        // 重新构建并缓存
        DataScopeContext newContext = buildDataScopeContext(userId);
        dataScopeCacheService.put(userId, newContext);
    }

    /**
     * 刷新所有用户的数据权限上下文缓存
     */
    public void refreshAllDataScope() {
        log.info("刷新所有用户的数据权限上下文缓存");
        dataScopeCacheService.evictAll();
    }

    private DataScopeContext buildDataScopeContext(String userId) {
        // 获取用户基本信息
        SysUser user = getUserWithValidation(userId);

        // 批量获取用户相关数据
        UserDataBundle userData = getUserDataBundle(userId);

        // 处理数据权限
        Set<String> dataScopes = processDataScopes(userData.getRoles());

        // 获取可访问的用户组
        List<String> accessibleGroupIds = getAccessibleGroupIds(dataScopes, userId, userData.getRoles());

        // 获取可访问的角色
        List<String> accessibleRoleIds = getAccessibleRoleIds(dataScopes, userData.getRoles());

        // 获取可访问的菜单
        MenuDataBundle menuData = getMenuDataBundle(dataScopes, userData.getRoleIds());
//        log.info("menuData={}", JSONUtil.toJsonStr(menuData));
        return DataScopeContext.builder()
                .userId(userId)
                .groupId(user.getGroupId())
                .roleCodes(userData.getRoleCodes())
                .roleIds(userData.getRoleIds())
                .dataScopes(dataScopes)
                .accessibleGroupIds(accessibleGroupIds)
                .accessibleRoleIds(accessibleRoleIds)
                .accessibleMenuIds(menuData.getAccessibleMenuIds())
                .accessiblePermissions(menuData.getAccessiblePermissions())
                .build();
    }

    /**
     * 获取用户信息并进行验证
     */
    private SysUser getUserWithValidation(String userId) {
        SysUser user = sysUserMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        return user;
    }

    public boolean canManageData(String userId) {
        // 查询用户的角色关联，获取得到数据权限范围，只有SELF就没有管理权限

        // 批量获取用户相关数据
        UserDataBundle userData = getUserDataBundle(userId);

        // 处理数据权限
        Set<String> dataScopes = processDataScopes(userData.getRoles());

        // 创建包含所有管理权限的集合
        Set<String> manageScopes = new HashSet<>(Arrays.asList(
                DataScopeConstant.ALL,
                DataScopeConstant.SELF_GROUP,
                DataScopeConstant.SELF_GROUP_ONLY,
                DataScopeConstant.SPECIFIED_GROUP
        ));

        // 检查是否存在任何管理权限
        return dataScopes.stream().anyMatch(manageScopes::contains);
    }

    /**
     * 批量获取用户相关数据
     */
    private UserDataBundle getUserDataBundle(String userId) {
        // 获取用户角色关联
        List<SysUserRole> userRoleRelations = sysUserRoleMapper.selectList(
                new LambdaQueryWrapper<SysUserRole>().eq(SysUserRole::getUserId, userId)
        );

        if (CollectionUtil.isEmpty(userRoleRelations)) {
            return UserDataBundle.empty();
        }

        // 获取角色详情
        List<String> roleIds = userRoleRelations.stream()
                .map(SysUserRole::getRoleId)
                .collect(Collectors.toList());

        List<SysRole> roles = sysRoleMapper.selectList(
                new LambdaQueryWrapper<SysRole>().in(SysRole::getId, roleIds)
        );

        return UserDataBundle.fromRoles(roles);
    }

    /**
     * 处理数据权限范围
     */
    private Set<String> processDataScopes(List<SysRole> roles) {
        return roles.stream()
                .map(SysRole::getDataScope)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
    }

    /**
     * 获取可访问的角色ID列表
     */
    private List<String> getAccessibleRoleIds(Set<String> dataScopes, List<SysRole> roles) {
        if (dataScopes.contains(DataScopeConstant.ALL)) {
            return getAllRoleIds();
        }

        // 非 DataScopeConstant.ALL 只能看其他的，不包括DataScopeConstant.ALL
        return getPartRoleIds(dataScopes);
    }

    private List<String> getAllRoleIds() {
        return sysRoleMapper.selectList(null).stream()
                .map(SysRole::getId)
                .collect(Collectors.toList());
    }

    private List<String> getPartRoleIds(Set<String> dataScopes) {
        Set<String> manageScopes = Set.of(
                DataScopeConstant.SELF_GROUP,
                DataScopeConstant.SELF_GROUP_ONLY,
                DataScopeConstant.SPECIFIED_GROUP
        );

        if (dataScopes.stream().noneMatch(manageScopes::contains)) {
            return List.of();
        }

        // 根据数据范围权限确定可查看的数据范围
        Set<String> visibleDataScopes = new HashSet<>();

        if (dataScopes.contains(DataScopeConstant.SELF_GROUP)) {
            Collections.addAll(visibleDataScopes,
                    DataScopeConstant.SELF_GROUP,
                    DataScopeConstant.SELF_GROUP_ONLY,
                    DataScopeConstant.SPECIFIED_GROUP,
                    DataScopeConstant.SELF
            );
        }
        if (dataScopes.contains(DataScopeConstant.SELF_GROUP_ONLY)) {
            Collections.addAll(visibleDataScopes,
                    DataScopeConstant.SELF_GROUP_ONLY,
                    DataScopeConstant.SPECIFIED_GROUP,
                    DataScopeConstant.SELF
            );
        }
        if (dataScopes.contains(DataScopeConstant.SPECIFIED_GROUP)) {
            Collections.addAll(visibleDataScopes,
                    DataScopeConstant.SPECIFIED_GROUP,
                    DataScopeConstant.SELF);
        }

        LambdaQueryWrapper<SysRole> queryWrapper = new LambdaQueryWrapper<SysRole>()
                .ne(SysRole::getDataScope, DataScopeConstant.ALL)
                .in(SysRole::getDataScope, visibleDataScopes);

        return sysRoleMapper.selectList(queryWrapper).stream()
                .map(SysRole::getId)
                .collect(Collectors.toList());
    }

    /**
     * 获取可访问的用户组ID列表
     */
    private List<String> getAccessibleGroupIds(Set<String> dataScopes, String userId, List<SysRole> roles) {
        List<String> accessibleGroupIds = new ArrayList<>();
        if (dataScopes.contains(DataScopeConstant.ALL)) {
            accessibleGroupIds.addAll(getAllGroupIds());
        }
        if (dataScopes.contains(DataScopeConstant.SELF_GROUP)) {
            accessibleGroupIds.addAll(getSelfGroupAndChildrenIds(userId));
        }
        if (dataScopes.contains(DataScopeConstant.SELF_GROUP_ONLY)) {
            accessibleGroupIds.addAll(getSelfGroupIdOnly(userId));
        }
        if (dataScopes.contains(DataScopeConstant.SPECIFIED_GROUP)) {
            accessibleGroupIds.addAll(getSpecifiedGroupIds(roles));
        }

        // 负责人自己的组
        List<SysGroup> sysGroups = sysGroupMapper.selectList(new LambdaQueryWrapper<SysGroup>()
                .eq(SysGroup::getAdminId, userId)
        );
        if (ObjectUtil.isNotEmpty(sysGroups)) {
            accessibleGroupIds.addAll(sysGroups.stream()
                    .map(SysGroup::getId)
                    .toList());
        }

        return accessibleGroupIds;
    }

    /**
     * 获取所有用户组ID
     */
    private List<String> getAllGroupIds() {
        return sysGroupMapper.selectList(null).stream()
                .map(SysGroup::getId)
                .collect(Collectors.toList());
    }

    /**
     * 获取本组及子组ID
     */
    private List<String> getSelfGroupAndChildrenIds(String userId) {
        log.info("获取本组及子组ID {}", userId);
        SysUser user = sysUserMapper.selectById(userId);
        List<String> list = sysGroupMapper.selectGroupAndChildrenIdsWithOption(user.getGroupId(), true);
        log.info("获取本组及子组ID {}", list);
        return list;
    }

    /**
     * 仅获取本组ID
     */
    private List<String> getSelfGroupIdOnly(String userId) {
        SysUser user = sysUserMapper.selectById(userId);
        return List.of(user.getGroupId());
    }

    /**
     * 获取指定用户组ID
     */
    private List<String> getSpecifiedGroupIds(List<SysRole> roles) {
        return roles.stream()
                .map(SysRole::getAssignGroupIds)
                .filter(Objects::nonNull)
                .flatMap(List::stream)
                .distinct()
                .collect(Collectors.toList());
    }

    /**
     * 获取可访问的菜单ID列表
     */
    private List<String> getAccessibleMenuIds(Set<String> dataScopes, List<String> roleIds) {
        if (CollectionUtil.isEmpty(roleIds)) {
            return Collections.emptyList();
        }

        // 如果是ALL，是所有菜单
        if (dataScopes.contains(DataScopeConstant.ALL)) {
            return sysMenuMapper.selectList(null).stream()
                    .map(SysMenu::getId)
                    .distinct()
                    .collect(Collectors.toList());
        }

        // 其他的根据权限来获取
        List<SysRoleMenu> roleMenus = sysRoleMenuMapper.selectList(
                new LambdaQueryWrapper<SysRoleMenu>().in(SysRoleMenu::getRoleId, roleIds)
        );

        return roleMenus.stream()
                .map(SysRoleMenu::getMenuId)
                .distinct()
                .collect(Collectors.toList());
    }

    private MenuDataBundle getMenuDataBundle(Set<String> dataScopes, List<String> roleIds) {
        if (CollectionUtil.isEmpty(roleIds)) {
            return MenuDataBundle.empty();
        }

        // 如果是ALL，是所有菜单
        if (dataScopes.contains(DataScopeConstant.ALL)) {
            List<SysMenu> sysMenus = sysMenuMapper.selectList(null);
            return MenuDataBundle.fromMenu(sysMenus);
        }

        // 其他的根据权限来获取
        List<SysRoleMenu> roleMenus = sysRoleMenuMapper.selectList(new LambdaQueryWrapper<SysRoleMenu>().in(SysRoleMenu::getRoleId, roleIds));
        List<String> menuIds = roleMenus.stream().map(SysRoleMenu::getMenuId).toList();
        if (ObjectUtil.isNotEmpty(menuIds)) {
            List<SysMenu> sysMenus = sysMenuMapper.selectList(new LambdaQueryWrapper<SysMenu>()
                    .in(SysMenu::getId, menuIds)
            );
            return MenuDataBundle.fromMenu(sysMenus);
        }
        return MenuDataBundle.fromMenu(List.of());
    }

    /**
     * 用户数据包，用于批量传递用户相关数据
     */
    @lombok.Data
    @lombok.Builder
    private static class MenuDataBundle {
        private List<String> accessibleMenuIds;
        private List<String> accessiblePermissions;

        public static MenuDataBundle empty() {
            return MenuDataBundle.builder()
                    .accessibleMenuIds(Collections.emptyList())
                    .accessiblePermissions(Collections.emptyList())
                    .build();
        }

        public static MenuDataBundle fromMenu(List<SysMenu> menus) {
            List<String> accessibleMenuIds = menus.stream()
                    .map(SysMenu::getId)
                    .toList();

            List<String> flattenedPermissions = menus.stream()
                    .map(SysMenu::getExJson)
                    .filter(exJson -> exJson != null && !exJson.isEmpty())
                    .flatMap(List::stream)
                    .toList();

            return MenuDataBundle.builder()
                    .accessibleMenuIds(accessibleMenuIds)
                    .accessiblePermissions(flattenedPermissions)
                    .build();

        }
    }

    /**
     * 用户数据包，用于批量传递用户相关数据
     */
    @lombok.Data
    @lombok.Builder
    private static class UserDataBundle {
        private List<SysRole> roles;
        private List<String> roleCodes;
        private List<String> roleIds;

        public static UserDataBundle empty() {
            return UserDataBundle.builder()
                    .roles(Collections.emptyList())
                    .roleCodes(Collections.emptyList())
                    .roleIds(Collections.emptyList())
                    .build();
        }

        public static UserDataBundle fromRoles(List<SysRole> roles) {
            List<String> roleCodes = roles.stream()
                    .map(SysRole::getCode)
                    .collect(Collectors.toList());

            List<String> roleIds = roles.stream()
                    .map(SysRole::getId)
                    .collect(Collectors.toList());

            return UserDataBundle.builder()
                    .roles(roles)
                    .roleCodes(roleCodes)
                    .roleIds(roleIds)
                    .build();
        }
    }
}