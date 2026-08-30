package github.jiangbyte.io.workspace.modules.overview.result;

import io.swagger.v3.oas.annotations.media.Schema;
import github.jiangbyte.io.workspace.modules.shortcut.result.WorkspaceShortcutResult;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * 工作台总览：快捷应用 + 本人近期操作/登录日志。
 *
 * Author: Charlie
 */
@Schema(description = "工作台总览：快捷应用 + 本人近期操作/登录日志。")
@Data
public class WorkspaceOverviewResult {
    @Schema(description = "shortcuts")
    private List<WorkspaceShortcutResult> shortcuts = new ArrayList<>();
    @Schema(description = "recentOperations")
    private List<WorkspaceActivityItemResult> recentOperations = new ArrayList<>();
    @Schema(description = "recentLogins")
    private List<WorkspaceActivityItemResult> recentLogins = new ArrayList<>();
}
