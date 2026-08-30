package github.jiangbyte.io.iam.modules.position.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import cn.dev33.satoken.annotation.SaCheckPermission;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import github.jiangbyte.io.common.satoken.StpKit;

import github.jiangbyte.io.common.core.domain.ApiResponse;
import github.jiangbyte.io.common.core.param.IdParam;
import github.jiangbyte.io.common.core.param.IdsParam;
import github.jiangbyte.io.common.log.annotation.OperationAudit;
import github.jiangbyte.io.iam.modules.position.entity.SysPosition;
import github.jiangbyte.io.iam.modules.position.param.SysPositionAddParam;
import github.jiangbyte.io.iam.modules.position.param.SysPositionEditParam;
import github.jiangbyte.io.iam.modules.position.param.SysPositionPageParam;
import github.jiangbyte.io.iam.modules.position.service.PositionService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import lombok.RequiredArgsConstructor;

/**
 * 管理端岗位 API：岗位 CRUD 与分页。
 *
 * Author: Charlie
 */
@Tag(name = "管理端岗位 API")
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class AdminPositionController {

    private final PositionService positionService;

    /** 创建岗位。 */
    @Operation(summary = "创建岗位。")
    @PostMapping("/v1/admin/sys/positions/create")
    @SaCheckPermission(value = "iam:position:create", type = StpKit.TYPE_ADMIN)
    @OperationAudit(resourceType = "iam_position", action = "create")
    public ApiResponse<Void> create(@Valid @RequestBody SysPositionAddParam param) {
        positionService.create(param);
        return ApiResponse.ok();
    }

    /** 更新岗位。 */
    @Operation(summary = "更新岗位。")
    @PostMapping("/v1/admin/sys/positions/update")
    @SaCheckPermission(value = "iam:position:update", type = StpKit.TYPE_ADMIN)
    @OperationAudit(resourceType = "iam_position", action = "update")
    public ApiResponse<Void> update(@Valid @RequestBody SysPositionEditParam param) {
        positionService.update(param);
        return ApiResponse.ok();
    }

    /** 批量删除岗位。 */
    @Operation(summary = "批量删除岗位。")
    @PostMapping("/v1/admin/sys/positions/delete")
    @SaCheckPermission(value = "iam:position:delete", type = StpKit.TYPE_ADMIN)
    @OperationAudit(resourceType = "iam_position", action = "delete")
    public ApiResponse<Void> delete(@Valid @RequestBody IdsParam param) {
        positionService.delete(param);
        return ApiResponse.ok();
    }

    /** 查询岗位详情。 */
    @Operation(summary = "查询岗位详情。")
    @GetMapping("/v1/admin/sys/positions/detail")
    @SaCheckPermission(value = "iam:position:detail", type = StpKit.TYPE_ADMIN)
    public ApiResponse<SysPosition> detail(@Valid @ModelAttribute IdParam param) {
        return ApiResponse.ok(positionService.detail(param.getId()));
    }

    /** 分页查询岗位。 */
    @Operation(summary = "分页查询岗位。")
    @GetMapping("/v1/admin/sys/positions/page")
    @SaCheckPermission(value = "iam:position:page", type = StpKit.TYPE_ADMIN)
    public ApiResponse<Page<SysPosition>> page(@Valid @ModelAttribute SysPositionPageParam param) {
        return ApiResponse.ok(positionService.page(param));
    }
}
