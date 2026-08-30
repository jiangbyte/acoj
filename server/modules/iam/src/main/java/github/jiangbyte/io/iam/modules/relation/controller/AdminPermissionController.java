package github.jiangbyte.io.iam.modules.relation.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import cn.dev33.satoken.annotation.SaCheckPermission;
import github.jiangbyte.io.common.satoken.StpKit;

import github.jiangbyte.io.common.core.domain.ApiResponse;
import github.jiangbyte.io.iam.modules.relation.result.SysRegisteredPermissionResult;
import github.jiangbyte.io.iam.modules.relation.service.PermissionRegistryService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import lombok.RequiredArgsConstructor;

/**
 * 管理端权限注册表 API：查询已注册权限清单。
 *
 * Author: Charlie
 */
@Tag(name = "管理端权限注册表 API")
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class AdminPermissionController {

    private final PermissionRegistryService permissionRegistryService;

    /** 列出系统已注册权限。 */
    @Operation(summary = "列出系统已注册权限。")
    @GetMapping("/v1/admin/permission-registry")
    @SaCheckPermission(value = "iam:resource:grant", type = StpKit.TYPE_ADMIN)
    public ApiResponse<List<SysRegisteredPermissionResult>> registered() {
        return ApiResponse.ok(permissionRegistryService.listRegisteredPermissions());
    }
}
