package github.jiangbyte.io.iam.modules.group.controller;

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
import github.jiangbyte.io.iam.modules.group.entity.SysGroup;
import github.jiangbyte.io.iam.modules.group.param.SysGroupAddParam;
import github.jiangbyte.io.iam.modules.group.param.SysGroupEditParam;
import github.jiangbyte.io.iam.modules.group.param.SysGroupGrantResourceParam;
import github.jiangbyte.io.iam.modules.group.param.SysGroupGrantRoleParam;
import github.jiangbyte.io.iam.modules.group.param.SysGroupGrantUserParam;
import github.jiangbyte.io.iam.modules.group.param.SysGroupPageParam;
import github.jiangbyte.io.iam.modules.group.result.SysGroupOwnRoleResult;
import github.jiangbyte.io.iam.modules.group.service.GroupService;
import github.jiangbyte.io.iam.modules.resource.result.SysResourceOwnResult;
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
 * 管理端用户组 API：CRUD、成员/角色/资源授权与查询。
 *
 * Author: Charlie
 */
@Tag(name = "管理端用户组 API")
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class AdminGroupController {

    private final GroupService groupService;

    /** 创建用户组。 */
    @Operation(summary = "创建用户组。")
    @PostMapping("/v1/admin/sys/groups/create")
    @SaCheckPermission(value = "iam:group:create", type = StpKit.TYPE_ADMIN)
    @OperationAudit(resourceType = "iam_group", action = "create")
    public ApiResponse<Void> create(@Valid @RequestBody SysGroupAddParam param) {
        groupService.create(param);
        return ApiResponse.ok();
    }

    /** 更新用户组。 */
    @Operation(summary = "更新用户组。")
    @PostMapping("/v1/admin/sys/groups/update")
    @SaCheckPermission(value = "iam:group:update", type = StpKit.TYPE_ADMIN)
    @OperationAudit(resourceType = "iam_group", action = "update")
    public ApiResponse<Void> update(@Valid @RequestBody SysGroupEditParam param) {
        groupService.update(param);
        return ApiResponse.ok();
    }

    /** 批量删除用户组。 */
    @Operation(summary = "批量删除用户组。")
    @PostMapping("/v1/admin/sys/groups/delete")
    @SaCheckPermission(value = "iam:group:delete", type = StpKit.TYPE_ADMIN)
    @OperationAudit(resourceType = "iam_group", action = "delete")
    public ApiResponse<Void> delete(@Valid @RequestBody IdsParam param) {
        groupService.delete(param);
        return ApiResponse.ok();
    }

    /** 查询用户组详情。 */
    @Operation(summary = "查询用户组详情。")
    @GetMapping("/v1/admin/sys/groups/detail")
    @SaCheckPermission(value = "iam:group:detail", type = StpKit.TYPE_ADMIN)
    public ApiResponse<SysGroup> detail(@Valid @ModelAttribute IdParam param) {
        return ApiResponse.ok(groupService.detail(param.getId()));
    }

    /** 分页查询用户组。 */
    @Operation(summary = "分页查询用户组。")
    @GetMapping("/v1/admin/sys/groups/page")
    @SaCheckPermission(value = "iam:group:page", type = StpKit.TYPE_ADMIN)
    public ApiResponse<Page<SysGroup>> page(@Valid @ModelAttribute SysGroupPageParam param) {
        return ApiResponse.ok(groupService.page(param));
    }

    /** 查询用户组成员。 */
    @Operation(summary = "查询用户组成员。")
    @GetMapping("/v1/admin/sys/groups/own-user")
    @SaCheckPermission(value = "iam:group:ownuser", type = StpKit.TYPE_ADMIN)
    public ApiResponse<SysOwnUserResult> ownUser(@Valid @ModelAttribute IdParam param) {
        return ApiResponse.ok(groupService.ownUsers(param.getId()));
    }

    /** 全量替换用户组成员。 */
    @Operation(summary = "全量替换用户组成员。")
    @PostMapping("/v1/admin/sys/groups/grant-user")
    @SaCheckPermission(value = "iam:group:grantuser", type = StpKit.TYPE_ADMIN)
    @OperationAudit(resourceType = "iam_group", action = "grant_user")
    public ApiResponse<Void> grantUser(@Valid @RequestBody SysGroupGrantUserParam param) {
        groupService.grantUsers(param);
        return ApiResponse.ok();
    }

    /** 查询用户组已拥有角色。 */
    @Operation(summary = "查询用户组已拥有角色。")
    @GetMapping("/v1/admin/sys/groups/own-role")
    @SaCheckPermission(value = "iam:group:ownrole", type = StpKit.TYPE_ADMIN)
    public ApiResponse<SysGroupOwnRoleResult> ownRole(
            @Valid @ModelAttribute IdParam param,
            @RequestParam(value = "account_type", required = false) String accountType) {
        return ApiResponse.ok(groupService.ownRoles(param.getId(), accountType));
    }

    /** 全量替换用户组角色授权。 */
    @Operation(summary = "全量替换用户组角色授权。")
    @PostMapping("/v1/admin/sys/groups/grant-role")
    @SaCheckPermission(value = "iam:group:grantrole", type = StpKit.TYPE_ADMIN)
    @OperationAudit(resourceType = "iam_group", action = "grant_role")
    public ApiResponse<Void> grantRole(@Valid @RequestBody SysGroupGrantRoleParam param) {
        groupService.grantRoles(param);
        return ApiResponse.ok();
    }

    /** 查询用户组已拥有管理端资源。 */
    @Operation(summary = "查询用户组已拥有管理端资源。")
    @GetMapping("/v1/admin/sys/groups/own-resource")
    @SaCheckPermission(value = "iam:group:ownresource", type = StpKit.TYPE_ADMIN)
    public ApiResponse<SysResourceOwnResult> ownResource(
            @Valid @ModelAttribute IdParam param,
            @RequestParam(value = "account_type", required = false) String accountType) {
        return ApiResponse.ok(groupService.ownResources(param.getId(), accountType));
    }

    /** 全量替换用户组管理端资源授权。 */
    @Operation(summary = "全量替换用户组管理端资源授权。")
    @PostMapping("/v1/admin/sys/groups/grant-resource")
    @SaCheckPermission(value = "iam:group:grantresource", type = StpKit.TYPE_ADMIN)
    @OperationAudit(resourceType = "iam_group", action = "grant_resource")
    public ApiResponse<Void> grantResource(@Valid @RequestBody SysGroupGrantResourceParam param) {
        groupService.grantResources(param);
        return ApiResponse.ok();
    }

    /** 查询用户组已拥有客户端资源。 */
    @Operation(summary = "查询用户组已拥有客户端资源。")
    @GetMapping("/v1/admin/sys/groups/own-client-resource")
    @SaCheckPermission(value = "iam:group:ownclientresource", type = StpKit.TYPE_ADMIN)
    public ApiResponse<SysResourceOwnResult> ownClientResource(
            @Valid @ModelAttribute IdParam param,
            @RequestParam(value = "account_type", required = false) String accountType) {
        return ApiResponse.ok(groupService.ownClientResources(param.getId(), accountType));
    }

    /** 全量替换用户组客户端资源授权。 */
    @Operation(summary = "全量替换用户组客户端资源授权。")
    @PostMapping("/v1/admin/sys/groups/grant-client-resource")
    @SaCheckPermission(value = "iam:group:grantclientresource", type = StpKit.TYPE_ADMIN)
    @OperationAudit(resourceType = "iam_group", action = "grant_client_resource")
    public ApiResponse<Void> grantClientResource(@Valid @RequestBody SysGroupGrantResourceParam param) {
        groupService.grantClientResources(param);
        return ApiResponse.ok();
    }
}
