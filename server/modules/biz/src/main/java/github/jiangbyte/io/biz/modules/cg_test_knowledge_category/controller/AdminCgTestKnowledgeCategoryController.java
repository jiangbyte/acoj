package github.jiangbyte.io.biz.modules.cg_test_knowledge_category.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import cn.dev33.satoken.annotation.SaCheckPermission;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import github.jiangbyte.io.common.core.domain.ApiResponse;
import github.jiangbyte.io.common.core.param.IdParam;
import github.jiangbyte.io.common.core.param.IdsParam;
import github.jiangbyte.io.common.log.annotation.OperationAudit;
import github.jiangbyte.io.common.satoken.StpKit;
import github.jiangbyte.io.biz.modules.cg_test_knowledge_category.entity.CgTestKnowledgeCategory;
import github.jiangbyte.io.biz.modules.cg_test_knowledge_category.param.CgTestKnowledgeCategoryAddParam;
import github.jiangbyte.io.biz.modules.cg_test_knowledge_category.param.CgTestKnowledgeCategoryEditParam;
import github.jiangbyte.io.biz.modules.cg_test_knowledge_category.param.CgTestKnowledgeCategoryPageParam;
import github.jiangbyte.io.biz.modules.cg_test_knowledge_category.service.CgTestKnowledgeCategoryService;
import github.jiangbyte.io.biz.modules.cg_test_knowledge_category.entity.CgTestKnowledgeDoc;
import github.jiangbyte.io.biz.modules.cg_test_knowledge_category.param.CgTestKnowledgeDocAddParam;
import github.jiangbyte.io.biz.modules.cg_test_knowledge_category.param.CgTestKnowledgeDocEditParam;
import github.jiangbyte.io.biz.modules.cg_test_knowledge_category.param.CgTestKnowledgeDocPageParam;
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
 * 管理端 CgTestKnowledgeCategory API：CRUD与树查询与子实体维护。
 *
 * Author: Charlie
 */
@Tag(name = "管理端 CgTestKnowledgeCategory API")
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class AdminCgTestKnowledgeCategoryController {

    private final CgTestKnowledgeCategoryService cgTestKnowledgeCategoryService;

    /** 创建。 */
    @Operation(summary = "创建。")
    @PostMapping("/v1/admin/biz/cg-test-knowledge-category/create")
    @SaCheckPermission(value = "biz:cgtestknowledgecategory:create", type = StpKit.TYPE_ADMIN)
    @OperationAudit(resourceType = "biz_cgtestknowledgecategory", action = "create")
    public ApiResponse<Void> create(@Valid @RequestBody CgTestKnowledgeCategoryAddParam param) {
        cgTestKnowledgeCategoryService.create(param);
        return ApiResponse.ok();
    }

    /** 更新。 */
    @Operation(summary = "更新。")
    @PostMapping("/v1/admin/biz/cg-test-knowledge-category/update")
    @SaCheckPermission(value = "biz:cgtestknowledgecategory:update", type = StpKit.TYPE_ADMIN)
    @OperationAudit(resourceType = "biz_cgtestknowledgecategory", action = "update")
    public ApiResponse<Void> update(@Valid @RequestBody CgTestKnowledgeCategoryEditParam param) {
        cgTestKnowledgeCategoryService.update(param);
        return ApiResponse.ok();
    }

    /** 批量删除。 */
    @Operation(summary = "批量删除。")
    @PostMapping("/v1/admin/biz/cg-test-knowledge-category/delete")
    @SaCheckPermission(value = "biz:cgtestknowledgecategory:delete", type = StpKit.TYPE_ADMIN)
    @OperationAudit(resourceType = "biz_cgtestknowledgecategory", action = "delete")
    public ApiResponse<Void> delete(@Valid @RequestBody IdsParam param) {
        cgTestKnowledgeCategoryService.delete(param);
        return ApiResponse.ok();
    }

    /** 查询详情。 */
    @Operation(summary = "查询详情。")
    @GetMapping("/v1/admin/biz/cg-test-knowledge-category/detail")
    @SaCheckPermission(value = "biz:cgtestknowledgecategory:detail", type = StpKit.TYPE_ADMIN)
    public ApiResponse<CgTestKnowledgeCategory> detail(@Valid @ModelAttribute IdParam param) {
        return ApiResponse.ok(cgTestKnowledgeCategoryService.detail(param.getId()));
    }

    /** 分页查询。 */
    @Operation(summary = "分页查询。")
    @GetMapping("/v1/admin/biz/cg-test-knowledge-category/page")
    @SaCheckPermission(value = "biz:cgtestknowledgecategory:page", type = StpKit.TYPE_ADMIN)
    public ApiResponse<Page<CgTestKnowledgeCategory>> page(@Valid @ModelAttribute CgTestKnowledgeCategoryPageParam param) {
        return ApiResponse.ok(cgTestKnowledgeCategoryService.page(param));
    }

    /** 树形查询。 */
    @Operation(summary = "树形查询。")
    @GetMapping("/v1/admin/biz/cg-test-knowledge-category/tree")
    @SaCheckPermission(value = "biz:cgtestknowledgecategory:tree", type = StpKit.TYPE_ADMIN)
    public ApiResponse<List<Tree<String>>> tree(@RequestParam(required = false) String keyword) {
        return ApiResponse.ok(cgTestKnowledgeCategoryService.tree(keyword));
    }

    /** 创建子项。 */
    @Operation(summary = "创建子项。")
    @PostMapping("/v1/admin/biz/cg-test-knowledge-category/children/create")
    @SaCheckPermission(value = "biz:cgtestknowledgecategory:create", type = StpKit.TYPE_ADMIN)
    @OperationAudit(resourceType = "biz_cgtestknowledgecategory", action = "create")
    public ApiResponse<Void> childCreate(@Valid @RequestBody CgTestKnowledgeDocAddParam param) {
        cgTestKnowledgeCategoryService.childCreate(param);
        return ApiResponse.ok();
    }

    /** 更新子项。 */
    @Operation(summary = "更新子项。")
    @PostMapping("/v1/admin/biz/cg-test-knowledge-category/children/update")
    @SaCheckPermission(value = "biz:cgtestknowledgecategory:update", type = StpKit.TYPE_ADMIN)
    @OperationAudit(resourceType = "biz_cgtestknowledgecategory", action = "update")
    public ApiResponse<Void> childUpdate(@Valid @RequestBody CgTestKnowledgeDocEditParam param) {
        cgTestKnowledgeCategoryService.childUpdate(param);
        return ApiResponse.ok();
    }

    /** 删除子项。 */
    @Operation(summary = "删除子项。")
    @PostMapping("/v1/admin/biz/cg-test-knowledge-category/children/delete")
    @SaCheckPermission(value = "biz:cgtestknowledgecategory:delete", type = StpKit.TYPE_ADMIN)
    @OperationAudit(resourceType = "biz_cgtestknowledgecategory", action = "delete")
    public ApiResponse<Void> childDelete(@Valid @RequestBody IdsParam param) {
        cgTestKnowledgeCategoryService.childDelete(param);
        return ApiResponse.ok();
    }

    /** 查询子项详情。 */
    @Operation(summary = "查询子项详情。")
    @GetMapping("/v1/admin/biz/cg-test-knowledge-category/children/detail")
    @SaCheckPermission(value = "biz:cgtestknowledgecategory:detail", type = StpKit.TYPE_ADMIN)
    public ApiResponse<CgTestKnowledgeDoc> childDetail(@Valid @ModelAttribute IdParam param) {
        return ApiResponse.ok(cgTestKnowledgeCategoryService.childDetail(param.getId()));
    }

    /** 分页查询子项。 */
    @Operation(summary = "分页查询子项。")
    @GetMapping("/v1/admin/biz/cg-test-knowledge-category/children/page")
    @SaCheckPermission(value = "biz:cgtestknowledgecategory:page", type = StpKit.TYPE_ADMIN)
    public ApiResponse<Page<CgTestKnowledgeDoc>> childPage(@Valid @ModelAttribute CgTestKnowledgeDocPageParam param) {
        return ApiResponse.ok(cgTestKnowledgeCategoryService.childPage(param));
    }
}
