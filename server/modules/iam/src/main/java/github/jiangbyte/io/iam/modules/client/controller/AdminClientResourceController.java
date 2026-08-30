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
import github.jiangbyte.io.iam.modules.client.entity.SysClientResource;
import github.jiangbyte.io.iam.modules.client.param.SysClientResourceAddParam;
import github.jiangbyte.io.iam.modules.client.param.SysClientResourceEditParam;
import github.jiangbyte.io.iam.modules.client.param.SysClientResourcePageParam;
import github.jiangbyte.io.iam.modules.client.param.SysClientResourcePermissionBindParam;
import github.jiangbyte.io.iam.modules.client.param.SysClientResourceTreeParam;
import github.jiangbyte.io.iam.modules.client.service.ClientResourceService;
import cn.hutool.core.lang.tree.Tree;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 管理端客户端资源 API：菜单/按钮资源 CRUD、树查询与权限绑定。
 *
 * Author: Charlie
 */
@Tag(name = "管理端客户端资源 API")
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class AdminClientResourceController {

    private final ClientResourceService clientResourceService;

    /** 创建客户端资源。 */
    @Operation(summary = "创建客户端资源。")
    @PostMapping("/v1/admin/sys/client-resources/create")
    @SaCheckPermission(value = "iam:clientresource:create", type = StpKit.TYPE_ADMIN)
    @OperationAudit(resourceType = "iam_clientresource", action = "create")
    public ApiResponse<Void> create(@Valid @RequestBody SysClientResourceAddParam param) {
        clientResourceService.create(param);
        return ApiResponse.ok();
    }

    /** 更新客户端资源。 */
    @Operation(summary = "更新客户端资源。")
    @PostMapping("/v1/admin/sys/client-resources/update")
    @SaCheckPermission(value = "iam:clientresource:update", type = StpKit.TYPE_ADMIN)
    @OperationAudit(resourceType = "iam_clientresource", action = "update")
    public ApiResponse<Void> update(@Valid @RequestBody SysClientResourceEditParam param) {
        clientResourceService.update(param);
        return ApiResponse.ok();
    }

    /** 批量删除客户端资源。 */
    @Operation(summary = "批量删除客户端资源。")
    @PostMapping("/v1/admin/sys/client-resources/delete")
    @SaCheckPermission(value = "iam:clientresource:delete", type = StpKit.TYPE_ADMIN)
    @OperationAudit(resourceType = "iam_clientresource", action = "delete")
    public ApiResponse<Void> delete(@Valid @RequestBody IdsParam param) {
        clientResourceService.delete(param);
        return ApiResponse.ok();
    }

    /** 查询客户端资源详情。 */
    @Operation(summary = "查询客户端资源详情。")
    @GetMapping("/v1/admin/sys/client-resources/detail")
    @SaCheckPermission(value = "iam:clientresource:detail", type = StpKit.TYPE_ADMIN)
    public ApiResponse<SysClientResource> detail(@Valid @ModelAttribute IdParam param) {
        return ApiResponse.ok(clientResourceService.detail(param.getId()));
    }

    /** 分页查询客户端资源。 */
    @Operation(summary = "分页查询客户端资源。")
    @GetMapping("/v1/admin/sys/client-resources/page")
    @SaCheckPermission(value = "iam:clientresource:page", type = StpKit.TYPE_ADMIN)
    public ApiResponse<Page<SysClientResource>> page(@Valid @ModelAttribute SysClientResourcePageParam param) {
        return ApiResponse.ok(clientResourceService.page(param));
    }

    /** 客户端资源树。 */
    @Operation(summary = "客户端资源树。")
    @GetMapping("/v1/admin/sys/client-resources/tree")
    @SaCheckPermission(value = "iam:clientresource:list", type = StpKit.TYPE_ADMIN)
    public ApiResponse<List<Tree<String>>> tree(@ModelAttribute SysClientResourceTreeParam param) {
        return ApiResponse.ok(clientResourceService.tree(param));
    }

    /** 绑定客户端资源权限。 */
    @Operation(summary = "绑定客户端资源权限。")
    @PostMapping("/v1/admin/client-resource-permissions")
    @SaCheckPermission(value = "iam:clientresource:grant", type = StpKit.TYPE_ADMIN)
    @OperationAudit(resourceType = "iam_clientresource", action = "grant")
    public ApiResponse<Void> bindPermission(@Valid @RequestBody SysClientResourcePermissionBindParam param) {
        clientResourceService.bindPermission(param);
        return ApiResponse.ok();
    }
}
