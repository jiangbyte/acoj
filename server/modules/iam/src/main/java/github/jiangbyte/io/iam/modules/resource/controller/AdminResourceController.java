package github.jiangbyte.io.iam.modules.resource.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import cn.dev33.satoken.annotation.SaCheckPermission;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import github.jiangbyte.io.common.satoken.StpKit;

import github.jiangbyte.io.common.core.domain.ApiResponse;
import github.jiangbyte.io.common.core.param.IdParam;
import github.jiangbyte.io.common.core.param.IdsParam;
import github.jiangbyte.io.common.log.annotation.OperationAudit;
import github.jiangbyte.io.iam.modules.resource.entity.SysResource;
import github.jiangbyte.io.iam.modules.resource.entity.SysResourceModule;
import github.jiangbyte.io.iam.modules.resource.param.SysResourceAddParam;
import github.jiangbyte.io.iam.modules.resource.param.SysResourceButtonAddParam;
import github.jiangbyte.io.iam.modules.resource.param.SysResourceButtonEditParam;
import github.jiangbyte.io.iam.modules.resource.param.SysResourceButtonPageParam;
import github.jiangbyte.io.iam.modules.resource.param.SysResourceEditParam;
import github.jiangbyte.io.iam.modules.resource.param.SysResourceModuleAddParam;
import github.jiangbyte.io.iam.modules.resource.param.SysResourceModuleEditParam;
import github.jiangbyte.io.iam.modules.resource.param.SysResourcePageParam;
import github.jiangbyte.io.iam.modules.resource.param.SysResourcePermissionBindParam;
import github.jiangbyte.io.iam.modules.resource.param.SysResourceTreeParam;
import github.jiangbyte.io.iam.modules.resource.result.SysResourceButtonResult;
import github.jiangbyte.io.iam.modules.resource.service.ResourceService;
import cn.hutool.core.lang.tree.Tree;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import lombok.RequiredArgsConstructor;

/**
 * 管理端资源 API：菜单/按钮/模块 CRUD、树、当前菜单与权限绑定。
 *
 * Author: Charlie
 */
@Tag(name = "管理端资源 API")
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class AdminResourceController {

    private final ResourceService resourceService;

    /** 创建管理端资源。 */
    @Operation(summary = "创建管理端资源。")
    @PostMapping("/v1/admin/sys/resources/create")
    @SaCheckPermission(value = "iam:resource:create", type = StpKit.TYPE_ADMIN)
    @OperationAudit(resourceType = "iam_resource", action = "create")
    public ApiResponse<Void> create(@Valid @RequestBody SysResourceAddParam param) {
        resourceService.create(param);
        return ApiResponse.ok();
    }

    /** 更新管理端资源。 */
    @Operation(summary = "更新管理端资源。")
    @PostMapping("/v1/admin/sys/resources/update")
    @SaCheckPermission(value = "iam:resource:update", type = StpKit.TYPE_ADMIN)
    @OperationAudit(resourceType = "iam_resource", action = "update")
    public ApiResponse<Void> update(@Valid @RequestBody SysResourceEditParam param) {
        resourceService.update(param);
        return ApiResponse.ok();
    }

    /** 批量删除管理端资源。 */
    @Operation(summary = "批量删除管理端资源。")
    @PostMapping("/v1/admin/sys/resources/delete")
    @SaCheckPermission(value = "iam:resource:delete", type = StpKit.TYPE_ADMIN)
    @OperationAudit(resourceType = "iam_resource", action = "delete")
    public ApiResponse<Void> delete(@Valid @RequestBody IdsParam param) {
        resourceService.delete(param);
        return ApiResponse.ok();
    }

    /** 查询管理端资源详情。 */
    @Operation(summary = "查询管理端资源详情。")
    @GetMapping("/v1/admin/sys/resources/detail")
    @SaCheckPermission(value = "iam:resource:detail", type = StpKit.TYPE_ADMIN)
    public ApiResponse<SysResource> detail(@Valid @ModelAttribute IdParam param) {
        return ApiResponse.ok(resourceService.detail(param.getId()));
    }

    /** 分页查询管理端资源。 */
    @Operation(summary = "分页查询管理端资源。")
    @GetMapping("/v1/admin/sys/resources/page")
    @SaCheckPermission(value = "iam:resource:page", type = StpKit.TYPE_ADMIN)
    public ApiResponse<Page<SysResource>> page(@Valid @ModelAttribute SysResourcePageParam param) {
        return ApiResponse.ok(resourceService.page(param));
    }

    /** 管理端资源树。 */
    @Operation(summary = "管理端资源树。")
    @GetMapping("/v1/admin/sys/resources/tree")
    @SaCheckPermission(value = "iam:resource:list", type = StpKit.TYPE_ADMIN)
    public ApiResponse<List<Tree<String>>> tree(@ModelAttribute SysResourceTreeParam param) {
        return ApiResponse.ok(resourceService.tree(param));
    }

    /** 当前账号可见菜单资源。 */
    @Operation(summary = "当前账号可见菜单资源。")
    @GetMapping("/v1/admin/sys/resources/current")
    public ApiResponse<List<SysResource>> current() {
        return ApiResponse.ok(resourceService.currentMenus());
    }

    /** 绑定管理端资源权限。 */
    @Operation(summary = "绑定管理端资源权限。")
    @PostMapping("/v1/admin/resource-permissions")
    @SaCheckPermission(value = "iam:resource:grant", type = StpKit.TYPE_ADMIN)
    @OperationAudit(resourceType = "iam_resource", action = "grant")
    public ApiResponse<Void> bindPermission(@Valid @RequestBody SysResourcePermissionBindParam param) {
        resourceService.bindPermission(param);
        return ApiResponse.ok();
    }

    /** 创建按钮资源。 */
    @Operation(summary = "创建按钮资源。")
    @PostMapping("/v1/admin/sys/resource-buttons/create")
    @SaCheckPermission(value = "iam:resource:create", type = StpKit.TYPE_ADMIN)
    @OperationAudit(resourceType = "iam_resource", action = "create")
    public ApiResponse<Void> createButton(@Valid @RequestBody SysResourceButtonAddParam param) {
        resourceService.createButton(param);
        return ApiResponse.ok();
    }

    /** 更新按钮资源。 */
    @Operation(summary = "更新按钮资源。")
    @PostMapping("/v1/admin/sys/resource-buttons/update")
    @SaCheckPermission(value = "iam:resource:update", type = StpKit.TYPE_ADMIN)
    @OperationAudit(resourceType = "iam_resource", action = "update")
    public ApiResponse<Void> updateButton(@Valid @RequestBody SysResourceButtonEditParam param) {
        resourceService.updateButton(param);
        return ApiResponse.ok();
    }

    /** 批量删除按钮资源。 */
    @Operation(summary = "批量删除按钮资源。")
    @PostMapping("/v1/admin/sys/resource-buttons/delete")
    @SaCheckPermission(value = "iam:resource:delete", type = StpKit.TYPE_ADMIN)
    @OperationAudit(resourceType = "iam_resource", action = "delete")
    public ApiResponse<Void> deleteButtons(@Valid @RequestBody IdsParam param) {
        resourceService.deleteButtons(param);
        return ApiResponse.ok();
    }

    /** 分页查询按钮资源。 */
    @Operation(summary = "分页查询按钮资源。")
    @GetMapping("/v1/admin/sys/resource-buttons/page")
    @SaCheckPermission(value = "iam:resource:list", type = StpKit.TYPE_ADMIN)
    public ApiResponse<Page<SysResourceButtonResult>> pageButtons(
            @Valid @ModelAttribute SysResourceButtonPageParam param) {
        return ApiResponse.ok(resourceService.pageButtons(param));
    }

    /** 创建资源模块。 */
    @Operation(summary = "创建资源模块。")
    @PostMapping("/v1/admin/sys/resource-modules/create")
    @SaCheckPermission(value = "iam:resourcemodule:create", type = StpKit.TYPE_ADMIN)
    @OperationAudit(resourceType = "iam_resourcemodule", action = "create")
    public ApiResponse<Void> createModule(@Valid @RequestBody SysResourceModuleAddParam param) {
        resourceService.createModule(param);
        return ApiResponse.ok();
    }

    /** 更新资源模块。 */
    @Operation(summary = "更新资源模块。")
    @PostMapping("/v1/admin/sys/resource-modules/update")
    @SaCheckPermission(value = "iam:resourcemodule:update", type = StpKit.TYPE_ADMIN)
    @OperationAudit(resourceType = "iam_resourcemodule", action = "update")
    public ApiResponse<Void> updateModule(@Valid @RequestBody SysResourceModuleEditParam param) {
        resourceService.updateModule(param);
        return ApiResponse.ok();
    }

    /** 批量删除资源模块。 */
    @Operation(summary = "批量删除资源模块。")
    @PostMapping("/v1/admin/sys/resource-modules/delete")
    @SaCheckPermission(value = "iam:resourcemodule:delete", type = StpKit.TYPE_ADMIN)
    @OperationAudit(resourceType = "iam_resourcemodule", action = "delete")
    public ApiResponse<Void> deleteModules(@Valid @RequestBody IdsParam param) {
        resourceService.deleteModules(param);
        return ApiResponse.ok();
    }

    /** 资源模块详情。 */
    @Operation(summary = "资源模块详情。")
    @GetMapping("/v1/admin/sys/resource-modules/detail")
    @SaCheckPermission(value = "iam:resourcemodule:detail", type = StpKit.TYPE_ADMIN)
    public ApiResponse<SysResourceModule> moduleDetail(@Valid @ModelAttribute IdParam param) {
        return ApiResponse.ok(resourceService.moduleDetail(param.getId()));
    }

    /** 分页查询资源模块。 */
    @Operation(summary = "分页查询资源模块。")
    @GetMapping("/v1/admin/sys/resource-modules/page")
    @SaCheckPermission(value = "iam:resourcemodule:page", type = StpKit.TYPE_ADMIN)
    public ApiResponse<Page<SysResourceModule>> pageModules(@Valid @ModelAttribute SysResourcePageParam param) {
        return ApiResponse.ok(resourceService.pageModules(param));
    }

    /** 资源模块选择器。 */
    @Operation(summary = "资源模块选择器。")
    @GetMapping("/v1/admin/sys/resource-modules/selector")
    @SaCheckPermission(value = "iam:resourcemodule:page", type = StpKit.TYPE_ADMIN)
    public ApiResponse<List<SysResourceModule>> moduleSelector() {
        return ApiResponse.ok(resourceService.moduleSelector());
    }
}
