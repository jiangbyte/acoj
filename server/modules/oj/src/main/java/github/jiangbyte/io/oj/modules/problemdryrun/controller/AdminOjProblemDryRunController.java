package github.jiangbyte.io.oj.modules.problemdryrun.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import github.jiangbyte.io.common.core.domain.ApiResponse;
import github.jiangbyte.io.common.core.param.IdParam;
import github.jiangbyte.io.common.log.annotation.OperationAudit;
import github.jiangbyte.io.common.satoken.StpKit;
import github.jiangbyte.io.oj.modules.problemdryrun.entity.OjProblemDryRun;
import github.jiangbyte.io.oj.modules.problemdryrun.param.OjProblemApplyLimitsParam;
import github.jiangbyte.io.oj.modules.problemdryrun.param.OjProblemDryRunPageParam;
import github.jiangbyte.io.oj.modules.problemdryrun.param.OjProblemDryRunParam;
import github.jiangbyte.io.oj.modules.problemdryrun.service.OjProblemDryRunService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 管理端 OJ 试跑 / 限额写回 API。
 *
 * Author: Charlie
 */
@Tag(name = "管理端 OJ 试跑 API")
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class AdminOjProblemDryRunController {

    private final OjProblemDryRunService ojProblemDryRunService;

    /** 同步试跑。 */
    @Operation(summary = "同步试跑（单测例或全测例）。")
    @PostMapping("/v1/admin/oj/problems/dry-run")
    @SaCheckPermission(value = "oj:problem:update", type = StpKit.TYPE_ADMIN)
    @OperationAudit(resourceType = "oj_problem_dry_run", action = "dry-run")
    public ApiResponse<OjProblemDryRun> dryRun(@Valid @RequestBody OjProblemDryRunParam param) {
        return ApiResponse.ok(ojProblemDryRunService.dryRun(param));
    }

    /** 试跑历史分页。 */
    @Operation(summary = "试跑历史分页。")
    @GetMapping("/v1/admin/oj/problems/dry-runs/page")
    @SaCheckPermission(value = "oj:problem:detail", type = StpKit.TYPE_ADMIN)
    public ApiResponse<Page<OjProblemDryRun>> page(@Valid @ModelAttribute OjProblemDryRunPageParam param) {
        return ApiResponse.ok(ojProblemDryRunService.page(param));
    }

    /** 试跑详情。 */
    @Operation(summary = "试跑详情。")
    @GetMapping("/v1/admin/oj/problems/dry-runs/detail")
    @SaCheckPermission(value = "oj:problem:detail", type = StpKit.TYPE_ADMIN)
    public ApiResponse<OjProblemDryRun> detail(@Valid @ModelAttribute IdParam param) {
        return ApiResponse.ok(ojProblemDryRunService.detail(param.getId()));
    }

    /** 写回题目时限/内存。 */
    @Operation(summary = "写回题目时限/内存。")
    @PostMapping("/v1/admin/oj/problems/apply-limits")
    @SaCheckPermission(value = "oj:problem:update", type = StpKit.TYPE_ADMIN)
    @OperationAudit(resourceType = "oj_problem", action = "apply-limits")
    public ApiResponse<Void> applyLimits(@Valid @RequestBody OjProblemApplyLimitsParam param) {
        ojProblemDryRunService.applyLimits(param);
        return ApiResponse.ok();
    }
}
