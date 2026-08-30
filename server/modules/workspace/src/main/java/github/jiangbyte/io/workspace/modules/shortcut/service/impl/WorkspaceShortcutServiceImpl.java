package github.jiangbyte.io.workspace.modules.shortcut.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import github.jiangbyte.io.common.core.exception.BizException;
import github.jiangbyte.io.common.log.audit.AuditSnapshots;
import github.jiangbyte.io.common.satoken.model.LoginUser;
import github.jiangbyte.io.common.satoken.utils.LoginHelper;
import github.jiangbyte.io.iam.account.AccountApi;
import github.jiangbyte.io.iam.account.AccountAuthorizationInfo;
import github.jiangbyte.io.workspace.modules.shortcut.entity.SysWorkspaceShortcut;
import github.jiangbyte.io.workspace.modules.shortcut.entity.WorkspaceMenuResource;
import github.jiangbyte.io.workspace.modules.shortcut.mapper.SysWorkspaceShortcutMapper;
import github.jiangbyte.io.workspace.modules.shortcut.mapper.WorkspaceMenuResourceMapper;
import github.jiangbyte.io.workspace.modules.shortcut.result.WorkspaceShortcutResult;
import github.jiangbyte.io.workspace.modules.shortcut.service.WorkspaceShortcutService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * {@link WorkspaceShortcutService} 实现：按账号维护个人快捷菜单，保存时校验可见 MENU。
 *
 * Author: Charlie
 */
@Service
@RequiredArgsConstructor
public class WorkspaceShortcutServiceImpl
        extends ServiceImpl<SysWorkspaceShortcutMapper, SysWorkspaceShortcut>
        implements WorkspaceShortcutService {

    private static final int MAX_SHORTCUTS = 16;
    private static final String HOME_CODE = "workspace";

    private final AccountApi accountApi;
    private final WorkspaceMenuResourceMapper menuResourceMapper;

    @Override
    public List<WorkspaceShortcutResult> listMine() {
        LoginUser user = LoginHelper.currentUser()
                .orElseThrow(() -> new BizException(401, "未登录"));
        List<SysWorkspaceShortcut> shortcuts = list(Wrappers.<SysWorkspaceShortcut>lambdaQuery()
                .eq(SysWorkspaceShortcut::getAccountId, user.getAccountId())
                .orderByAsc(SysWorkspaceShortcut::getSort)
                .orderByAsc(SysWorkspaceShortcut::getId));
        if (shortcuts.isEmpty()) {
            return List.of();
        }

        List<String> resourceIds = shortcuts.stream()
                .map(SysWorkspaceShortcut::getResourceId)
                .filter(StringUtils::hasText)
                .distinct()
                .toList();
        Map<String, WorkspaceMenuResource> menuMap = resourceIds.isEmpty()
                ? Map.of()
                : menuResourceMapper.selectList(Wrappers.<WorkspaceMenuResource>lambdaQuery()
                                .in(WorkspaceMenuResource::getId, resourceIds)
                                .eq(WorkspaceMenuResource::getStatus, "ENABLED")
                                .eq(WorkspaceMenuResource::getResourceType, "MENU"))
                        .stream()
                        .collect(Collectors.toMap(WorkspaceMenuResource::getId, Function.identity(), (a, b) -> a, LinkedHashMap::new));

        Set<String> granted = resolveGrantedResourceIds(user);
        List<WorkspaceShortcutResult> rows = new ArrayList<>();
        for (SysWorkspaceShortcut shortcut : shortcuts) {
            WorkspaceMenuResource menu = menuMap.get(shortcut.getResourceId());
            if (menu == null || !StringUtils.hasText(menu.getPath())) {
                continue;
            }
            if (granted != null && !granted.contains(shortcut.getResourceId())) {
                continue;
            }
            rows.add(toShortcutResult(shortcut, menu));
        }
        return rows;
    }

    @Override
    @Transactional
    public List<WorkspaceShortcutResult> replaceMine(List<String> resourceIds) {
        LoginUser user = LoginHelper.currentUser()
                .orElseThrow(() -> new BizException(401, "未登录"));
        AuditSnapshots.subject(user.getAccountId());
        List<String> normalized = normalizeResourceIds(resourceIds);
        if (normalized.size() > MAX_SHORTCUTS) {
            throw new BizException("快捷应用最多 " + MAX_SHORTCUTS + " 个");
        }

        Set<String> granted = resolveGrantedResourceIds(user);
        OffsetDateTime now = OffsetDateTime.now();
        List<SysWorkspaceShortcut> entities = new ArrayList<>(normalized.size());
        int sort = 1;
        for (String resourceId : normalized) {
            WorkspaceMenuResource menu = menuResourceMapper.selectById(resourceId);
            if (menu == null
                    || !"MENU".equals(menu.getResourceType())
                    || !"ENABLED".equals(menu.getStatus())
                    || !StringUtils.hasText(menu.getPath())
                    || HOME_CODE.equals(menu.getCode())) {
                throw new BizException("存在不可用的菜单资源");
            }
            if (granted != null && !granted.contains(resourceId)) {
                throw new BizException("存在未授权的菜单：" + menu.getName());
            }
            SysWorkspaceShortcut entity = new SysWorkspaceShortcut();
            entity.setAccountId(user.getAccountId());
            entity.setResourceId(resourceId);
            entity.setSort(sort++);
            entity.setCreatedAt(now);
            entity.setCreatedBy(user.getAccountId());
            entity.setUpdatedAt(now);
            entity.setUpdatedBy(user.getAccountId());
            entities.add(entity);
        }

        remove(Wrappers.<SysWorkspaceShortcut>lambdaQuery()
                .eq(SysWorkspaceShortcut::getAccountId, user.getAccountId()));
        if (!entities.isEmpty()) {
            saveBatch(entities);
        }
        AuditSnapshots.after(Map.of("resourceIds", normalized));
        return listMine();
    }

    private WorkspaceShortcutResult toShortcutResult(SysWorkspaceShortcut shortcut, WorkspaceMenuResource menu) {
        WorkspaceShortcutResult row = new WorkspaceShortcutResult();
        row.setId(shortcut.getId());
        row.setResourceId(shortcut.getResourceId());
        row.setSort(shortcut.getSort());
        row.setName(menu.getName());
        row.setPath(menu.getPath());
        row.setIcon(menu.getIcon());
        row.setCode(menu.getCode());
        return row;
    }

    private List<String> normalizeResourceIds(List<String> resourceIds) {
        LinkedHashSet<String> unique = new LinkedHashSet<>();
        if (resourceIds != null) {
            for (String id : resourceIds) {
                if (StringUtils.hasText(id)) {
                    unique.add(id.trim());
                }
            }
        }
        return new ArrayList<>(unique);
    }

    /**
     * @return null 表示全权限；否则为已授权资源 id 集合
     */
    private Set<String> resolveGrantedResourceIds(LoginUser user) {
        if (isFullAccess(user)) {
            return null;
        }
        AccountAuthorizationInfo authorization = accountApi.getAuthorization(user.getAccountId());
        if (authorization == null || authorization.getResourceIds() == null) {
            return Set.of();
        }
        return new HashSet<>(authorization.getResourceIds());
    }

    private static boolean isFullAccess(LoginUser user) {
        Set<String> permissions = user.getPermissions();
        Set<String> roles = user.getRoles();
        return (permissions != null && permissions.contains("*:*:*"))
                || (roles != null && roles.contains("SUPER_ADMIN"));
    }
}
