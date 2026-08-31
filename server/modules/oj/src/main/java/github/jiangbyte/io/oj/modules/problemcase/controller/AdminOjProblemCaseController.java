package github.jiangbyte.io.oj.modules.problemcase.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import github.jiangbyte.io.common.core.domain.ApiResponse;
import github.jiangbyte.io.common.core.param.IdParam;
import github.jiangbyte.io.common.core.param.IdsParam;
import github.jiangbyte.io.common.log.annotation.OperationAudit;
import github.jiangbyte.io.common.satoken.StpKit;
import github.jiangbyte.io.oj.modules.problemcase.entity.OjProblemCase;
import github.jiangbyte.io.oj.modules.problemcase.param.OjProblemCaseAddParam;
import github.jiangbyte.io.oj.modules.problemcase.param.OjProblemCaseEditParam;
import github.jiangbyte.io.oj.modules.problemcase.param.OjProblemCasePageParam;
import github.jiangbyte.io.oj.modules.problemcase.service.OjProblemCaseService;
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
 * 管理端 OJ 题目测例 API：CRUD（权限复用 oj:problem:*）。
 *
 * Author: Charlie
 */
@Tag(name = "管理端 OJ 题目测例 API")
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class AdminOjProblemCaseController {

    private final OjProblemCaseService ojProblemCaseService;

    /** 创建。 */
    @Operation(summary = "创建。")
    @PostMapping("/v1/admin/oj/problem-cases/create")
    @SaCheckPermission(value = "oj:problem:update", type = StpKit.TYPE_ADMIN)
    @OperationAudit(resourceType = "oj_problem_case", action = "create")
    public ApiResponse<Void> create(@Valid @RequestBody OjProblemCaseAddParam param) {
        ojProblemCaseService.create(param);
        return ApiResponse.ok();
    }

    /** 更新。 */
    @Operation(summary = "更新。")
    @PostMapping("/v1/admin/oj/problem-cases/update")
    @SaCheckPermission(value = "oj:problem:update", type = StpKit.TYPE_ADMIN)
    @OperationAudit(resourceType = "oj_problem_case", action = "update")
    public ApiResponse<Void> update(@Valid @RequestBody OjProblemCaseEditParam param) {
        ojProblemCaseService.update(param);
        return ApiResponse.ok();
    }

    /** 批量删除。 */
    @Operation(summary = "批量删除。")
    @PostMapping("/v1/admin/oj/problem-cases/delete")
    @SaCheckPermission(value = "oj:problem:update", type = StpKit.TYPE_ADMIN)
    @OperationAudit(resourceType = "oj_problem_case", action = "delete")
    public ApiResponse<Void> delete(@Valid @RequestBody IdsParam param) {
        ojProblemCaseService.delete(param);
        return ApiResponse.ok();
    }

    /** 查询详情。 */
    @Operation(summary = "查询详情。")
    @GetMapping("/v1/admin/oj/problem-cases/detail")
    @SaCheckPermission(value = "oj:problem:detail", type = StpKit.TYPE_ADMIN)
    public ApiResponse<OjProblemCase> detail(@Valid @ModelAttribute IdParam param) {
        return ApiResponse.ok(ojProblemCaseService.detail(param.getId()));
    }

    /** 分页查询。 */
    @Operation(summary = "分页查询。")
    @GetMapping("/v1/admin/oj/problem-cases/page")
    @SaCheckPermission(value = "oj:problem:page", type = StpKit.TYPE_ADMIN)
    public ApiResponse<Page<OjProblemCase>> page(@Valid @ModelAttribute OjProblemCasePageParam param) {
        return ApiResponse.ok(ojProblemCaseService.page(param));
    }
}
