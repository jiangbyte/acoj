package github.jiangbyte.io.iam.modules.client.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import cn.dev33.satoken.annotation.SaCheckPermission;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import github.jiangbyte.io.common.core.domain.ApiResponse;
import github.jiangbyte.io.common.core.param.IdParam;
import github.jiangbyte.io.common.core.param.IdsParam;
import github.jiangbyte.io.common.log.annotation.OperationAudit;
import github.jiangbyte.io.common.satoken.StpKit;
import github.jiangbyte.io.iam.modules.client.entity.SysClientModule;
import github.jiangbyte.io.iam.modules.client.param.SysClientModuleAddParam;
import github.jiangbyte.io.iam.modules.client.param.SysClientModuleEditParam;
import github.jiangbyte.io.iam.modules.client.param.SysClientModulePageParam;
import github.jiangbyte.io.iam.modules.client.service.ClientModuleService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 管理端客户端模块 API：客户端资源模块的 CRUD、分页与选择器。
 *
 * Author: Charlie
 */
@Tag(name = "管理端客户端模块 API")
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class AdminClientModuleController {

    private final ClientModuleService clientModuleService;

    /** 创建客户端模块。 */
    @Operation(summary = "创建客户端模块。")
    @PostMapping("/v1/admin/sys/client-modules/create")
    @SaCheckPermission(value = "iam:clientmodule:create", type = StpKit.TYPE_ADMIN)
    @OperationAudit(resourceType = "iam_clientmodule", action = "create")
    public ApiResponse<Void> create(@Valid @RequestBody SysClientModuleAddParam param) {
        clientModuleService.create(param);
        return ApiResponse.ok();
    }

    /** 更新客户端模块。 */
    @Operation(summary = "更新客户端模块。")
    @PostMapping("/v1/admin/sys/client-modules/update")
    @SaCheckPermission(value = "iam:clientmodule:update", type = StpKit.TYPE_ADMIN)
    @OperationAudit(resourceType = "iam_clientmodule", action = "update")
    public ApiResponse<Void> update(@Valid @RequestBody SysClientModuleEditParam param) {
        clientModuleService.update(param);
        return ApiResponse.ok();
    }

    /** 批量删除客户端模块。 */
    @Operation(summary = "批量删除客户端模块。")
    @PostMapping("/v1/admin/sys/client-modules/delete")
    @SaCheckPermission(value = "iam:clientmodule:delete", type = StpKit.TYPE_ADMIN)
    @OperationAudit(resourceType = "iam_clientmodule", action = "delete")
    public ApiResponse<Void> delete(@Valid @RequestBody IdsParam param) {
        clientModuleService.delete(param);
        return ApiResponse.ok();
    }

    /** 查询客户端模块详情。 */
    @Operation(summary = "查询客户端模块详情。")
    @GetMapping("/v1/admin/sys/client-modules/detail")
    @SaCheckPermission(value = "iam:clientmodule:detail", type = StpKit.TYPE_ADMIN)
    public ApiResponse<SysClientModule> detail(@Valid @ModelAttribute IdParam param) {
        return ApiResponse.ok(clientModuleService.detail(param.getId()));
    }

    /** 分页查询客户端模块。 */
    @Operation(summary = "分页查询客户端模块。")
    @GetMapping("/v1/admin/sys/client-modules/page")
    @SaCheckPermission(value = "iam:clientmodule:page", type = StpKit.TYPE_ADMIN)
    public ApiResponse<Page<SysClientModule>> page(@Valid @ModelAttribute SysClientModulePageParam param) {
        return ApiResponse.ok(clientModuleService.page(param));
    }

    /** 模块选择器列表。 */
    @Operation(summary = "模块选择器列表。")
    @GetMapping("/v1/admin/sys/client-modules/selector")
    @SaCheckPermission(value = "iam:clientmodule:page", type = StpKit.TYPE_ADMIN)
    public ApiResponse<List<SysClientModule>> selector(@RequestParam(required = false) String accountType) {
        return ApiResponse.ok(clientModuleService.selector(accountType));
    }
}
