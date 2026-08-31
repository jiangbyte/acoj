package github.jiangbyte.io.oj.modules.problemsolution.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import github.jiangbyte.io.common.core.domain.ApiResponse;
import github.jiangbyte.io.common.core.param.IdParam;
import github.jiangbyte.io.common.core.param.IdsParam;
import github.jiangbyte.io.common.log.annotation.OperationAudit;
import github.jiangbyte.io.common.satoken.StpKit;
import github.jiangbyte.io.oj.modules.problemsolution.entity.OjProblemSolution;
import github.jiangbyte.io.oj.modules.problemsolution.param.OjProblemSolutionAddParam;
import github.jiangbyte.io.oj.modules.problemsolution.param.OjProblemSolutionEditParam;
import github.jiangbyte.io.oj.modules.problemsolution.param.OjProblemSolutionPageParam;
import github.jiangbyte.io.oj.modules.problemsolution.service.OjProblemSolutionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 管理端 OJ 参考答案 API。
 *
 * Author: Charlie
 */
@Tag(name = "管理端 OJ 参考答案 API")
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class AdminOjProblemSolutionController {

    private final OjProblemSolutionService ojProblemSolutionService;

    /** 创建。 */
    @Operation(summary = "创建。")
    @PostMapping("/v1/admin/oj/problem-solutions/create")
    @SaCheckPermission(value = "oj:problem:update", type = StpKit.TYPE_ADMIN)
    @OperationAudit(resourceType = "oj_problem_solution", action = "create")
    public ApiResponse<Void> create(@Valid @RequestBody OjProblemSolutionAddParam param) {
        ojProblemSolutionService.create(param);
        return ApiResponse.ok();
    }

    /** 更新。 */
    @Operation(summary = "更新。")
    @PostMapping("/v1/admin/oj/problem-solutions/update")
    @SaCheckPermission(value = "oj:problem:update", type = StpKit.TYPE_ADMIN)
    @OperationAudit(resourceType = "oj_problem_solution", action = "update")
    public ApiResponse<Void> update(@Valid @RequestBody OjProblemSolutionEditParam param) {
        ojProblemSolutionService.update(param);
        return ApiResponse.ok();
    }

    /** 批量删除。 */
    @Operation(summary = "批量删除。")
    @PostMapping("/v1/admin/oj/problem-solutions/delete")
    @SaCheckPermission(value = "oj:problem:update", type = StpKit.TYPE_ADMIN)
    @OperationAudit(resourceType = "oj_problem_solution", action = "delete")
    public ApiResponse<Void> delete(@Valid @RequestBody IdsParam param) {
        ojProblemSolutionService.delete(param);
        return ApiResponse.ok();
    }

    /** 查询详情。 */
    @Operation(summary = "查询详情。")
    @GetMapping("/v1/admin/oj/problem-solutions/detail")
    @SaCheckPermission(value = "oj:problem:detail", type = StpKit.TYPE_ADMIN)
    public ApiResponse<OjProblemSolution> detail(@Valid @ModelAttribute IdParam param) {
        return ApiResponse.ok(ojProblemSolutionService.detail(param.getId()));
    }

    /** 分页查询。 */
    @Operation(summary = "分页查询。")
    @GetMapping("/v1/admin/oj/problem-solutions/page")
    @SaCheckPermission(value = "oj:problem:detail", type = StpKit.TYPE_ADMIN)
    public ApiResponse<Page<OjProblemSolution>> page(@Valid @ModelAttribute OjProblemSolutionPageParam param) {
        return ApiResponse.ok(ojProblemSolutionService.page(param));
    }

    /** 题目下全部参考答案。 */
    @Operation(summary = "题目下全部参考答案。")
    @GetMapping("/v1/admin/oj/problem-solutions/list")
    @SaCheckPermission(value = "oj:problem:detail", type = StpKit.TYPE_ADMIN)
    public ApiResponse<List<OjProblemSolution>> list(@Valid @ModelAttribute ProblemIdQuery param) {
        return ApiResponse.ok(ojProblemSolutionService.listByProblemId(param.getProblemId()));
    }

    @Data
    public static class ProblemIdQuery {
        @NotBlank
        private String problemId;
    }
}
