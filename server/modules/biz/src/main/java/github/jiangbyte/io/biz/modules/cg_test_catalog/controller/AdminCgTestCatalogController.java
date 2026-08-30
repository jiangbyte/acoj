package github.jiangbyte.io.biz.modules.cg_test_catalog.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import cn.dev33.satoken.annotation.SaCheckPermission;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import github.jiangbyte.io.common.core.domain.ApiResponse;
import github.jiangbyte.io.common.core.param.IdParam;
import github.jiangbyte.io.common.core.param.IdsParam;
import github.jiangbyte.io.common.log.annotation.OperationAudit;
import github.jiangbyte.io.common.satoken.StpKit;
import github.jiangbyte.io.biz.modules.cg_test_catalog.entity.CgTestCatalog;
import github.jiangbyte.io.biz.modules.cg_test_catalog.param.CgTestCatalogAddParam;
import github.jiangbyte.io.biz.modules.cg_test_catalog.param.CgTestCatalogEditParam;
import github.jiangbyte.io.biz.modules.cg_test_catalog.param.CgTestCatalogPageParam;
import github.jiangbyte.io.biz.modules.cg_test_catalog.service.CgTestCatalogService;
import cn.hutool.core.lang.tree.Tree;
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
 * 管理端 CgTestCatalog API：CRUD与树查询。
 *
 * Author: Charlie
 */
@Tag(name = "管理端 CgTestCatalog API")
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class AdminCgTestCatalogController {

    private final CgTestCatalogService cgTestCatalogService;

    /** 创建。 */
    @Operation(summary = "创建。")
    @PostMapping("/v1/admin/biz/cg-test-catalog/create")
    @SaCheckPermission(value = "biz:cgtestcatalog:create", type = StpKit.TYPE_ADMIN)
    @OperationAudit(resourceType = "biz_cgtestcatalog", action = "create")
    public ApiResponse<Void> create(@Valid @RequestBody CgTestCatalogAddParam param) {
        cgTestCatalogService.create(param);
        return ApiResponse.ok();
    }

    /** 更新。 */
    @Operation(summary = "更新。")
    @PostMapping("/v1/admin/biz/cg-test-catalog/update")
    @SaCheckPermission(value = "biz:cgtestcatalog:update", type = StpKit.TYPE_ADMIN)
    @OperationAudit(resourceType = "biz_cgtestcatalog", action = "update")
    public ApiResponse<Void> update(@Valid @RequestBody CgTestCatalogEditParam param) {
        cgTestCatalogService.update(param);
        return ApiResponse.ok();
    }

    /** 批量删除。 */
    @Operation(summary = "批量删除。")
    @PostMapping("/v1/admin/biz/cg-test-catalog/delete")
    @SaCheckPermission(value = "biz:cgtestcatalog:delete", type = StpKit.TYPE_ADMIN)
    @OperationAudit(resourceType = "biz_cgtestcatalog", action = "delete")
    public ApiResponse<Void> delete(@Valid @RequestBody IdsParam param) {
        cgTestCatalogService.delete(param);
        return ApiResponse.ok();
    }

    /** 查询详情。 */
    @Operation(summary = "查询详情。")
    @GetMapping("/v1/admin/biz/cg-test-catalog/detail")
    @SaCheckPermission(value = "biz:cgtestcatalog:detail", type = StpKit.TYPE_ADMIN)
    public ApiResponse<CgTestCatalog> detail(@Valid @ModelAttribute IdParam param) {
        return ApiResponse.ok(cgTestCatalogService.detail(param.getId()));
    }

    /** 分页查询。 */
    @Operation(summary = "分页查询。")
    @GetMapping("/v1/admin/biz/cg-test-catalog/page")
    @SaCheckPermission(value = "biz:cgtestcatalog:page", type = StpKit.TYPE_ADMIN)
    public ApiResponse<Page<CgTestCatalog>> page(@Valid @ModelAttribute CgTestCatalogPageParam param) {
        return ApiResponse.ok(cgTestCatalogService.page(param));
    }

    /** 树形查询。 */
    @Operation(summary = "树形查询。")
    @GetMapping("/v1/admin/biz/cg-test-catalog/tree")
    @SaCheckPermission(value = "biz:cgtestcatalog:tree", type = StpKit.TYPE_ADMIN)
    public ApiResponse<List<Tree<String>>> tree(@RequestParam(required = false) String keyword) {
        return ApiResponse.ok(cgTestCatalogService.tree(keyword));
    }
}
