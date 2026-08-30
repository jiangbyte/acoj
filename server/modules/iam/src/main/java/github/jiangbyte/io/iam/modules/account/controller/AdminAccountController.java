package github.jiangbyte.io.iam.modules.account.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import cn.dev33.satoken.annotation.SaCheckPermission;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import github.jiangbyte.io.common.satoken.StpKit;

import github.jiangbyte.io.common.core.domain.ApiResponse;
import github.jiangbyte.io.common.core.param.IdParam;
import github.jiangbyte.io.common.core.param.IdsParam;
import github.jiangbyte.io.common.log.annotation.OperationAudit;
import github.jiangbyte.io.iam.modules.account.param.SysAccountAddParam;
import github.jiangbyte.io.iam.modules.account.param.SysAccountEditParam;
import github.jiangbyte.io.iam.modules.account.param.SysAccountGrantDeptParam;
import github.jiangbyte.io.iam.modules.account.param.SysAccountGrantGroupParam;
import github.jiangbyte.io.iam.modules.account.param.SysAccountGrantResourceParam;
import github.jiangbyte.io.iam.modules.account.param.SysAccountGrantRoleParam;
import github.jiangbyte.io.iam.modules.account.param.SysAccountPageParam;
import github.jiangbyte.io.iam.modules.account.param.SysAccountUpdateLoginIdentityParam;
import github.jiangbyte.io.iam.modules.account.result.SysAccountListResult;
import github.jiangbyte.io.iam.modules.account.result.SysAccountOwnDeptResult;
import github.jiangbyte.io.iam.modules.account.result.SysAccountOwnGroupResult;
import github.jiangbyte.io.iam.modules.account.result.SysAccountOwnRoleResult;
import github.jiangbyte.io.iam.modules.account.result.SysAccountResult;
import github.jiangbyte.io.iam.modules.account.service.AccountService;
import github.jiangbyte.io.iam.modules.resource.result.SysResourceOwnResult;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;

/**
 * 管理端账号 API。HTTP 前缀 /v1/admin/sys/... 与共享前端约定一致
 * （代码包在 module/iam，不在 module/sys）。
 * 提供账号 CRUD、分页及角色/用户组/部门/资源授权接口。
 *
 * Author: Charlie
 */
@Tag(name = "管理端账号 API")
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class AdminAccountController {

    private final AccountService accountService;

    /** 创建账号。 */
    @Operation(summary = "创建账号。")
    @PostMapping("/v1/admin/sys/accounts/create")
    @SaCheckPermission(value = "iam:account:create", type = StpKit.TYPE_ADMIN)
    @OperationAudit(resourceType = "iam_account", action = "create")
    public ApiResponse<Void> create(@Valid @RequestBody SysAccountAddParam param) {
        accountService.create(param);
        return ApiResponse.ok();
    }

    /** 更新账号。 */
    @Operation(summary = "更新账号。")
    @PostMapping("/v1/admin/sys/accounts/update")
    @SaCheckPermission(value = "iam:account:update", type = StpKit.TYPE_ADMIN)
    @OperationAudit(resourceType = "iam_account", action = "update")
    public ApiResponse<Void> update(@Valid @RequestBody SysAccountEditParam param) {
        accountService.update(param);
        return ApiResponse.ok();
    }

    /** 更新账号邮箱/手机号登录身份。 */
    @Operation(summary = "更新账号邮箱/手机号登录身份。")
    @PostMapping("/v1/admin/sys/accounts/update-login-identity")
    @SaCheckPermission(value = "iam:account:update", type = StpKit.TYPE_ADMIN)
    @OperationAudit(resourceType = "iam_account", action = "update_login_identity")
    public ApiResponse<Void> updateLoginIdentity(@Valid @RequestBody SysAccountUpdateLoginIdentityParam param) {
        accountService.updateLoginIdentity(param);
        return ApiResponse.ok();
    }

    /** 批量删除账号。 */
    @Operation(summary = "批量删除账号。")
    @PostMapping("/v1/admin/sys/accounts/delete")
    @SaCheckPermission(value = "iam:account:delete", type = StpKit.TYPE_ADMIN)
    @OperationAudit(resourceType = "iam_account", action = "delete")
    public ApiResponse<Void> delete(@Valid @RequestBody IdsParam param) {
        accountService.delete(param);
        return ApiResponse.ok();
    }

    /** 查询账号详情。 */
    @Operation(summary = "查询账号详情。")
    @GetMapping("/v1/admin/sys/accounts/detail")
    @SaCheckPermission(value = "iam:account:detail", type = StpKit.TYPE_ADMIN)
    public ApiResponse<SysAccountResult> detail(@Valid @ModelAttribute IdParam param) {
        return ApiResponse.ok(accountService.detail(param.getId()));
    }

    /** 分页查询账号。 */
    @Operation(summary = "分页查询账号。")
    @GetMapping("/v1/admin/sys/accounts/page")
    @SaCheckPermission(value = "iam:account:page", type = StpKit.TYPE_ADMIN)
    public ApiResponse<Page<SysAccountListResult>> page(@Valid @ModelAttribute SysAccountPageParam param) {
        return ApiResponse.ok(accountService.page(param));
    }

    /** 查询账号已拥有角色。 */
    @Operation(summary = "查询账号已拥有角色。")
    @GetMapping("/v1/admin/sys/accounts/own-role")
    @SaCheckPermission(value = "iam:account:ownrole", type = StpKit.TYPE_ADMIN)
    public ApiResponse<SysAccountOwnRoleResult> ownRole(@Valid @ModelAttribute IdParam param) {
        return ApiResponse.ok(accountService.ownRoles(param.getId()));
    }

    /** 全量替换账号角色授权。 */
    @Operation(summary = "全量替换账号角色授权。")
    @PostMapping("/v1/admin/sys/accounts/grant-role")
    @SaCheckPermission(value = "iam:account:grantrole", type = StpKit.TYPE_ADMIN)
    @OperationAudit(resourceType = "iam_account", action = "grant_role")
    public ApiResponse<Void> grantRole(@Valid @RequestBody SysAccountGrantRoleParam param) {
        accountService.grantRoles(param);
        return ApiResponse.ok();
    }

    /** 查询账号已拥有用户组。 */
    @Operation(summary = "查询账号已拥有用户组。")
    @GetMapping("/v1/admin/sys/accounts/own-group")
    @SaCheckPermission(value = "iam:account:owngroup", type = StpKit.TYPE_ADMIN)
    public ApiResponse<SysAccountOwnGroupResult> ownGroup(@Valid @ModelAttribute IdParam param) {
        return ApiResponse.ok(accountService.ownGroups(param.getId()));
    }

    /** 全量替换账号用户组授权。 */
    @Operation(summary = "全量替换账号用户组授权。")
    @PostMapping("/v1/admin/sys/accounts/grant-group")
    @SaCheckPermission(value = "iam:account:grantgroup", type = StpKit.TYPE_ADMIN)
    @OperationAudit(resourceType = "iam_account", action = "grant_group")
    public ApiResponse<Void> grantGroup(@Valid @RequestBody SysAccountGrantGroupParam param) {
        accountService.grantGroups(param);
        return ApiResponse.ok();
    }

    /** 查询账号已拥有部门。 */
    @Operation(summary = "查询账号已拥有部门。")
    @GetMapping("/v1/admin/sys/accounts/own-dept")
    @SaCheckPermission(value = "iam:account:owndept", type = StpKit.TYPE_ADMIN)
    public ApiResponse<SysAccountOwnDeptResult> ownDept(@Valid @ModelAttribute IdParam param) {
        return ApiResponse.ok(accountService.ownDepts(param.getId()));
    }

    /** 全量替换账号部门授权。 */
    @Operation(summary = "全量替换账号部门授权。")
    @PostMapping("/v1/admin/sys/accounts/grant-dept")
    @SaCheckPermission(value = "iam:account:grantdept", type = StpKit.TYPE_ADMIN)
    @OperationAudit(resourceType = "iam_account", action = "grant_dept")
    public ApiResponse<Void> grantDept(@Valid @RequestBody SysAccountGrantDeptParam param) {
        accountService.grantDepts(param);
        return ApiResponse.ok();
    }

    /** 查询账号已拥有管理端资源。 */
    @Operation(summary = "查询账号已拥有管理端资源。")
    @GetMapping("/v1/admin/sys/accounts/own-resource")
    @SaCheckPermission(value = "iam:account:ownresource", type = StpKit.TYPE_ADMIN)
    public ApiResponse<SysResourceOwnResult> ownResource(@Valid @ModelAttribute IdParam param) {
        return ApiResponse.ok(accountService.ownResources(param.getId()));
    }

    /** 全量替换账号管理端资源授权。 */
    @Operation(summary = "全量替换账号管理端资源授权。")
    @PostMapping("/v1/admin/sys/accounts/grant-resource")
    @SaCheckPermission(value = "iam:account:grantresource", type = StpKit.TYPE_ADMIN)
    @OperationAudit(resourceType = "iam_account", action = "grant_resource")
    public ApiResponse<Void> grantResource(@Valid @RequestBody SysAccountGrantResourceParam param) {
        accountService.grantResources(param);
        return ApiResponse.ok();
    }

    /** 查询账号已拥有客户端资源。 */
    @Operation(summary = "查询账号已拥有客户端资源。")
    @GetMapping("/v1/admin/sys/accounts/own-client-resource")
    @SaCheckPermission(value = "iam:account:ownclientresource", type = StpKit.TYPE_ADMIN)
    public ApiResponse<SysResourceOwnResult> ownClientResource(@Valid @ModelAttribute IdParam param) {
        return ApiResponse.ok(accountService.ownClientResources(param.getId()));
    }

    /** 全量替换账号客户端资源授权。 */
    @Operation(summary = "全量替换账号客户端资源授权。")
    @PostMapping("/v1/admin/sys/accounts/grant-client-resource")
    @SaCheckPermission(value = "iam:account:grantclientresource", type = StpKit.TYPE_ADMIN)
    @OperationAudit(resourceType = "iam_account", action = "grant_client_resource")
    public ApiResponse<Void> grantClientResource(@Valid @RequestBody SysAccountGrantResourceParam param) {
        accountService.grantClientResources(param);
        return ApiResponse.ok();
    }
}
