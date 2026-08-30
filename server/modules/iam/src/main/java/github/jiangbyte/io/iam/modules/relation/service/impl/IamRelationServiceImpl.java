package github.jiangbyte.io.iam.modules.relation.service.impl;

import github.jiangbyte.io.iam.modules.relation.service.IamRelationService;

import com.baomidou.lock.annotation.Lock4j;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import github.jiangbyte.io.common.core.enums.AccountType;
import github.jiangbyte.io.common.core.exception.BizException;
import github.jiangbyte.io.common.core.util.BatchPartition;
import github.jiangbyte.io.common.mybatis.datasource.DataSourceSticky;
import github.jiangbyte.io.common.satoken.model.LoginUser;
import github.jiangbyte.io.common.satoken.utils.LoginHelper;
import github.jiangbyte.io.iam.modules.account.entity.SysAccount;
import github.jiangbyte.io.iam.modules.account.mapper.SysAccountMapper;
import github.jiangbyte.io.iam.modules.account.support.AccountAuthorization;
import github.jiangbyte.io.iam.modules.client.entity.SysClientResource;
import github.jiangbyte.io.iam.modules.client.mapper.SysClientResourceMapper;
import github.jiangbyte.io.iam.modules.dept.result.SysDeptGrantResult;
import github.jiangbyte.io.iam.modules.dept.support.DataScopeResolver;
import github.jiangbyte.io.iam.modules.relation.constants.IamRelationTypes;
import github.jiangbyte.io.iam.modules.relation.entity.SysIamRelation;
import github.jiangbyte.io.iam.modules.relation.mapper.SysIamRelationMapper;
import github.jiangbyte.io.iam.modules.resource.entity.SysResource;
import github.jiangbyte.io.iam.modules.resource.mapper.SysResourceMapper;
import github.jiangbyte.io.iam.modules.resource.result.SysResourceGrantResult;
import github.jiangbyte.io.iam.modules.role.entity.SysRole;
import github.jiangbyte.io.iam.modules.role.mapper.SysRoleMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;

/**
 * IAM 关系服务实现：批量查询关系、组装授权视图，以及授予关系的删除后重建。
 *
 * Author: Charlie
 */
@Service
@RequiredArgsConstructor
public class IamRelationServiceImpl extends ServiceImpl<SysIamRelationMapper, SysIamRelation> implements IamRelationService {
    private final SysRoleMapper roleMapper;
    private final SysResourceMapper resourceMapper;
    private final SysClientResourceMapper clientResourceMapper;
    private final SysAccountMapper accountMapper;
    private final DataScopeResolver dataScopeResolver;

    @Override
    public AccountAuthorization getAccountAuthorization(String accountId) {
        return getAccountsAuthorization(List.of(accountId)).getOrDefault(accountId, new AccountAuthorization());
    }

    @Override
    public Map<String, AccountAuthorization> getAccountsMembership(Collection<String> accountIds) {
        // 仅组装轻量成员 id，不展开权限
        return loadAccountsAuthorization(accountIds, false);
    }

    @Override
    public Map<String, AccountAuthorization> getAccountsAuthorization(Collection<String> accountIds) {
        // 批量加载关系并组装完整授权
        return loadAccountsAuthorization(accountIds, true);
    }

    private Map<String, AccountAuthorization> loadAccountsAuthorization(
            Collection<String> accountIds, boolean includeResourcePermissions) {
        // 去重账号并预置空授权壳
        List<String> uniqueIds = accountIds.stream().filter(StringUtils::hasText).distinct().toList();
        Map<String, AccountAuthorization> result = new LinkedHashMap<>();
        for (String accountId : uniqueIds) {
            result.put(accountId, new AccountAuthorization());
        }
        if (uniqueIds.isEmpty()) {
            return result;
        }

        // 批量查账号类型，后续关系行按 accountType 过滤
        Map<String, String> accountTypeMap = loadAccountTypeMap(uniqueIds);

        // 一次查出账号直接关系：组 / 部门 / 角色
        List<SysIamRelation> accountRels = getBaseMapper().selectList(Wrappers.<SysIamRelation>lambdaQuery()
                .eq(SysIamRelation::getSubjectType, IamRelationTypes.SUBJECT_ACCOUNT)
                .in(SysIamRelation::getSubjectId, uniqueIds)
                .in(SysIamRelation::getRelationType, List.of(
                        IamRelationTypes.ACCOUNT_GROUP,
                        IamRelationTypes.ACCOUNT_DEPT,
                        IamRelationTypes.ACCOUNT_ROLE)));

        Map<String, Set<String>> groupIdsByAccount = new HashMap<>();
        Map<String, Set<String>> roleIdsByAccount = new HashMap<>();
        Map<String, Set<String>> accountIdsByGroup = new HashMap<>();
        Map<String, Set<String>> accountIdsByRole = new HashMap<>();

        // 汇总到 AccountAuthorization，并建组/角色 ↔ 账号反向索引（供继承与资源授予展开）
        for (SysIamRelation rel : accountRels) {
            String accountId = rel.getSubjectId();
            AccountAuthorization auth = result.get(accountId);
            if (auth == null || !matchesAccountType(accountTypeMap.get(accountId), rel.getAccountType())) {
                continue;
            }
            if (IamRelationTypes.ACCOUNT_GROUP.equals(rel.getRelationType())) {
                auth.getGroupIds().add(rel.getTargetId());
                groupIdsByAccount.computeIfAbsent(accountId, key -> new HashSet<>()).add(rel.getTargetId());
                accountIdsByGroup.computeIfAbsent(rel.getTargetId(), key -> new HashSet<>()).add(accountId);
            } else if (IamRelationTypes.ACCOUNT_DEPT.equals(rel.getRelationType())) {
                auth.getDeptIds().add(rel.getTargetId());
            } else if (IamRelationTypes.ACCOUNT_ROLE.equals(rel.getRelationType())) {
                roleIdsByAccount.computeIfAbsent(accountId, key -> new HashSet<>()).add(rel.getTargetId());
                accountIdsByRole.computeIfAbsent(rel.getTargetId(), key -> new HashSet<>()).add(accountId);
            }
        }

        // 经用户组展开继承角色，并入账号角色集合
        Set<String> allGroupIds = accountIdsByGroup.keySet();
        if (!allGroupIds.isEmpty()) {
            List<SysIamRelation> groupRoles = getBaseMapper().selectList(Wrappers.<SysIamRelation>lambdaQuery()
                    .eq(SysIamRelation::getSubjectType, IamRelationTypes.SUBJECT_GROUP)
                    .in(SysIamRelation::getSubjectId, allGroupIds)
                    .eq(SysIamRelation::getRelationType, IamRelationTypes.GROUP_ROLE)
                    .eq(SysIamRelation::getTargetType, IamRelationTypes.TARGET_ROLE));
            for (SysIamRelation rel : groupRoles) {
                Set<String> accounts = accountIdsByGroup.getOrDefault(rel.getSubjectId(), Set.of());
                for (String accountId : accounts) {
                    if (!matchesAccountType(accountTypeMap.get(accountId), rel.getAccountType())) {
                        continue;
                    }
                    roleIdsByAccount.computeIfAbsent(accountId, key -> new HashSet<>()).add(rel.getTargetId());
                    accountIdsByRole.computeIfAbsent(rel.getTargetId(), key -> new HashSet<>()).add(accountId);
                }
            }
        }

        // 批量加载角色编码并回填 roleIds / roleCodes
        Set<String> allRoleIds = roleIdsByAccount.values().stream().flatMap(Set::stream).collect(Collectors.toSet());
        Map<String, String> roleCodeMap = new HashMap<>();
        if (!allRoleIds.isEmpty()) {
            List<SysRole> roles = roleMapper.selectByIds(allRoleIds);
            for (SysRole role : roles) {
                roleCodeMap.put(role.getId(), role.getCode());
            }
        }
        for (Map.Entry<String, Set<String>> entry : roleIdsByAccount.entrySet()) {
            AccountAuthorization auth = result.get(entry.getKey());
            List<String> sortedRoleIds = entry.getValue().stream().sorted().toList();
            auth.setRoleIds(new ArrayList<>(sortedRoleIds));
            auth.setRoleCodes(sortedRoleIds.stream()
                    .map(roleCodeMap::get)
                    .filter(Objects::nonNull)
                    .sorted()
                    .toList());
        }

        // 组、部门去重排序；仅要成员关系时到此返回
        for (String accountId : uniqueIds) {
            AccountAuthorization auth = result.get(accountId);
            auth.setGroupIds(auth.getGroupIds().stream().distinct().sorted().toList());
            auth.setDeptIds(auth.getDeptIds().stream().distinct().sorted().toList());
        }

        if (!includeResourcePermissions) {
            return result;
        }

        // 合并账号/组/角色上的后台与客户端资源授予，展开为权限键
        Map<String, List<SysIamRelation>> resourceGrantsByAccount = listResourceGrantsByAccount(
                uniqueIds, accountIdsByGroup, accountIdsByRole, accountTypeMap);
        Map<String, List<LoginUser.PermissionGrant>> permissionGrantsByAccount =
                listPermissionGrantsByAccount(resourceGrantsByAccount, accountTypeMap);
        Map<String, List<SysIamRelation>> clientResourceGrantsByAccount = listClientResourceGrantsByAccount(
                uniqueIds, accountIdsByGroup, accountIdsByRole, accountTypeMap);
        Map<String, Set<String>> clientPermissionKeysByAccount =
                listClientPermissionKeysByAccount(clientResourceGrantsByAccount, accountTypeMap);

        // 回填资源 ID、权限授予明细与权限键；超管补通配 *:*:*
        for (String accountId : uniqueIds) {
            AccountAuthorization auth = result.get(accountId);
            List<SysIamRelation> grants = resourceGrantsByAccount.getOrDefault(accountId, List.of());
            auth.setResourceIds(grants.stream()
                    .map(SysIamRelation::getTargetId)
                    .distinct()
                    .sorted()
                    .toList());
            List<LoginUser.PermissionGrant> permissionGrants =
                    permissionGrantsByAccount.getOrDefault(accountId, List.of());
            auth.setPermissionGrants(permissionGrants);
            List<String> keys = permissionGrants.stream()
                    .map(LoginUser.PermissionGrant::getPermissionKey)
                    .distinct()
                    .sorted()
                    .toList();
            auth.setPermissionKeys(new ArrayList<>(keys));
            auth.setButtonCodes(new ArrayList<>(keys));
            List<SysIamRelation> clientGrants = clientResourceGrantsByAccount.getOrDefault(accountId, List.of());
            auth.setClientResourceIds(clientGrants.stream()
                    .map(SysIamRelation::getTargetId)
                    .distinct()
                    .sorted()
                    .toList());
            auth.setClientPermissionKeys(clientPermissionKeysByAccount.getOrDefault(accountId, Set.of()).stream()
                    .sorted()
                    .toList());
            if (auth.getRoleCodes().contains(IamRelationTypes.SUPER_ADMIN)) {
                if (!auth.getPermissionKeys().contains("*:*:*")) {
                    auth.getPermissionKeys().add("*:*:*");
                    auth.getButtonCodes().add("*:*:*");
                }
            }
        }
        return result;
    }

    private Map<String, List<SysIamRelation>> listResourceGrantsByAccount(
            List<String> accountIds,
            Map<String, Set<String>> accountIdsByGroup,
            Map<String, Set<String>> accountIdsByRole,
            Map<String, String> accountTypeMap) {
        return listTypedResourceGrantsByAccount(
                accountIds,
                accountIdsByGroup,
                accountIdsByRole,
                accountTypeMap,
                IamRelationTypes.SUBJECT_RESOURCE_GRANT,
                IamRelationTypes.TARGET_RESOURCE);
    }

    private Map<String, List<SysIamRelation>> listClientResourceGrantsByAccount(
            List<String> accountIds,
            Map<String, Set<String>> accountIdsByGroup,
            Map<String, Set<String>> accountIdsByRole,
            Map<String, String> accountTypeMap) {
        return listTypedResourceGrantsByAccount(
                accountIds,
                accountIdsByGroup,
                accountIdsByRole,
                accountTypeMap,
                IamRelationTypes.SUBJECT_CLIENT_RESOURCE_GRANT,
                IamRelationTypes.TARGET_CLIENT_RESOURCE);
    }

    private Map<String, List<SysIamRelation>> listTypedResourceGrantsByAccount(
            List<String> accountIds,
            Map<String, Set<String>> accountIdsByGroup,
            Map<String, Set<String>> accountIdsByRole,
            Map<String, String> accountTypeMap,
            String relationType,
            String targetType) {
        if (CollectionUtils.isEmpty(accountIds)
                && accountIdsByGroup.isEmpty()
                && accountIdsByRole.isEmpty()) {
            return Map.of();
        }
        Set<String> groupIds = accountIdsByGroup.keySet();
        Set<String> roleIds = accountIdsByRole.keySet();
        // 一次查出账号/组/角色主体上未过期且启用的资源授予
        List<SysIamRelation> grants = getBaseMapper().selectList(Wrappers.<SysIamRelation>lambdaQuery()
                .eq(SysIamRelation::getRelationType, relationType)
                .eq(SysIamRelation::getTargetType, targetType)
                .and(wrapper -> wrapper
                        .isNull(SysIamRelation::getStatus)
                        .or()
                        .eq(SysIamRelation::getStatus, IamRelationTypes.STATUS_ENABLED))
                .and(wrapper -> wrapper
                        .isNull(SysIamRelation::getExpiredAt)
                        .or()
                        .gt(SysIamRelation::getExpiredAt, OffsetDateTime.now()))
                .and(wrapper -> {
                    wrapper.and(accountWrapper -> accountWrapper
                            .eq(SysIamRelation::getSubjectType, IamRelationTypes.SUBJECT_ACCOUNT)
                            .in(SysIamRelation::getSubjectId, accountIds));
                    if (!groupIds.isEmpty()) {
                        wrapper.or(groupWrapper -> groupWrapper
                                .eq(SysIamRelation::getSubjectType, IamRelationTypes.SUBJECT_GROUP)
                                .in(SysIamRelation::getSubjectId, groupIds));
                    }
                    if (!roleIds.isEmpty()) {
                        wrapper.or(roleWrapper -> roleWrapper
                                .eq(SysIamRelation::getSubjectType, IamRelationTypes.SUBJECT_ROLE)
                                .in(SysIamRelation::getSubjectId, roleIds));
                    }
                }));

        // 按主体类型摊到账号：直接授予 / 经组继承 / 经角色继承，并过滤 accountType
        Map<String, List<SysIamRelation>> byAccount = new HashMap<>();
        for (SysIamRelation grant : grants) {
            if (IamRelationTypes.SUBJECT_ACCOUNT.equals(grant.getSubjectType())
                    && accountIds.contains(grant.getSubjectId())) {
                if (matchesAccountType(accountTypeMap.get(grant.getSubjectId()), grant.getAccountType())) {
                    byAccount.computeIfAbsent(grant.getSubjectId(), key -> new ArrayList<>()).add(grant);
                }
            } else if (IamRelationTypes.SUBJECT_GROUP.equals(grant.getSubjectType())
                    && groupIds.contains(grant.getSubjectId())) {
                for (String accountId : accountIdsByGroup.getOrDefault(grant.getSubjectId(), Set.of())) {
                    if (matchesAccountType(accountTypeMap.get(accountId), grant.getAccountType())) {
                        byAccount.computeIfAbsent(accountId, key -> new ArrayList<>()).add(grant);
                    }
                }
            } else if (IamRelationTypes.SUBJECT_ROLE.equals(grant.getSubjectType())
                    && roleIds.contains(grant.getSubjectId())) {
                for (String accountId : accountIdsByRole.getOrDefault(grant.getSubjectId(), Set.of())) {
                    if (matchesAccountType(accountTypeMap.get(accountId), grant.getAccountType())) {
                        byAccount.computeIfAbsent(accountId, key -> new ArrayList<>()).add(grant);
                    }
                }
            }
        }
        return byAccount;
    }

    private Map<String, List<LoginUser.PermissionGrant>> listPermissionGrantsByAccount(
            Map<String, List<SysIamRelation>> resourceGrantsByAccount,
            Map<String, String> accountTypeMap) {
        // CASCADE/DIRECT 边才展开权限；仅菜单的 DIRECT 通常无 RESOURCE_PERMISSION，不贡献权限
        Set<String> permissionResourceIds = resourceGrantsByAccount.values().stream()
                .flatMap(List::stream)
                .map(SysIamRelation::getTargetId)
                .collect(Collectors.toSet());
        // 批量加载资源上的权限绑定（含数据范围）
        Map<String, List<SysIamRelation>> permissionsByResource =
                loadPermissionsBySubject(IamRelationTypes.SUBJECT_RESOURCE, IamRelationTypes.RESOURCE_PERMISSION,
                        permissionResourceIds);

        // 按账号合并权限键，同 key 保留首条来源（含 dataScope）
        Map<String, List<LoginUser.PermissionGrant>> result = new HashMap<>();
        for (Map.Entry<String, List<SysIamRelation>> entry : resourceGrantsByAccount.entrySet()) {
            String accountType = accountTypeMap.get(entry.getKey());
            Map<String, LoginUser.PermissionGrant> merged = new LinkedHashMap<>();
            for (SysIamRelation grant : entry.getValue()) {
                if (!expandsPermissionKeys(grant.getGrantMode())) {
                    continue;
                }
                for (SysIamRelation permission : permissionsByResource.getOrDefault(grant.getTargetId(), List.of())) {
                    if (!StringUtils.hasText(permission.getTargetKey())
                            || !matchesAccountType(accountType, permission.getAccountType())) {
                        continue;
                    }
                    LoginUser.PermissionGrant pg = new LoginUser.PermissionGrant();
                    pg.setPermissionKey(permission.getTargetKey());
                    pg.setDataScope(permission.getDataScope());
                    pg.setCustomScopeDeptIds(permission.getCustomScopeDeptIds() == null
                            ? List.of()
                            : permission.getCustomScopeDeptIds());
                    pg.setSourceType(grant.getSubjectType());
                    pg.setSourceId(grant.getSubjectId());
                    merged.putIfAbsent(permission.getTargetKey(), pg);
                }
            }
            result.put(entry.getKey(), new ArrayList<>(merged.values()));
        }
        return result;
    }

    private Map<String, Set<String>> listClientPermissionKeysByAccount(
            Map<String, List<SysIamRelation>> clientResourceGrantsByAccount,
            Map<String, String> accountTypeMap) {
        // 客户端资源授予 → 批量取 CLIENT_RESOURCE_PERMISSION → 按账号汇总权限键
        Set<String> permissionResourceIds = clientResourceGrantsByAccount.values().stream()
                .flatMap(List::stream)
                .map(SysIamRelation::getTargetId)
                .collect(Collectors.toSet());
        Map<String, List<SysIamRelation>> permissionsByResource = loadPermissionsBySubject(
                IamRelationTypes.SUBJECT_CLIENT_RESOURCE,
                IamRelationTypes.CLIENT_RESOURCE_PERMISSION,
                permissionResourceIds);
        Map<String, Set<String>> result = new HashMap<>();
        for (Map.Entry<String, List<SysIamRelation>> entry : clientResourceGrantsByAccount.entrySet()) {
            String accountType = accountTypeMap.get(entry.getKey());
            Set<String> keys = new HashSet<>();
            for (SysIamRelation grant : entry.getValue()) {
                if (!expandsPermissionKeys(grant.getGrantMode())) {
                    continue;
                }
                for (SysIamRelation permission : permissionsByResource.getOrDefault(grant.getTargetId(), List.of())) {
                    if (StringUtils.hasText(permission.getTargetKey())
                            && matchesAccountType(accountType, permission.getAccountType())) {
                        keys.add(permission.getTargetKey());
                    }
                }
            }
            result.put(entry.getKey(), keys);
        }
        return result;
    }

    private Map<String, List<SysIamRelation>> loadPermissionsBySubject(
            String subjectType, String relationType, Set<String> subjectIds) {
        Map<String, List<SysIamRelation>> permissionsByResource = new HashMap<>();
        if (CollectionUtils.isEmpty(subjectIds)) {
            return permissionsByResource;
        }
        List<SysIamRelation> permissions = getBaseMapper().selectList(Wrappers.<SysIamRelation>lambdaQuery()
                .eq(SysIamRelation::getSubjectType, subjectType)
                .eq(SysIamRelation::getRelationType, relationType)
                .eq(SysIamRelation::getTargetType, IamRelationTypes.TARGET_PERMISSION)
                .in(SysIamRelation::getSubjectId, subjectIds));
        for (SysIamRelation permission : permissions) {
            permissionsByResource.computeIfAbsent(permission.getSubjectId(), key -> new ArrayList<>())
                    .add(permission);
        }
        return permissionsByResource;
    }

    private static boolean expandsPermissionKeys(String grantMode) {
        return !StringUtils.hasText(grantMode)
                || IamRelationTypes.GRANT_CASCADE.equals(grantMode)
                || IamRelationTypes.GRANT_DIRECT.equals(grantMode);
    }

    @Override
    public List<String> listTargetIds(String subjectType, String subjectId, String relationType) {
        return listTargetIds(subjectType, subjectId, relationType, null);
    }

    @Override
    public List<String> listTargetIds(String subjectType, String subjectId, String relationType, String accountType) {
        return getBaseMapper().selectList(Wrappers.<SysIamRelation>lambdaQuery()
                        .eq(SysIamRelation::getSubjectType, subjectType)
                        .eq(SysIamRelation::getSubjectId, subjectId)
                        .eq(SysIamRelation::getRelationType, relationType)
                        .eq(StringUtils.hasText(accountType), SysIamRelation::getAccountType, accountType))
                .stream()
                .map(SysIamRelation::getTargetId)
                .filter(StringUtils::hasText)
                .distinct()
                .toList();
    }

    @Override
    public List<String> listSubjectIds(String relationType, String targetType, String targetId) {
        return getBaseMapper().selectList(Wrappers.<SysIamRelation>lambdaQuery()
                        .eq(SysIamRelation::getRelationType, relationType)
                        .eq(SysIamRelation::getTargetType, targetType)
                        .eq(SysIamRelation::getTargetId, targetId))
                .stream()
                .map(SysIamRelation::getSubjectId)
                .filter(StringUtils::hasText)
                .distinct()
                .toList();
    }

    @Override
    public List<SysDeptGrantResult> listAccountDepts(String accountId) {
        String accountType = resolveAccountType(accountId);
        return getBaseMapper().selectList(Wrappers.<SysIamRelation>lambdaQuery()
                        .eq(SysIamRelation::getSubjectType, IamRelationTypes.SUBJECT_ACCOUNT)
                        .eq(SysIamRelation::getSubjectId, accountId)
                        .eq(SysIamRelation::getRelationType, IamRelationTypes.ACCOUNT_DEPT)
                        .eq(SysIamRelation::getAccountType, accountType))
                .stream()
                .map(rel -> {
                    SysDeptGrantResult info = new SysDeptGrantResult();
                    info.setDeptId(rel.getTargetId());
                    info.setIsPrimary(Boolean.TRUE.equals(rel.getIsPrimary()));
                    return info;
                })
                .toList();
    }

    @Override
    public List<SysResourceGrantResult> listSubjectResourceGrants(String subjectType, String subjectId) {
        return listSubjectResourceGrants(subjectType, subjectId, null);
    }

    @Override
    public List<SysResourceGrantResult> listSubjectResourceGrants(
            String subjectType, String subjectId, String accountType) {
        return listSubjectTypedResourceGrants(
                subjectType,
                subjectId,
                accountType,
                IamRelationTypes.SUBJECT_RESOURCE_GRANT,
                IamRelationTypes.TARGET_RESOURCE,
                IamRelationTypes.SUBJECT_RESOURCE,
                IamRelationTypes.RESOURCE_PERMISSION);
    }

    @Override
    public List<SysResourceGrantResult> listSubjectClientResourceGrants(
            String subjectType, String subjectId, String accountType) {
        return listSubjectTypedResourceGrants(
                subjectType,
                subjectId,
                accountType,
                IamRelationTypes.SUBJECT_CLIENT_RESOURCE_GRANT,
                IamRelationTypes.TARGET_CLIENT_RESOURCE,
                IamRelationTypes.SUBJECT_CLIENT_RESOURCE,
                IamRelationTypes.CLIENT_RESOURCE_PERMISSION);
    }

    private List<SysResourceGrantResult> listSubjectTypedResourceGrants(
            String subjectType,
            String subjectId,
            String accountType,
            String grantRelationType,
            String targetType,
            String permissionSubjectType,
            String permissionRelationType) {
        // 查主体上的资源授予边
        var query = Wrappers.<SysIamRelation>lambdaQuery()
                .eq(SysIamRelation::getSubjectType, subjectType)
                .eq(SysIamRelation::getSubjectId, subjectId)
                .eq(SysIamRelation::getRelationType, grantRelationType)
                .eq(SysIamRelation::getTargetType, targetType)
                .eq(StringUtils.hasText(accountType), SysIamRelation::getAccountType, accountType)
                .orderByAsc(SysIamRelation::getId);
        List<SysIamRelation> grants = getBaseMapper().selectList(query);
        if (grants.isEmpty()) {
            return List.of();
        }
        List<String> resourceIds = grants.stream().map(SysIamRelation::getTargetId).distinct().toList();
        // 批量加载各资源绑定的权限键
        Map<String, List<String>> permissionMap = new HashMap<>();
        List<SysIamRelation> permissions = getBaseMapper().selectList(Wrappers.<SysIamRelation>lambdaQuery()
                .eq(SysIamRelation::getSubjectType, permissionSubjectType)
                .eq(SysIamRelation::getRelationType, permissionRelationType)
                .in(SysIamRelation::getSubjectId, resourceIds)
                .eq(StringUtils.hasText(accountType), SysIamRelation::getAccountType, accountType));
        for (SysIamRelation permission : permissions) {
            permissionMap.computeIfAbsent(permission.getSubjectId(), key -> new ArrayList<>())
                    .add(permission.getTargetKey());
        }

        // 加载资源元数据：类型 / 父节点 / code（按钮回落用）
        boolean client = IamRelationTypes.TARGET_CLIENT_RESOURCE.equals(targetType);
        Map<String, String> resourceTypeById = new HashMap<>();
        Map<String, String> parentIdById = new HashMap<>();
        Map<String, String> codeById = new HashMap<>();
        if (client) {
            for (SysClientResource resource : clientResourceMapper.selectByIds(resourceIds)) {
                resourceTypeById.put(resource.getId(), resource.getResourceType());
                parentIdById.put(resource.getId(), resource.getParentId());
                codeById.put(resource.getId(), resource.getCode());
            }
        } else {
            for (SysResource resource : resourceMapper.selectByIds(resourceIds)) {
                resourceTypeById.put(resource.getId(), resource.getResourceType());
                parentIdById.put(resource.getId(), resource.getParentId());
                codeById.put(resource.getId(), resource.getCode());
            }
        }

        // 按钮/动作权限上卷到父菜单；菜单本身占位（可无 permissionKeys）
        Map<String, Set<String>> grantMap = new LinkedHashMap<>();
        for (String resourceId : resourceIds) {
            String resourceType = resourceTypeById.get(resourceId);
            if ("BUTTON".equals(resourceType) || "ACTION".equals(resourceType)) {
                String parentId = StringUtils.hasText(parentIdById.get(resourceId))
                        ? parentIdById.get(resourceId)
                        : resourceId;
                Set<String> keys = grantMap.computeIfAbsent(parentId, key -> new HashSet<>());
                List<String> mapped = permissionMap.get(resourceId);
                if (mapped != null && !mapped.isEmpty()) {
                    keys.addAll(mapped);
                } else if (StringUtils.hasText(codeById.get(resourceId))) {
                    keys.add(codeById.get(resourceId));
                }
            } else {
                grantMap.computeIfAbsent(resourceId, key -> new HashSet<>());
            }
        }
        List<SysResourceGrantResult> result = new ArrayList<>();
        for (Map.Entry<String, Set<String>> entry : grantMap.entrySet()) {
            SysResourceGrantResult info = new SysResourceGrantResult();
            info.setResourceId(entry.getKey());
            info.setPermissionKeys(entry.getValue().stream().sorted().toList());
            result.add(info);
        }
        return result;
    }

    @Override
    @Transactional
    @Lock4j(keys = {"'iam:rel:account:' + #accountId"}, expire = 30000, acquireTimeout = 5000)
    public void replaceAccountRoles(String accountId, List<String> roleIds) {
        DataSourceSticky.mark();
        assertAccountAccessibleIfLoggedIn(accountId);
        // 先删后插：替换账号-角色
        String accountType = resolveAccountType(accountId);
        deleteSubjectRelations(IamRelationTypes.SUBJECT_ACCOUNT, accountId, IamRelationTypes.ACCOUNT_ROLE, accountType);
        if (CollectionUtils.isEmpty(roleIds)) {
            LoginHelper.logoutAccount(accountId);
            return;
        }
        List<SysIamRelation> relations = new ArrayList<>();
        for (String roleId : roleIds.stream().distinct().toList()) {
            relations.add(newRelation(
                    IamRelationTypes.SUBJECT_ACCOUNT,
                    accountId,
                    accountType,
                    IamRelationTypes.ACCOUNT_ROLE,
                    IamRelationTypes.TARGET_ROLE,
                    roleId));
        }
        saveRelations(relations);
        LoginHelper.logoutAccount(accountId);
    }

    @Override
    @Transactional
    @Lock4j(keys = {"'iam:rel:account:' + #accountId"}, expire = 30000, acquireTimeout = 5000)
    public void replaceAccountGroups(String accountId, List<String> groupIds) {
        DataSourceSticky.mark();
        assertAccountAccessibleIfLoggedIn(accountId);
        // 先删后插：替换账号-用户组
        String accountType = resolveAccountType(accountId);
        deleteSubjectRelations(IamRelationTypes.SUBJECT_ACCOUNT, accountId, IamRelationTypes.ACCOUNT_GROUP, accountType);
        if (CollectionUtils.isEmpty(groupIds)) {
            LoginHelper.logoutAccount(accountId);
            return;
        }
        List<SysIamRelation> relations = new ArrayList<>();
        for (String groupId : groupIds.stream().distinct().toList()) {
            relations.add(newRelation(
                    IamRelationTypes.SUBJECT_ACCOUNT,
                    accountId,
                    accountType,
                    IamRelationTypes.ACCOUNT_GROUP,
                    IamRelationTypes.TARGET_GROUP,
                    groupId));
        }
        saveRelations(relations);
        LoginHelper.logoutAccount(accountId);
    }

    @Override
    @Transactional
    @Lock4j(keys = {"'iam:rel:account:' + #accountId"}, expire = 30000, acquireTimeout = 5000)
    public void replaceAccountDepts(String accountId, List<SysDeptGrantResult> grantInfoList) {
        DataSourceSticky.mark();
        assertAccountAccessibleIfLoggedIn(accountId);
        // 先删后插：替换账号-部门
        String accountType = resolveAccountType(accountId);
        deleteSubjectRelations(IamRelationTypes.SUBJECT_ACCOUNT, accountId, IamRelationTypes.ACCOUNT_DEPT, accountType);
        if (CollectionUtils.isEmpty(grantInfoList)) {
            LoginHelper.logoutAccount(accountId);
            return;
        }
        List<SysIamRelation> relations = new ArrayList<>();
        for (SysDeptGrantResult info : grantInfoList) {
            SysIamRelation rel = newRelation(
                    IamRelationTypes.SUBJECT_ACCOUNT,
                    accountId,
                    accountType,
                    IamRelationTypes.ACCOUNT_DEPT,
                    IamRelationTypes.TARGET_DEPT,
                    info.getDeptId());
            rel.setIsPrimary(Boolean.TRUE.equals(info.getIsPrimary()));
            relations.add(rel);
        }
        saveRelations(relations);
        LoginHelper.logoutAccount(accountId);
    }

    @Override
    @Transactional
    @Lock4j(keys = {"'iam:rel:' + #subjectType + ':' + #subjectId"}, expire = 30000, acquireTimeout = 5000)
    public void replaceSubjectResourceGrants(
            String subjectType, String subjectId, List<SysResourceGrantResult> grants, String accountType) {
        DataSourceSticky.mark();
        // 先删后插：替换主体资源授予
        String resolvedType = normalizeAccountType(accountType);
        deleteSubjectRelations(subjectType, subjectId, IamRelationTypes.SUBJECT_RESOURCE_GRANT, resolvedType);
        if (CollectionUtils.isEmpty(grants)) {
            return;
        }
        // 收集勾选的菜单/资源 ID 与权限键
        List<String> originalResourceIds = grants.stream()
                .map(SysResourceGrantResult::getResourceId)
                .filter(StringUtils::hasText)
                .distinct()
                .toList();
        Set<String> originalSet = new HashSet<>(originalResourceIds);
        List<String> permissionKeys = grants.stream()
                .filter(item -> item.getPermissionKeys() != null)
                .flatMap(item -> item.getPermissionKeys().stream())
                .filter(StringUtils::hasText)
                .distinct()
                .toList();

        // 校验勾选资源存在
        if (!originalResourceIds.isEmpty()) {
            Set<String> existing = resourceMapper.selectByIds(originalResourceIds).stream()
                    .map(SysResource::getId)
                    .collect(Collectors.toSet());
            if (existing.size() != originalResourceIds.size()) {
                throw new BizException("Resource not found");
            }
        }

        // 权限键解析为按钮/动作资源 ID，并入授予目标集合
        List<String> resourceIds = new ArrayList<>(originalResourceIds);
        if (!permissionKeys.isEmpty()) {
            Map<String, Set<String>> permissionResourceMap = resolvePermissionResourceIds(
                    permissionKeys,
                    resolvedType,
                    IamRelationTypes.SUBJECT_RESOURCE,
                    IamRelationTypes.RESOURCE_PERMISSION,
                    false);
            for (String permissionKey : permissionKeys) {
                Set<String> ids = permissionResourceMap.get(permissionKey);
                if (ids == null || ids.isEmpty()) {
                    throw new BizException("Permission resource not found: " + permissionKey);
                }
                resourceIds.addAll(ids);
            }
        }
        resourceIds = resourceIds.stream().filter(StringUtils::hasText).distinct().toList();

        // 无权限键 → CASCADE 整资源；权限键解析出的边 → CASCADE；勾选菜单本身 → DIRECT
        List<SysIamRelation> relations = new ArrayList<>(resourceIds.size());
        for (String resourceId : resourceIds) {
            boolean cascadeWhole = permissionKeys.isEmpty() && originalSet.contains(resourceId);
            boolean permissionEdge = !originalSet.contains(resourceId);
            SysIamRelation rel = newRelation(
                    subjectType,
                    subjectId,
                    resolvedType,
                    IamRelationTypes.SUBJECT_RESOURCE_GRANT,
                    IamRelationTypes.TARGET_RESOURCE,
                    resourceId);
            if (cascadeWhole) {
                rel.setGrantMode(IamRelationTypes.GRANT_CASCADE);
            } else if (permissionEdge) {
                rel.setGrantMode(IamRelationTypes.GRANT_CASCADE);
            } else {
                rel.setGrantMode(IamRelationTypes.GRANT_DIRECT);
            }
            relations.add(rel);
        }
        saveRelations(relations);
        if (IamRelationTypes.SUBJECT_ACCOUNT.equals(subjectType)) {
            LoginHelper.logoutAccount(subjectId);
        }
    }

    @Override
    @Transactional
    @Lock4j(keys = {"'iam:rel:client:' + #subjectType + ':' + #subjectId"}, expire = 30000, acquireTimeout = 5000)
    public void replaceSubjectClientResourceGrants(
            String subjectType, String subjectId, List<SysResourceGrantResult> grants, String accountType) {
        DataSourceSticky.mark();
        // 先删后插：替换主体客户端资源授予（流程同后台资源）
        String resolvedType = normalizeAccountType(accountType);
        deleteSubjectRelations(subjectType, subjectId, IamRelationTypes.SUBJECT_CLIENT_RESOURCE_GRANT, resolvedType);
        if (CollectionUtils.isEmpty(grants)) {
            return;
        }
        List<String> originalResourceIds = grants.stream()
                .map(SysResourceGrantResult::getResourceId)
                .filter(StringUtils::hasText)
                .distinct()
                .toList();
        Set<String> originalSet = new HashSet<>(originalResourceIds);
        List<String> permissionKeys = grants.stream()
                .filter(item -> item.getPermissionKeys() != null)
                .flatMap(item -> item.getPermissionKeys().stream())
                .filter(StringUtils::hasText)
                .distinct()
                .toList();

        if (!originalResourceIds.isEmpty()) {
            Set<String> existing = clientResourceMapper.selectByIds(originalResourceIds).stream()
                    .map(SysClientResource::getId)
                    .collect(Collectors.toSet());
            if (existing.size() != originalResourceIds.size()) {
                throw new BizException("Client resource not found");
            }
        }

        // 权限键解析为客户端按钮/动作资源并并入授予集合
        List<String> resourceIds = new ArrayList<>(originalResourceIds);
        if (!permissionKeys.isEmpty()) {
            Map<String, Set<String>> permissionResourceMap = resolvePermissionResourceIds(
                    permissionKeys,
                    resolvedType,
                    IamRelationTypes.SUBJECT_CLIENT_RESOURCE,
                    IamRelationTypes.CLIENT_RESOURCE_PERMISSION,
                    true);
            for (String permissionKey : permissionKeys) {
                Set<String> ids = permissionResourceMap.get(permissionKey);
                if (ids == null || ids.isEmpty()) {
                    throw new BizException("Client permission resource not found: " + permissionKey);
                }
                resourceIds.addAll(ids);
            }
        }
        resourceIds = resourceIds.stream().filter(StringUtils::hasText).distinct().toList();

        List<SysIamRelation> relations = new ArrayList<>(resourceIds.size());
        for (String resourceId : resourceIds) {
            boolean cascadeWhole = permissionKeys.isEmpty() && originalSet.contains(resourceId);
            boolean permissionEdge = !originalSet.contains(resourceId);
            SysIamRelation rel = newRelation(
                    subjectType,
                    subjectId,
                    resolvedType,
                    IamRelationTypes.SUBJECT_CLIENT_RESOURCE_GRANT,
                    IamRelationTypes.TARGET_CLIENT_RESOURCE,
                    resourceId);
            if (cascadeWhole) {
                rel.setGrantMode(IamRelationTypes.GRANT_CASCADE);
            } else if (permissionEdge) {
                rel.setGrantMode(IamRelationTypes.GRANT_CASCADE);
            } else {
                rel.setGrantMode(IamRelationTypes.GRANT_DIRECT);
            }
            relations.add(rel);
        }
        saveRelations(relations);
        if (IamRelationTypes.SUBJECT_ACCOUNT.equals(subjectType)) {
            LoginHelper.logoutAccount(subjectId);
        }
    }

    private Map<String, Set<String>> resolvePermissionResourceIds(
            List<String> permissionKeys,
            String accountType,
            String permissionSubjectType,
            String permissionRelationType,
            boolean client) {
        // 先从权限绑定关系解析 subjectId（资源 ID）
        Map<String, Set<String>> permissionResourceMap = new HashMap<>();
        List<SysIamRelation> permissionRows = getBaseMapper().selectList(Wrappers.<SysIamRelation>lambdaQuery()
                .eq(SysIamRelation::getSubjectType, permissionSubjectType)
                .eq(SysIamRelation::getRelationType, permissionRelationType)
                .eq(SysIamRelation::getTargetType, IamRelationTypes.TARGET_PERMISSION)
                .eq(SysIamRelation::getAccountType, accountType)
                .in(SysIamRelation::getTargetKey, permissionKeys));
        for (SysIamRelation row : permissionRows) {
            if (StringUtils.hasText(row.getTargetKey()) && StringUtils.hasText(row.getSubjectId())) {
                permissionResourceMap.computeIfAbsent(row.getTargetKey(), key -> new HashSet<>())
                        .add(row.getSubjectId());
            }
        }
        // 再按 code 兜底匹配 BUTTON/ACTION 资源（未绑权限行时仍可授权）
        if (client) {
            clientResourceMapper.selectList(Wrappers.<SysClientResource>lambdaQuery()
                            .in(SysClientResource::getCode, permissionKeys)
                            .in(SysClientResource::getResourceType, List.of("BUTTON", "ACTION")))
                    .forEach(resource -> permissionResourceMap
                            .computeIfAbsent(resource.getCode(), key -> new HashSet<>())
                            .add(resource.getId()));
        } else {
            resourceMapper.selectList(Wrappers.<SysResource>lambdaQuery()
                            .in(SysResource::getCode, permissionKeys)
                            .in(SysResource::getResourceType, List.of("BUTTON", "ACTION")))
                    .forEach(resource -> permissionResourceMap
                            .computeIfAbsent(resource.getCode(), key -> new HashSet<>())
                            .add(resource.getId()));
        }
        return permissionResourceMap;
    }

    @Override
    @Transactional
    @Lock4j(keys = {"'iam:rel:role:' + #roleId"}, expire = 30000, acquireTimeout = 5000)
    public void replaceRoleUsers(String roleId, List<String> accountIds) {
        DataSourceSticky.mark();
        SysRole role = roleMapper.selectById(roleId);
        if (role == null) {
            throw new BizException(404, "Role not found");
        }
        dataScopeResolver.assertOwnerOrDeptAccessible(
                role.getCreatedBy(), role.getOwnerDeptId(), "iam:role:page");
        Set<String> affected = new HashSet<>(listSubjectIdsByTarget(
                IamRelationTypes.ACCOUNT_ROLE, IamRelationTypes.TARGET_ROLE, roleId));
        if (accountIds != null) {
            accountIds.stream().filter(StringUtils::hasText).forEach(affected::add);
        }
        // 先按角色目标删光 ACCOUNT_ROLE，再按账号类型批量重建
        getBaseMapper().delete(Wrappers.<SysIamRelation>lambdaQuery()
                .eq(SysIamRelation::getRelationType, IamRelationTypes.ACCOUNT_ROLE)
                .eq(SysIamRelation::getTargetType, IamRelationTypes.TARGET_ROLE)
                .eq(SysIamRelation::getTargetId, roleId));
        if (!CollectionUtils.isEmpty(accountIds)) {
            Map<String, String> accountTypeMap = loadAccountTypeMap(accountIds);
            List<SysIamRelation> relations = new ArrayList<>();
            for (String accountId : accountIds.stream().distinct().toList()) {
                String accountType = accountTypeMap.get(accountId);
                if (!StringUtils.hasText(accountType)) {
                    throw new BizException("Account not found: " + accountId);
                }
                relations.add(newRelation(
                        IamRelationTypes.SUBJECT_ACCOUNT,
                        accountId,
                        accountType,
                        IamRelationTypes.ACCOUNT_ROLE,
                        IamRelationTypes.TARGET_ROLE,
                        roleId));
            }
            saveRelations(relations);
        }
        logoutAccounts(affected);
    }

    @Override
    @Transactional
    @Lock4j(keys = {"'iam:rel:group:' + #groupId"}, expire = 30000, acquireTimeout = 5000)
    public void replaceGroupUsers(String groupId, List<String> accountIds) {
        DataSourceSticky.mark();
        Set<String> affected = new HashSet<>(listSubjectIdsByTarget(
                IamRelationTypes.ACCOUNT_GROUP, IamRelationTypes.TARGET_GROUP, groupId));
        if (accountIds != null) {
            accountIds.stream().filter(StringUtils::hasText).forEach(affected::add);
        }
        // 先按组目标删光 ACCOUNT_GROUP，再按账号类型批量重建
        getBaseMapper().delete(Wrappers.<SysIamRelation>lambdaQuery()
                .eq(SysIamRelation::getRelationType, IamRelationTypes.ACCOUNT_GROUP)
                .eq(SysIamRelation::getTargetType, IamRelationTypes.TARGET_GROUP)
                .eq(SysIamRelation::getTargetId, groupId));
        if (!CollectionUtils.isEmpty(accountIds)) {
            Map<String, String> accountTypeMap = loadAccountTypeMap(accountIds);
            List<SysIamRelation> relations = new ArrayList<>();
            for (String accountId : accountIds.stream().distinct().toList()) {
                String accountType = accountTypeMap.get(accountId);
                if (!StringUtils.hasText(accountType)) {
                    throw new BizException("Account not found: " + accountId);
                }
                relations.add(newRelation(
                        IamRelationTypes.SUBJECT_ACCOUNT,
                        accountId,
                        accountType,
                        IamRelationTypes.ACCOUNT_GROUP,
                        IamRelationTypes.TARGET_GROUP,
                        groupId));
            }
            saveRelations(relations);
        }
        logoutAccounts(affected);
    }

    @Override
    @Transactional
    @Lock4j(keys = {"'iam:rel:group-roles:' + #groupId"}, expire = 30000, acquireTimeout = 5000)
    public void replaceGroupRoles(String groupId, List<String> roleIds, String accountType) {
        DataSourceSticky.mark();
        String resolvedType = normalizeAccountType(accountType);
        Set<String> affected = new HashSet<>(listSubjectIdsByTarget(
                IamRelationTypes.ACCOUNT_GROUP, IamRelationTypes.TARGET_GROUP, groupId));
        deleteSubjectRelations(IamRelationTypes.SUBJECT_GROUP, groupId, IamRelationTypes.GROUP_ROLE, resolvedType);
        if (!CollectionUtils.isEmpty(roleIds)) {
            List<SysIamRelation> relations = new ArrayList<>();
            for (String roleId : roleIds.stream().distinct().toList()) {
                relations.add(newRelation(
                        IamRelationTypes.SUBJECT_GROUP,
                        groupId,
                        resolvedType,
                        IamRelationTypes.GROUP_ROLE,
                        IamRelationTypes.TARGET_ROLE,
                        roleId));
            }
            saveRelations(relations);
        }
        logoutAccounts(affected);
    }

    private List<String> listSubjectIdsByTarget(String relationType, String targetType, String targetId) {
        return getBaseMapper().selectList(Wrappers.<SysIamRelation>lambdaQuery()
                        .eq(SysIamRelation::getRelationType, relationType)
                        .eq(SysIamRelation::getTargetType, targetType)
                        .eq(SysIamRelation::getTargetId, targetId)
                        .eq(SysIamRelation::getSubjectType, IamRelationTypes.SUBJECT_ACCOUNT))
                .stream()
                .map(SysIamRelation::getSubjectId)
                .filter(StringUtils::hasText)
                .distinct()
                .toList();
    }

    private static void logoutAccounts(Collection<String> accountIds) {
        if (accountIds == null || accountIds.isEmpty()) {
            return;
        }
        for (String accountId : accountIds) {
            if (StringUtils.hasText(accountId)) {
                LoginHelper.logoutAccount(accountId);
            }
        }
    }

    @Override
    @Transactional
    public void bindResourcePermission(
            String resourceId,
            String permissionKey,
            String accountType,
            String dataScope,
            List<String> customScopeDeptIds,
            Integer sort,
            String description) {
        // 同资源+权限键+账号类型：先删旧绑定再插入（含数据范围）
        String resolvedType = normalizeAccountType(accountType);
        getBaseMapper().delete(Wrappers.<SysIamRelation>lambdaQuery()
                .eq(SysIamRelation::getSubjectType, IamRelationTypes.SUBJECT_RESOURCE)
                .eq(SysIamRelation::getSubjectId, resourceId)
                .eq(SysIamRelation::getRelationType, IamRelationTypes.RESOURCE_PERMISSION)
                .eq(SysIamRelation::getTargetKey, permissionKey)
                .eq(SysIamRelation::getAccountType, resolvedType));
        SysIamRelation rel = newRelation(
                IamRelationTypes.SUBJECT_RESOURCE,
                resourceId,
                resolvedType,
                IamRelationTypes.RESOURCE_PERMISSION,
                IamRelationTypes.TARGET_PERMISSION,
                null);
        rel.setTargetKey(permissionKey);
        rel.setDataScope(dataScope);
        rel.setCustomScopeDeptIds(customScopeDeptIds);
        rel.setSort(sort);
        rel.setDescription(description);
        getBaseMapper().insert(rel);
    }

    @Override
    @Transactional
    public void bindClientResourcePermission(
            String resourceId,
            String permissionKey,
            String accountType,
            String dataScope,
            List<String> customScopeDeptIds,
            Integer sort,
            String description) {
        // 同客户端资源+权限键+账号类型：先删旧绑定再插入
        String resolvedType = normalizeAccountType(accountType);
        getBaseMapper().delete(Wrappers.<SysIamRelation>lambdaQuery()
                .eq(SysIamRelation::getSubjectType, IamRelationTypes.SUBJECT_CLIENT_RESOURCE)
                .eq(SysIamRelation::getSubjectId, resourceId)
                .eq(SysIamRelation::getRelationType, IamRelationTypes.CLIENT_RESOURCE_PERMISSION)
                .eq(SysIamRelation::getTargetKey, permissionKey)
                .eq(SysIamRelation::getAccountType, resolvedType));
        SysIamRelation rel = newRelation(
                IamRelationTypes.SUBJECT_CLIENT_RESOURCE,
                resourceId,
                resolvedType,
                IamRelationTypes.CLIENT_RESOURCE_PERMISSION,
                IamRelationTypes.TARGET_PERMISSION,
                null);
        rel.setTargetKey(permissionKey);
        rel.setDataScope(dataScope);
        rel.setCustomScopeDeptIds(customScopeDeptIds);
        rel.setSort(sort);
        rel.setDescription(description);
        getBaseMapper().insert(rel);
    }

    @Override
    public void deleteSubjectRelations(String subjectType, String subjectId, String relationType) {
        deleteSubjectRelations(subjectType, subjectId, relationType, null);
    }

    @Override
    public void deleteSubjectRelations(String subjectType, String subjectId, String relationType, String accountType) {
        getBaseMapper().delete(Wrappers.<SysIamRelation>lambdaQuery()
                .eq(SysIamRelation::getSubjectType, subjectType)
                .eq(SysIamRelation::getSubjectId, subjectId)
                .eq(SysIamRelation::getRelationType, relationType)
                .eq(StringUtils.hasText(accountType), SysIamRelation::getAccountType, accountType));
    }

    @Override
    public void deleteSubjectRelations(String subjectType, Collection<String> subjectIds, String relationType) {
        if (CollectionUtils.isEmpty(subjectIds)) {
            return;
        }
        // 分批 IN 删除，避免超长 SQL
        for (List<String> batch : BatchPartition.partition(subjectIds)) {
            getBaseMapper().delete(Wrappers.<SysIamRelation>lambdaQuery()
                    .eq(SysIamRelation::getSubjectType, subjectType)
                    .eq(SysIamRelation::getRelationType, relationType)
                    .in(SysIamRelation::getSubjectId, batch));
        }
    }

    @Override
    public void deleteBySubjectIds(String subjectType, Collection<String> subjectIds) {
        if (CollectionUtils.isEmpty(subjectIds)) {
            return;
        }
        for (List<String> batch : BatchPartition.partition(subjectIds)) {
            getBaseMapper().delete(Wrappers.<SysIamRelation>lambdaQuery()
                    .eq(SysIamRelation::getSubjectType, subjectType)
                    .in(SysIamRelation::getSubjectId, batch));
        }
    }

    @Override
    public void deleteByTargetIds(String targetType, Collection<String> targetIds) {
        if (CollectionUtils.isEmpty(targetIds)) {
            return;
        }
        for (List<String> batch : BatchPartition.partition(targetIds)) {
            getBaseMapper().delete(Wrappers.<SysIamRelation>lambdaQuery()
                    .eq(SysIamRelation::getTargetType, targetType)
                    .in(SysIamRelation::getTargetId, batch));
        }
    }

    private void saveRelations(List<SysIamRelation> relations) {
        if (relations == null || relations.isEmpty()) {
            return;
        }
        // 分批 saveBatch，避免单次过大
        int size = BatchPartition.DEFAULT_SIZE;
        for (int i = 0; i < relations.size(); i += size) {
            this.saveBatch(relations.subList(i, Math.min(i + size, relations.size())));
        }
    }

    private SysIamRelation newRelation(
            String subjectType,
            String subjectId,
            String accountType,
            String relationType,
            String targetType,
            String targetId) {
        SysIamRelation rel = new SysIamRelation();
        rel.setSubjectType(subjectType);
        rel.setSubjectId(subjectId);
        rel.setAccountType(normalizeAccountType(accountType));
        rel.setRelationType(relationType);
        rel.setTargetType(targetType);
        rel.setTargetId(targetId);
        rel.setStatus(IamRelationTypes.STATUS_ENABLED);
        return rel;
    }

    private Map<String, String> loadAccountTypeMap(Collection<String> accountIds) {
        List<String> uniqueIds = accountIds.stream().filter(StringUtils::hasText).distinct().toList();
        if (uniqueIds.isEmpty()) {
            return Map.of();
        }
        Map<String, String> map = new HashMap<>();
        for (SysAccount account : accountMapper.selectByIds(uniqueIds)) {
            map.put(account.getId(), account.getAccountType());
        }
        return map;
    }

    private String resolveAccountType(String accountId) {
        SysAccount account = accountMapper.selectById(accountId);
        if (account == null || !StringUtils.hasText(account.getAccountType())) {
            throw new BizException(404, "Account not found");
        }
        return normalizeAccountType(account.getAccountType());
    }

    private static String normalizeAccountType(String accountType) {
        if (!StringUtils.hasText(accountType)) {
            return AccountType.ADMIN.name();
        }
        return accountType.trim().toUpperCase();
    }

    private static boolean matchesAccountType(String expected, String actual) {
        if (!StringUtils.hasText(expected)) {
            return false;
        }
        return expected.equalsIgnoreCase(actual);
    }

    /** 有登录态时校验账号数据范围（注册等无登录链路跳过）。 */
    private void assertAccountAccessibleIfLoggedIn(String accountId) {
        if (LoginHelper.currentUser().isEmpty()) {
            return;
        }
        dataScopeResolver.assertAccountAccessible(accountId, "iam:account:page");
    }
}
