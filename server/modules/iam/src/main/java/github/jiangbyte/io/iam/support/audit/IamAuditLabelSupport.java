package github.jiangbyte.io.iam.support.audit;

import github.jiangbyte.io.iam.modules.client.entity.SysClientResource;
import github.jiangbyte.io.iam.modules.client.mapper.SysClientResourceMapper;
import github.jiangbyte.io.iam.modules.dept.entity.SysDept;
import github.jiangbyte.io.iam.modules.dept.mapper.SysDeptMapper;
import github.jiangbyte.io.iam.modules.dept.result.SysDeptGrantResult;
import github.jiangbyte.io.iam.modules.group.entity.SysGroup;
import github.jiangbyte.io.iam.modules.group.mapper.SysGroupMapper;
import github.jiangbyte.io.iam.modules.resource.entity.SysResource;
import github.jiangbyte.io.iam.modules.resource.mapper.SysResourceMapper;
import github.jiangbyte.io.iam.modules.resource.result.SysResourceGrantResult;
import github.jiangbyte.io.iam.modules.role.entity.SysRole;
import github.jiangbyte.io.iam.modules.role.mapper.SysRoleMapper;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * IAM 审计：将 ID / 授权结构解析为可读展示标签。
 *
 * Author: Charlie
 */
public final class IamAuditLabelSupport {

    private IamAuditLabelSupport() {
    }

    public static Map<String, Object> grantResourceField(
            String fieldKey,
            List<SysResourceGrantResult> grants,
            SysResourceMapper resourceMapper) {
        return Map.of(fieldKey, formatResourceGrants(grants, loadResourceNames(grants, resourceMapper, SysResourceGrantResult::getResourceId)));
    }

    public static Map<String, Object> grantClientResourceField(
            String fieldKey,
            List<SysResourceGrantResult> grants,
            SysClientResourceMapper resourceMapper) {
        return Map.of(fieldKey, formatResourceGrants(grants, loadClientResourceNames(grants, resourceMapper)));
    }

    public static Map<String, Object> roleIdsField(Collection<String> roleIds, SysRoleMapper roleMapper) {
        return Map.of("角色", resolveRoleLabels(roleIds, roleMapper));
    }

    public static Map<String, Object> groupIdsField(Collection<String> groupIds, SysGroupMapper groupMapper) {
        return Map.of("用户组", resolveGroupLabels(groupIds, groupMapper));
    }

    public static Map<String, Object> accountIdsField(Collection<String> accountIds, Function<String, String> labelFn) {
        return Map.of("账号", resolveAccountLabels(accountIds, labelFn));
    }

    public static Map<String, Object> deptGrantField(Collection<SysDeptGrantResult> grants, SysDeptMapper deptMapper) {
        return Map.of("部门", formatDeptGrants(grants, deptMapper));
    }

    public static Map<String, Object> permissionBindField(
            String permissionKey,
            String accountType,
            String dataScope) {
        Map<String, Object> fields = new LinkedHashMap<>();
        if (StringUtils.hasText(permissionKey)) {
            fields.put("权限键", permissionKey.trim());
        }
        if (StringUtils.hasText(accountType)) {
            fields.put("账号类型", accountType.trim());
        }
        if (StringUtils.hasText(dataScope)) {
            fields.put("数据范围", dataScope.trim());
        }
        return fields;
    }

    public static List<String> formatResourceGrants(
            List<SysResourceGrantResult> grants,
            Map<String, String> resourceNames) {
        if (grants == null || grants.isEmpty()) {
            return List.of();
        }
        List<String> labels = new ArrayList<>();
        for (SysResourceGrantResult grant : grants) {
            if (grant == null || !StringUtils.hasText(grant.getResourceId())) {
                continue;
            }
            String name = resourceNames.getOrDefault(grant.getResourceId(), grant.getResourceId());
            List<String> keys = grant.getPermissionKeys();
            if (keys == null || keys.isEmpty()) {
                labels.add(name);
            } else {
                labels.add(name + "（" + String.join("，", keys) + "）");
            }
        }
        return labels;
    }

    public static List<String> formatDeptGrants(Collection<SysDeptGrantResult> grants, SysDeptMapper deptMapper) {
        if (grants == null || grants.isEmpty()) {
            return List.of();
        }
        List<String> deptIds = grants.stream()
                .filter(Objects::nonNull)
                .map(SysDeptGrantResult::getDeptId)
                .filter(StringUtils::hasText)
                .distinct()
                .toList();
        Map<String, String> names = deptMapper.selectByIds(deptIds).stream()
                .collect(Collectors.toMap(SysDept::getId, d -> defaultName(d.getName(), d   .getId()), (a, b) -> a, LinkedHashMap::new));
        List<String> labels = new ArrayList<>();
        for (SysDeptGrantResult grant : grants) {
            if (grant == null || !StringUtils.hasText(grant.getDeptId())) {
                continue;
            }
            String name = names.getOrDefault(grant.getDeptId(), grant.getDeptId());
            if (Boolean.TRUE.equals(grant.getIsPrimary())) {
                labels.add(name + "（主部门）");
            } else {
                labels.add(name);
            }
        }
        return labels;
    }

    public static List<String> resolveRoleLabels(Collection<String> roleIds, SysRoleMapper roleMapper) {
        return resolveIdLabels(roleIds, roleMapper.selectByIds(distinctIds(roleIds)), SysRole::getId,
                role -> defaultNameWithCode(role.getName(), role.getCode(), role.getId()));
    }

    public static List<String> resolveGroupLabels(Collection<String> groupIds, SysGroupMapper groupMapper) {
        return resolveIdLabels(groupIds, groupMapper.selectByIds(distinctIds(groupIds)), SysGroup::getId,
                group -> defaultName(group.getName(), group.getId()));
    }

    public static List<String> resolveAccountLabels(Collection<String> accountIds, Function<String, String> labelFn) {
        if (accountIds == null || accountIds.isEmpty()) {
            return List.of();
        }
        List<String> labels = new ArrayList<>();
        for (String accountId : accountIds) {
            if (!StringUtils.hasText(accountId)) {
                continue;
            }
            String label = labelFn.apply(accountId.trim());
            labels.add(StringUtils.hasText(label) ? label : accountId);
        }
        return labels;
    }

    private static Map<String, String> loadResourceNames(
            List<SysResourceGrantResult> grants,
            SysResourceMapper resourceMapper,
            Function<SysResourceGrantResult, String> idGetter) {
        if (grants == null || grants.isEmpty()) {
            return Map.of();
        }
        List<String> ids = grants.stream()
                .filter(Objects::nonNull)
                .map(idGetter)
                .filter(StringUtils::hasText)
                .distinct()
                .toList();
        if (ids.isEmpty()) {
            return Map.of();
        }
        return resourceMapper.selectByIds(ids).stream()
                .collect(Collectors.toMap(SysResource::getId,
                        resource -> defaultNameWithCode(resource.getName(), resource.getCode(), resource.getId()),
                        (a, b) -> a,
                        LinkedHashMap::new));
    }

    private static Map<String, String> loadClientResourceNames(
            List<SysResourceGrantResult> grants,
            SysClientResourceMapper resourceMapper) {
        if (grants == null || grants.isEmpty()) {
            return Map.of();
        }
        List<String> ids = grants.stream()
                .filter(Objects::nonNull)
                .map(SysResourceGrantResult::getResourceId)
                .filter(StringUtils::hasText)
                .distinct()
                .toList();
        if (ids.isEmpty()) {
            return Map.of();
        }
        return resourceMapper.selectByIds(ids).stream()
                .collect(Collectors.toMap(SysClientResource::getId,
                        resource -> defaultNameWithCode(resource.getName(), resource.getCode(), resource.getId()),
                        (a, b) -> a,
                        LinkedHashMap::new));
    }

    private static <T> List<String> resolveIdLabels(
            Collection<String> ids,
            List<T> entities,
            Function<T, String> idGetter,
            Function<T, String> labelGetter) {
        if (ids == null || ids.isEmpty()) {
            return List.of();
        }
        Map<String, String> labelMap = entities.stream()
                .collect(Collectors.toMap(idGetter, labelGetter, (a, b) -> a, LinkedHashMap::new));
        List<String> labels = new ArrayList<>();
        for (String id : ids) {
            if (!StringUtils.hasText(id)) {
                continue;
            }
            labels.add(labelMap.getOrDefault(id, id));
        }
        return labels;
    }

    private static List<String> distinctIds(Collection<String> ids) {
        if (ids == null || ids.isEmpty()) {
            return List.of();
        }
        return ids.stream().filter(StringUtils::hasText).map(String::trim).distinct().toList();
    }

    private static String defaultName(String name, String id) {
        if (StringUtils.hasText(name)) {
            return name.trim();
        }
        return id;
    }

    private static String defaultNameWithCode(String name, String code, String id) {
        if (StringUtils.hasText(name)) {
            return name.trim();
        }
        if (StringUtils.hasText(code)) {
            return code.trim();
        }
        return id;
    }
}
