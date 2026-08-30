package github.jiangbyte.io.biz.modules.cg_test_order.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import cn.dev33.satoken.annotation.SaCheckPermission;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import github.jiangbyte.io.common.core.domain.ApiResponse;
import github.jiangbyte.io.common.core.param.IdParam;
import github.jiangbyte.io.common.core.param.IdsParam;
import github.jiangbyte.io.common.log.annotation.OperationAudit;
import github.jiangbyte.io.common.satoken.StpKit;
import github.jiangbyte.io.biz.modules.cg_test_order.entity.CgTestOrder;
import github.jiangbyte.io.biz.modules.cg_test_order.param.CgTestOrderAddParam;
import github.jiangbyte.io.biz.modules.cg_test_order.param.CgTestOrderEditParam;
import github.jiangbyte.io.biz.modules.cg_test_order.param.CgTestOrderPageParam;
import github.jiangbyte.io.biz.modules.cg_test_order.service.CgTestOrderService;
import github.jiangbyte.io.biz.modules.cg_test_order.entity.CgTestOrderItem;
import github.jiangbyte.io.biz.modules.cg_test_order.param.CgTestOrderItemAddParam;
import github.jiangbyte.io.biz.modules.cg_test_order.param.CgTestOrderItemEditParam;
import github.jiangbyte.io.biz.modules.cg_test_order.param.CgTestOrderItemPageParam;
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
 * 管理端 CgTestOrder API：CRUD与子实体维护。
 *
 * Author: Charlie
 */
@Tag(name = "管理端 CgTestOrder API")
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class AdminCgTestOrderController {

    private final CgTestOrderService cgTestOrderService;

    /** 创建。 */
    @Operation(summary = "创建。")
    @PostMapping("/v1/admin/biz/cg-test-order/create")
    @SaCheckPermission(value = "biz:cgtestorder:create", type = StpKit.TYPE_ADMIN)
    @OperationAudit(resourceType = "biz_cgtestorder", action = "create")
    public ApiResponse<Void> create(@Valid @RequestBody CgTestOrderAddParam param) {
        cgTestOrderService.create(param);
        return ApiResponse.ok();
    }

    /** 更新。 */
    @Operation(summary = "更新。")
    @PostMapping("/v1/admin/biz/cg-test-order/update")
    @SaCheckPermission(value = "biz:cgtestorder:update", type = StpKit.TYPE_ADMIN)
    @OperationAudit(resourceType = "biz_cgtestorder", action = "update")
    public ApiResponse<Void> update(@Valid @RequestBody CgTestOrderEditParam param) {
        cgTestOrderService.update(param);
        return ApiResponse.ok();
    }

    /** 批量删除。 */
    @Operation(summary = "批量删除。")
    @PostMapping("/v1/admin/biz/cg-test-order/delete")
    @SaCheckPermission(value = "biz:cgtestorder:delete", type = StpKit.TYPE_ADMIN)
    @OperationAudit(resourceType = "biz_cgtestorder", action = "delete")
    public ApiResponse<Void> delete(@Valid @RequestBody IdsParam param) {
        cgTestOrderService.delete(param);
        return ApiResponse.ok();
    }

    /** 查询详情。 */
    @Operation(summary = "查询详情。")
    @GetMapping("/v1/admin/biz/cg-test-order/detail")
    @SaCheckPermission(value = "biz:cgtestorder:detail", type = StpKit.TYPE_ADMIN)
    public ApiResponse<CgTestOrder> detail(@Valid @ModelAttribute IdParam param) {
        return ApiResponse.ok(cgTestOrderService.detail(param.getId()));
    }

    /** 分页查询。 */
    @Operation(summary = "分页查询。")
    @GetMapping("/v1/admin/biz/cg-test-order/page")
    @SaCheckPermission(value = "biz:cgtestorder:page", type = StpKit.TYPE_ADMIN)
    public ApiResponse<Page<CgTestOrder>> page(@Valid @ModelAttribute CgTestOrderPageParam param) {
        return ApiResponse.ok(cgTestOrderService.page(param));
    }

    /** 创建子项。 */
    @Operation(summary = "创建子项。")
    @PostMapping("/v1/admin/biz/cg-test-order/children/create")
    @SaCheckPermission(value = "biz:cgtestorder:create", type = StpKit.TYPE_ADMIN)
    @OperationAudit(resourceType = "biz_cgtestorder", action = "create")
    public ApiResponse<Void> childCreate(@Valid @RequestBody CgTestOrderItemAddParam param) {
        cgTestOrderService.childCreate(param);
        return ApiResponse.ok();
    }

    /** 更新子项。 */
    @Operation(summary = "更新子项。")
    @PostMapping("/v1/admin/biz/cg-test-order/children/update")
    @SaCheckPermission(value = "biz:cgtestorder:update", type = StpKit.TYPE_ADMIN)
    @OperationAudit(resourceType = "biz_cgtestorder", action = "update")
    public ApiResponse<Void> childUpdate(@Valid @RequestBody CgTestOrderItemEditParam param) {
        cgTestOrderService.childUpdate(param);
        return ApiResponse.ok();
    }

    /** 删除子项。 */
    @Operation(summary = "删除子项。")
    @PostMapping("/v1/admin/biz/cg-test-order/children/delete")
    @SaCheckPermission(value = "biz:cgtestorder:delete", type = StpKit.TYPE_ADMIN)
    @OperationAudit(resourceType = "biz_cgtestorder", action = "delete")
    public ApiResponse<Void> childDelete(@Valid @RequestBody IdsParam param) {
        cgTestOrderService.childDelete(param);
        return ApiResponse.ok();
    }

    /** 查询子项详情。 */
    @Operation(summary = "查询子项详情。")
    @GetMapping("/v1/admin/biz/cg-test-order/children/detail")
    @SaCheckPermission(value = "biz:cgtestorder:detail", type = StpKit.TYPE_ADMIN)
    public ApiResponse<CgTestOrderItem> childDetail(@Valid @ModelAttribute IdParam param) {
        return ApiResponse.ok(cgTestOrderService.childDetail(param.getId()));
    }

    /** 分页查询子项。 */
    @Operation(summary = "分页查询子项。")
    @GetMapping("/v1/admin/biz/cg-test-order/children/page")
    @SaCheckPermission(value = "biz:cgtestorder:page", type = StpKit.TYPE_ADMIN)
    public ApiResponse<Page<CgTestOrderItem>> childPage(@Valid @ModelAttribute CgTestOrderItemPageParam param) {
        return ApiResponse.ok(cgTestOrderService.childPage(param));
    }
}
