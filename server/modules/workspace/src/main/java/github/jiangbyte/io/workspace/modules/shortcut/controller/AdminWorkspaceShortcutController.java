package github.jiangbyte.io.workspace.modules.shortcut.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import github.jiangbyte.io.common.core.domain.ApiResponse;
import github.jiangbyte.io.common.log.annotation.OperationAudit;
import github.jiangbyte.io.workspace.modules.shortcut.param.WorkspaceShortcutSaveParam;
import github.jiangbyte.io.workspace.modules.shortcut.result.WorkspaceShortcutResult;
import github.jiangbyte.io.workspace.modules.shortcut.service.WorkspaceShortcutService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 管理端工作台个人快捷应用 API。
 *
 * Author: Charlie
 */
@Tag(name = "管理端工作台个人快捷应用 API")
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class AdminWorkspaceShortcutController {

    private final WorkspaceShortcutService shortcutService;

    @Operation(summary = "shortcuts。")
    @GetMapping("/v1/admin/workspace/shortcuts")
    public ApiResponse<List<WorkspaceShortcutResult>> list() {
        return ApiResponse.ok(shortcutService.listMine());
    }

    @Operation(summary = "shortcuts。")
    @PostMapping("/v1/admin/workspace/shortcuts")
    @OperationAudit(resourceType = "workspace_shortcut", action = "update", name = "更新快捷应用")
    public ApiResponse<List<WorkspaceShortcutResult>> replace(@Valid @RequestBody WorkspaceShortcutSaveParam request) {
        return ApiResponse.ok(shortcutService.replaceMine(request.getResourceIds()));
    }
}
