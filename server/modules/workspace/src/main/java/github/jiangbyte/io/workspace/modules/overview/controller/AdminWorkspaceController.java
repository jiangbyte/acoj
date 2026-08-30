package github.jiangbyte.io.workspace.modules.overview.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import github.jiangbyte.io.common.core.domain.ApiResponse;
import github.jiangbyte.io.workspace.modules.overview.result.WorkspaceOverviewResult;
import github.jiangbyte.io.workspace.modules.overview.service.WorkspaceService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 管理端工作台 API：快捷应用与本人近期活动。
 *
 * Author: Charlie
 */
@Tag(name = "管理端工作台 API")
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class AdminWorkspaceController {

    private final WorkspaceService workspaceService;

    /** 获取工作台总览（登录即可）。 */
    @Operation(summary = "获取工作台总览（登录即可）。")
    @GetMapping("/v1/admin/workspace/overview")
    public ApiResponse<WorkspaceOverviewResult> overview() {
        return ApiResponse.ok(workspaceService.overview());
    }
}
