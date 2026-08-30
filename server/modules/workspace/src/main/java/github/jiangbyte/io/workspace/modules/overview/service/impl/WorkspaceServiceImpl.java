package github.jiangbyte.io.workspace.modules.overview.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import github.jiangbyte.io.common.core.exception.BizException;
import github.jiangbyte.io.common.mybatis.datasource.ReadDataSource;
import github.jiangbyte.io.common.satoken.model.LoginUser;
import github.jiangbyte.io.common.satoken.utils.LoginHelper;
import github.jiangbyte.io.workspace.modules.overview.entity.WorkspaceAuditLog;
import github.jiangbyte.io.workspace.modules.overview.mapper.WorkspaceAuditLogMapper;
import github.jiangbyte.io.workspace.modules.overview.result.WorkspaceActivityItemResult;
import github.jiangbyte.io.workspace.modules.overview.result.WorkspaceOverviewResult;
import github.jiangbyte.io.workspace.modules.overview.service.WorkspaceService;
import github.jiangbyte.io.workspace.modules.shortcut.service.WorkspaceShortcutService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * {@link WorkspaceService} 实现：组装快捷应用与本人近期操作/登录日志。
 *
 * Author: Charlie
 */
@Service
@RequiredArgsConstructor
public class WorkspaceServiceImpl implements WorkspaceService {

    private static final int ACTIVITY_LIMIT = 10;

    private final WorkspaceShortcutService shortcutService;
    private final WorkspaceAuditLogMapper auditLogMapper;

    @ReadDataSource
    @Override
    public WorkspaceOverviewResult overview() {
        LoginUser loginUser = LoginHelper.currentUser()
                .orElseThrow(() -> new BizException(401, "未登录"));
        WorkspaceOverviewResult overview = new WorkspaceOverviewResult();
        overview.setShortcuts(shortcutService.listMine());
        overview.setRecentOperations(listRecentOperations(loginUser.getAccountId()));
        overview.setRecentLogins(listRecentLogins(loginUser.getAccountId()));
        return overview;
    }

    private List<WorkspaceActivityItemResult> listRecentOperations(String accountId) {
        Page<WorkspaceAuditLog> page = auditLogMapper.selectPage(
                new Page<>(1, ACTIVITY_LIMIT, false),
                Wrappers.<WorkspaceAuditLog>lambdaQuery()
                        .eq(WorkspaceAuditLog::getAccountId, accountId)
                        .and(w -> w
                                .ne(WorkspaceAuditLog::getAction, "login")
                                .or()
                                .isNull(WorkspaceAuditLog::getAction))
                        .orderByDesc(WorkspaceAuditLog::getCreatedAt));
        return page.getRecords().stream().map(this::toActivityItem).toList();
    }

    private List<WorkspaceActivityItemResult> listRecentLogins(String accountId) {
        Page<WorkspaceAuditLog> page = auditLogMapper.selectPage(
                new Page<>(1, ACTIVITY_LIMIT, false),
                Wrappers.<WorkspaceAuditLog>lambdaQuery()
                        .eq(WorkspaceAuditLog::getAccountId, accountId)
                        .eq(WorkspaceAuditLog::getAction, "login")
                        .orderByDesc(WorkspaceAuditLog::getCreatedAt));
        return page.getRecords().stream().map(this::toActivityItem).toList();
    }

    private WorkspaceActivityItemResult toActivityItem(WorkspaceAuditLog log) {
        WorkspaceActivityItemResult item = new WorkspaceActivityItemResult();
        item.setId(log.getId());
        item.setModule(log.getModule());
        item.setModuleLabel(log.getModuleLabel());
        item.setAction(log.getAction());
        item.setActionName(log.getActionName());
        item.setActionType(log.getActionType());
        item.setSummary(log.getSummary());
        item.setSuccess(log.getSuccess());
        item.setIp(log.getIp());
        item.setUserAgent(log.getUserAgent());
        item.setOperatorName(log.getOperatorName());
        item.setDurationMs(log.getDurationMs());
        item.setResourceId(log.getResourceId());
        item.setCreatedAt(log.getCreatedAt());
        return item;
    }
}
