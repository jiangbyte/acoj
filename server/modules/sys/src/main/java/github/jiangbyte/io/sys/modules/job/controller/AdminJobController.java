package github.jiangbyte.io.sys.modules.job.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import cn.dev33.satoken.annotation.SaCheckPermission;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import github.jiangbyte.io.common.core.domain.ApiResponse;
import github.jiangbyte.io.common.core.param.IdParam;
import github.jiangbyte.io.common.core.param.IdsParam;
import github.jiangbyte.io.common.log.annotation.OperationAudit;
import github.jiangbyte.io.common.satoken.StpKit;
import github.jiangbyte.io.sys.modules.job.entity.SysJob;
import github.jiangbyte.io.sys.modules.job.param.SysJobAddParam;
import github.jiangbyte.io.sys.modules.job.param.SysJobEditParam;
import github.jiangbyte.io.sys.modules.job.param.SysJobEnabledParam;
import github.jiangbyte.io.sys.modules.job.param.SysJobPageParam;
import github.jiangbyte.io.sys.modules.job.service.JobService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 管理端任务 API：CRUD、启停与立即执行。
 *
 * Author: Charlie
 */
@Tag(name = "管理端任务 API")
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class AdminJobController {

    private final JobService jobService;

    /** 分页查询。 */
    @Operation(summary = "分页查询。")
    @GetMapping("/v1/admin/sys/jobs/page")
    @SaCheckPermission(value = "sys:job:page", type = StpKit.TYPE_ADMIN)
    public ApiResponse<Page<SysJob>> page(@Valid @ModelAttribute SysJobPageParam param) {
        return ApiResponse.ok(jobService.page(param));
    }

    /** 查询详情。 */
    @Operation(summary = "查询详情。")
    @GetMapping("/v1/admin/sys/jobs/detail")
    @SaCheckPermission(value = "sys:job:detail", type = StpKit.TYPE_ADMIN)
    public ApiResponse<SysJob> detail(@Valid @ModelAttribute IdParam param) {
        return ApiResponse.ok(jobService.detail(param.getId()));
    }

    /** 创建。 */
    @Operation(summary = "创建。")
    @PostMapping("/v1/admin/sys/jobs/create")
    @SaCheckPermission(value = "sys:job:create", type = StpKit.TYPE_ADMIN)
    @OperationAudit(resourceType = "sys_job", action = "create")
    public ApiResponse<Void> create(@Valid @RequestBody SysJobAddParam param) {
        jobService.create(param);
        return ApiResponse.ok();
    }

    /** 更新。 */
    @Operation(summary = "更新。")
    @PostMapping("/v1/admin/sys/jobs/update")
    @SaCheckPermission(value = "sys:job:update", type = StpKit.TYPE_ADMIN)
    @OperationAudit(resourceType = "sys_job", action = "update")
    public ApiResponse<Void> update(@Valid @RequestBody SysJobEditParam param) {
        jobService.update(param);
        return ApiResponse.ok();
    }

    /** 批量删除。 */
    @Operation(summary = "批量删除。")
    @PostMapping("/v1/admin/sys/jobs/delete")
    @SaCheckPermission(value = "sys:job:delete", type = StpKit.TYPE_ADMIN)
    @OperationAudit(resourceType = "sys_job", action = "delete")
    public ApiResponse<Void> delete(@Valid @RequestBody IdsParam param) {
        jobService.delete(param);
        return ApiResponse.ok();
    }

    /** 启停。 */
    @Operation(summary = "启停。")
    @PostMapping("/v1/admin/sys/jobs/enabled")
    @SaCheckPermission(value = "sys:job:update", type = StpKit.TYPE_ADMIN)
    @OperationAudit(resourceType = "sys_job", action = "enabled")
    public ApiResponse<Void> enabled(@Valid @RequestBody SysJobEnabledParam param) {
        jobService.updateEnabled(param);
        return ApiResponse.ok();
    }

    /** 立即执行。 */
    @Operation(summary = "立即执行。")
    @PostMapping("/v1/admin/sys/jobs/run")
    @SaCheckPermission(value = "sys:job:run", type = StpKit.TYPE_ADMIN)
    @OperationAudit(resourceType = "sys_job", action = "run")
    public ApiResponse<Void> run(@Valid @RequestBody IdParam param) {
        jobService.runNow(param.getId());
        return ApiResponse.ok();
    }
}
