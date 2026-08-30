package github.jiangbyte.io.workspace.modules.shortcut.service;

import github.jiangbyte.io.workspace.modules.shortcut.result.WorkspaceShortcutResult;

import java.util.List;

/**
 * 工作台个人快捷应用服务。
 *
 * Author: Charlie
 */
public interface WorkspaceShortcutService {

    /** 当前登录账号的快捷应用列表（已过滤失效/无权限菜单）。 */
    List<WorkspaceShortcutResult> listMine();

    /** 整体替换当前账号快捷应用。 */
    List<WorkspaceShortcutResult> replaceMine(List<String> resourceIds);
}
