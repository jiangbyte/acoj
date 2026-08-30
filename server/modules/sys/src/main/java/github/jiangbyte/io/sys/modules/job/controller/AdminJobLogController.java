package github.jiangbyte.io.sys.modules.job.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import cn.dev33.satoken.annotation.SaCheckPermission;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import github.jiangbyte.io.common.core.domain.ApiResponse;
import github.jiangbyte.io.common.satoken.StpKit;
import github.jiangbyte.io.sys.modules.job.entity.SysJobLog;
import github.jiangbyte.io.sys.modules.job.param.SysJobLogPageParam;
import github.jiangbyte.io.sys.modules.job.service.JobLogService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 管理端任务执行记录 API。
 *
 * Author: Charlie
 */
@Tag(name = "管理端任务执行记录 API")
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class AdminJobLogController {

    private final JobLogService jobLogService;

    /** 分页查询执行记录。 */
    @Operation(summary = "分页查询执行记录。")
    @GetMapping("/v1/admin/sys/job-logs/page")
    @SaCheckPermission(value = "sys:joblog:page", type = StpKit.TYPE_ADMIN)
    public ApiResponse<Page<SysJobLog>> page(@Valid @ModelAttribute SysJobLogPageParam param) {
        return ApiResponse.ok(jobLogService.page(param));
    }
}
