package github.jiangbyte.io.workspace.modules.overview.service;

import github.jiangbyte.io.workspace.modules.overview.result.WorkspaceOverviewResult;

/**
 * 工作台领域服务：组装快捷应用与本人近期活动日志。
 *
 * Author: Charlie
 */
public interface WorkspaceService {

    /** 组装工作台首页数据。 */
    WorkspaceOverviewResult overview();
}
