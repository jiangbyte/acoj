package github.jiangbyte.io.biz.modules.cg_test_activity.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import cn.dev33.satoken.annotation.SaCheckPermission;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import github.jiangbyte.io.common.core.domain.ApiResponse;
import github.jiangbyte.io.common.core.param.IdParam;
import github.jiangbyte.io.common.core.param.IdsParam;
import github.jiangbyte.io.common.log.annotation.OperationAudit;
import github.jiangbyte.io.common.satoken.StpKit;
import github.jiangbyte.io.biz.modules.cg_test_activity.entity.CgTestActivity;
import github.jiangbyte.io.biz.modules.cg_test_activity.param.CgTestActivityAddParam;
import github.jiangbyte.io.biz.modules.cg_test_activity.param.CgTestActivityEditParam;
import github.jiangbyte.io.biz.modules.cg_test_activity.param.CgTestActivityPageParam;
import github.jiangbyte.io.biz.modules.cg_test_activity.service.CgTestActivityService;
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
 * 管理端 CgTestActivity API：CRUD。
 *
 * Author: Charlie
 */
@Tag(name = "管理端 CgTestActivity API")
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class AdminCgTestActivityController {

    private final CgTestActivityService cgTestActivityService;

    /** 创建。 */
    @Operation(summary = "创建。")
    @PostMapping("/v1/admin/biz/cg-test-activity/create")
    @SaCheckPermission(value = "biz:cgtestactivity:create", type = StpKit.TYPE_ADMIN)
    @OperationAudit(resourceType = "biz_cgtestactivity", action = "create")
    public ApiResponse<Void> create(@Valid @RequestBody CgTestActivityAddParam param) {
        cgTestActivityService.create(param);
        return ApiResponse.ok();
    }

    /** 更新。 */
    @Operation(summary = "更新。")
    @PostMapping("/v1/admin/biz/cg-test-activity/update")
    @SaCheckPermission(value = "biz:cgtestactivity:update", type = StpKit.TYPE_ADMIN)
    @OperationAudit(resourceType = "biz_cgtestactivity", action = "update")
    public ApiResponse<Void> update(@Valid @RequestBody CgTestActivityEditParam param) {
        cgTestActivityService.update(param);
        return ApiResponse.ok();
    }

    /** 批量删除。 */
    @Operation(summary = "批量删除。")
    @PostMapping("/v1/admin/biz/cg-test-activity/delete")
    @SaCheckPermission(value = "biz:cgtestactivity:delete", type = StpKit.TYPE_ADMIN)
    @OperationAudit(resourceType = "biz_cgtestactivity", action = "delete")
    public ApiResponse<Void> delete(@Valid @RequestBody IdsParam param) {
        cgTestActivityService.delete(param);
        return ApiResponse.ok();
    }

    /** 查询详情。 */
    @Operation(summary = "查询详情。")
    @GetMapping("/v1/admin/biz/cg-test-activity/detail")
    @SaCheckPermission(value = "biz:cgtestactivity:detail", type = StpKit.TYPE_ADMIN)
    public ApiResponse<CgTestActivity> detail(@Valid @ModelAttribute IdParam param) {
        return ApiResponse.ok(cgTestActivityService.detail(param.getId()));
    }

    /** 分页查询。 */
    @Operation(summary = "分页查询。")
    @GetMapping("/v1/admin/biz/cg-test-activity/page")
    @SaCheckPermission(value = "biz:cgtestactivity:page", type = StpKit.TYPE_ADMIN)
    public ApiResponse<Page<CgTestActivity>> page(@Valid @ModelAttribute CgTestActivityPageParam param) {
        return ApiResponse.ok(cgTestActivityService.page(param));
    }
}
