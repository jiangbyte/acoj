package github.jiangbyte.io.iam.modules.role.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import cn.dev33.satoken.annotation.SaCheckPermission;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import github.jiangbyte.io.common.satoken.StpKit;

import github.jiangbyte.io.common.core.domain.ApiResponse;
import github.jiangbyte.io.common.core.param.IdParam;
import github.jiangbyte.io.common.core.param.IdsParam;
import github.jiangbyte.io.common.log.annotation.OperationAudit;
import github.jiangbyte.io.iam.modules.account.result.SysOwnUserResult;
import github.jiangbyte.io.iam.modules.resource.result.SysResourceOwnResult;
import github.jiangbyte.io.iam.modules.role.entity.SysRole;
import github.jiangbyte.io.iam.modules.role.param.SysRoleAddParam;
import github.jiangbyte.io.iam.modules.role.param.SysRoleEditParam;
import github.jiangbyte.io.iam.modules.role.param.SysRoleGrantResourceParam;
import github.jiangbyte.io.iam.modules.role.param.SysRoleGrantUserParam;
import github.jiangbyte.io.iam.modules.role.param.SysRolePageParam;
import github.jiangbyte.io.iam.modules.role.service.RoleService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;

/**
 * 管理端角色 API：CRUD、资源授权与成员授权。
 *
 * Author: Charlie
 */
@Tag(name = "管理端角色 API")
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class AdminRoleController {

    private final RoleService roleService;

    /** 创建角色。 */
    @Operation(summary = "创建角色。")
    @PostMapping("/v1/admin/sys/roles/create")
    @SaCheckPermission(value = "iam:role:create", type = StpKit.TYPE_ADMIN)
    @OperationAudit(resourceType = "iam_role", action = "create")
    public ApiResponse<Void> create(@Valid @RequestBody SysRoleAddParam param) {
        roleService.create(param);
        return ApiResponse.ok();
    }

    /** 更新角色。 */
    @Operation(summary = "更新角色。")
    @PostMapping("/v1/admin/sys/roles/update")
    @SaCheckPermission(value = "iam:role:update", type = StpKit.TYPE_ADMIN)
    @OperationAudit(resourceType = "iam_role", action = "update")
    public ApiResponse<Void> update(@Valid @RequestBody SysRoleEditParam param) {
        roleService.update(param);
        return ApiResponse.ok();
    }

    /** 批量删除角色。 */
    @Operation(summary = "批量删除角色。")
    @PostMapping("/v1/admin/sys/roles/delete")
    @SaCheckPermission(value = "iam:role:delete", type = StpKit.TYPE_ADMIN)
    @OperationAudit(resourceType = "iam_role", action = "delete")
    public ApiResponse<Void> delete(@Valid @RequestBody IdsParam param) {
        roleService.delete(param);
        return ApiResponse.ok();
    }

    /** 查询角色详情。 */
    @Operation(summary = "查询角色详情。")
    @GetMapping("/v1/admin/sys/roles/detail")
    @SaCheckPermission(value = "iam:role:detail", type = StpKit.TYPE_ADMIN)
    public ApiResponse<SysRole> detail(@Valid @ModelAttribute IdParam param) {
        return ApiResponse.ok(roleService.detail(param.getId()));
    }

    /** 分页查询角色。 */
    @Operation(summary = "分页查询角色。")
    @GetMapping("/v1/admin/sys/roles/page")
    @SaCheckPermission(value = "iam:role:page", type = StpKit.TYPE_ADMIN)
    public ApiResponse<Page<SysRole>> page(@Valid @ModelAttribute SysRolePageParam param) {
        return ApiResponse.ok(roleService.page(param));
    }

    /** 查询角色已拥有管理端资源。 */
    @Operation(summary = "查询角色已拥有管理端资源。")
    @GetMapping("/v1/admin/sys/roles/own-resource")
    @SaCheckPermission(value = "iam:role:ownresource", type = StpKit.TYPE_ADMIN)
    public ApiResponse<SysResourceOwnResult> ownResource(
            @Valid @ModelAttribute IdParam param,
            @RequestParam(value = "account_type", required = false) String accountType) {
        return ApiResponse.ok(roleService.ownResources(param.getId(), accountType));
    }

    /** 全量替换角色管理端资源授权。 */
    @Operation(summary = "全量替换角色管理端资源授权。")
    @PostMapping("/v1/admin/sys/roles/grant-resource")
    @SaCheckPermission(value = "iam:role:grantresource", type = StpKit.TYPE_ADMIN)
    @OperationAudit(resourceType = "iam_role", action = "grant_resource")
    public ApiResponse<Void> grantResource(@Valid @RequestBody SysRoleGrantResourceParam param) {
        roleService.grantResources(param);
        return ApiResponse.ok();
    }

    /** 查询角色已拥有客户端资源。 */
    @Operation(summary = "查询角色已拥有客户端资源。")
    @GetMapping("/v1/admin/sys/roles/own-client-resource")
    @SaCheckPermission(value = "iam:role:ownclientresource", type = StpKit.TYPE_ADMIN)
    public ApiResponse<SysResourceOwnResult> ownClientResource(
            @Valid @ModelAttribute IdParam param,
            @RequestParam(value = "account_type", required = false) String accountType) {
        return ApiResponse.ok(roleService.ownClientResources(param.getId(), accountType));
    }

    /** 全量替换角色客户端资源授权。 */
    @Operation(summary = "全量替换角色客户端资源授权。")
    @PostMapping("/v1/admin/sys/roles/grant-client-resource")
    @SaCheckPermission(value = "iam:role:grantclientresource", type = StpKit.TYPE_ADMIN)
    @OperationAudit(resourceType = "iam_role", action = "grant_client_resource")
    public ApiResponse<Void> grantClientResource(@Valid @RequestBody SysRoleGrantResourceParam param) {
        roleService.grantClientResources(param);
        return ApiResponse.ok();
    }

    /** 查询角色成员。 */
    @Operation(summary = "查询角色成员。")
    @GetMapping("/v1/admin/sys/roles/own-user")
    @SaCheckPermission(value = "iam:role:ownuser", type = StpKit.TYPE_ADMIN)
    public ApiResponse<SysOwnUserResult> ownUser(@Valid @ModelAttribute IdParam param) {
        return ApiResponse.ok(roleService.ownUsers(param.getId()));
    }

    /** 全量替换角色成员。 */
    @Operation(summary = "全量替换角色成员。")
    @PostMapping("/v1/admin/sys/roles/grant-user")
    @SaCheckPermission(value = "iam:role:grantuser", type = StpKit.TYPE_ADMIN)
    @OperationAudit(resourceType = "iam_role", action = "grant_user")
    public ApiResponse<Void> grantUser(@Valid @RequestBody SysRoleGrantUserParam param) {
        roleService.grantUsers(param);
        return ApiResponse.ok();
    }
}
