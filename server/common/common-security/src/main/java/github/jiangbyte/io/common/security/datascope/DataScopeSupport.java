package github.jiangbyte.io.common.security.datascope;

import github.jiangbyte.io.common.core.enums.DataScopeType;
import github.jiangbyte.io.common.core.exception.BizException;
import github.jiangbyte.io.common.satoken.model.LoginUser;
import github.jiangbyte.io.common.satoken.utils.LoginHelper;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 数据权限支持：根据登录用户范围解析约束并应用到查询条件。
 *
 * Author: Charlie
 */
public final class DataScopeSupport {

    private DataScopeSupport() {
    }

    public static Optional<LoginUser.PermissionGrant> findGrant(LoginUser user, String permissionKey) {
        if (user == null || user.getPermissionGrants() == null || !StringUtils.hasText(permissionKey)) {
            return Optional.empty();
        }
        LoginUser.PermissionGrant matched = null;
        for (LoginUser.PermissionGrant grant : user.getPermissionGrants()) {
            if (permissionKey.equals(grant.getPermissionKey())) {
                matched = grant;
            }
        }
        return Optional.ofNullable(matched);
    }

    public static boolean hasUnrestricted(LoginUser user, String permissionKey) {
        if (user == null) {
            return false;
        }
        if (user.getPermissions() != null && user.getPermissions().contains("*:*:*")) {
            return true;
        }
        return findGrant(user, permissionKey)
                .map(grant -> DataScopeType.ALL.name().equalsIgnoreCase(grant.getDataScope()))
                .orElse(false);
    }

    /**
     * @param expandDeptAndChildren 将根部门 id 展开为含子孙；用于 DEPT_AND_CHILD
     */
    public static DataScopeConstraint resolve(
            LoginUser user,
            String permissionKey,
            Function<Collection<String>, List<String>> expandDeptAndChildren) {
        if (user == null) {
            return new DataScopeConstraint.Deny();
        }
        if (hasUnrestricted(user, permissionKey)) {
            return new DataScopeConstraint.All();
        }
        LoginUser.PermissionGrant grant = findGrant(user, permissionKey).orElse(null);
        DataScopeType scope = parseScope(grant == null ? null : grant.getDataScope());
        return switch (scope) {
            case ALL -> new DataScopeConstraint.All();
            case SELF -> {
                if (!StringUtils.hasText(user.getAccountId())) {
                    yield new DataScopeConstraint.Deny();
                }
                yield new DataScopeConstraint.Self(user.getAccountId());
            }
            case DEPT -> new DataScopeConstraint.Depts(uniqueIds(user.getDeptIds()));
            case DEPT_AND_CHILD -> {
                List<String> roots = uniqueIds(user.getDeptIds());
                List<String> expanded = expandDeptAndChildren == null
                        ? roots
                        : uniqueIds(expandDeptAndChildren.apply(roots));
                yield new DataScopeConstraint.Depts(expanded);
            }
            case CUSTOM -> {
                List<String> custom = grant == null ? List.of() : uniqueIds(grant.getCustomScopeDeptIds());
                yield new DataScopeConstraint.Depts(custom);
            }
        };
    }

    public static DataScopeConstraint resolveCurrent(
            String permissionKey,
            Function<Collection<String>, List<String>> expandDeptAndChildren) {
        return LoginHelper.currentUser()
                .map(user -> resolve(user, permissionKey, expandDeptAndChildren))
                .orElseGet(DataScopeConstraint.Deny::new);
    }

    /** 账号主体：ALL 放行；SELF 比账号 id；部门范围需调用方结合关系判定（此处无 dept 上下文则拒绝）。 */
    public static boolean allowsAccount(DataScopeConstraint constraint, String accountId) {
        if (constraint == null) {
            return false;
        }
        return switch (constraint) {
            case DataScopeConstraint.All ignored -> true;
            case DataScopeConstraint.Self self ->
                    StringUtils.hasText(accountId) && accountId.equals(self.accountId());
            case DataScopeConstraint.Depts ignored -> false;
            case DataScopeConstraint.Deny ignored -> false;
        };
    }

    /** 负责人/部门主体：ALL 放行；SELF 比负责人；DEPTS 要求 ownerDeptId 非空且落在范围内。 */
    public static boolean allowsOwnerOrDept(
            DataScopeConstraint constraint, String ownerAccountId, String ownerDeptId) {
        if (constraint == null) {
            return false;
        }
        return switch (constraint) {
            case DataScopeConstraint.All ignored -> true;
            case DataScopeConstraint.Self self ->
                    StringUtils.hasText(ownerAccountId) && ownerAccountId.equals(self.accountId());
            case DataScopeConstraint.Depts depts ->
                    StringUtils.hasText(ownerDeptId)
                            && depts.deptIds() != null
                            && depts.deptIds().contains(ownerDeptId);
            case DataScopeConstraint.Deny ignored -> false;
        };
    }

    public static void assertAccountAccessible(DataScopeConstraint constraint, String accountId) {
        if (!allowsAccount(constraint, accountId)) {
            throw new BizException(403, "无权访问该数据");
        }
    }

    public static void assertOwnerOrDeptAccessible(
            DataScopeConstraint constraint, String ownerAccountId, String ownerDeptId) {
        if (!allowsOwnerOrDept(constraint, ownerAccountId, ownerDeptId)) {
            throw new BizException(403, "无权访问该数据");
        }
    }

    private static DataScopeType parseScope(String raw) {
        if (!StringUtils.hasText(raw)) {
            return DataScopeType.SELF;
        }
        try {
            return DataScopeType.valueOf(raw.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException ignored) {
            return DataScopeType.SELF;
        }
    }

    private static List<String> uniqueIds(Collection<String> values) {
        if (values == null || values.isEmpty()) {
            return List.of();
        }
        return values.stream()
                .filter(StringUtils::hasText)
                .map(String::trim)
                .distinct()
                .sorted()
                .collect(Collectors.toCollection(ArrayList::new));
    }
}
