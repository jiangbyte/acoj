package github.jiangbyte.io.oj.modules.problem.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import github.jiangbyte.io.common.core.domain.ApiResponse;
import github.jiangbyte.io.common.core.param.IdParam;
import github.jiangbyte.io.common.core.param.IdsParam;
import github.jiangbyte.io.common.log.annotation.OperationAudit;
import github.jiangbyte.io.common.satoken.StpKit;
import github.jiangbyte.io.oj.modules.problem.entity.OjProblem;
import github.jiangbyte.io.oj.modules.problem.param.OjProblemAddParam;
import github.jiangbyte.io.oj.modules.problem.param.OjProblemEditParam;
import github.jiangbyte.io.oj.modules.problem.param.OjProblemPageParam;
import github.jiangbyte.io.oj.modules.problem.param.OjProblemReplaceCasesParam;
import github.jiangbyte.io.oj.modules.problem.param.OjProblemSetTagsParam;
import github.jiangbyte.io.oj.modules.problem.service.OjProblemService;
import github.jiangbyte.io.oj.modules.problemcase.service.OjProblemCaseService;
import github.jiangbyte.io.oj.modules.problemlanguagelimit.service.OjProblemLanguageLimitService;
import github.jiangbyte.io.oj.modules.tag.service.OjTagService;
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
 * 管理端 OJ 题目 API：CRUD、测例升版本、打标。
 *
 * Author: Charlie
 */
@Tag(name = "管理端 OJ 题目 API")
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class AdminOjProblemController {

    private final OjProblemService ojProblemService;
    private final OjProblemCaseService ojProblemCaseService;
    private final OjTagService ojTagService;
    private final OjProblemLanguageLimitService ojProblemLanguageLimitService;

    /** 创建。 */
    @Operation(summary = "创建。")
    @PostMapping("/v1/admin/oj/problems/create")
    @SaCheckPermission(value = "oj:problem:create", type = StpKit.TYPE_ADMIN)
    @OperationAudit(resourceType = "oj_problem", action = "create")
    public ApiResponse<Void> create(@Valid @RequestBody OjProblemAddParam param) {
        ojProblemService.create(param);
        return ApiResponse.ok();
    }

    /** 更新。 */
    @Operation(summary = "更新。")
    @PostMapping("/v1/admin/oj/problems/update")
    @SaCheckPermission(value = "oj:problem:update", type = StpKit.TYPE_ADMIN)
    @OperationAudit(resourceType = "oj_problem", action = "update")
    public ApiResponse<Void> update(@Valid @RequestBody OjProblemEditParam param) {
        ojProblemService.update(param);
        return ApiResponse.ok();
    }

    /** 批量删除。 */
    @Operation(summary = "批量删除。")
    @PostMapping("/v1/admin/oj/problems/delete")
    @SaCheckPermission(value = "oj:problem:delete", type = StpKit.TYPE_ADMIN)
    @OperationAudit(resourceType = "oj_problem", action = "delete")
    public ApiResponse<Void> delete(@Valid @RequestBody IdsParam param) {
        ojProblemService.delete(param);
        return ApiResponse.ok();
    }

    /** 查询详情。 */
    @Operation(summary = "查询详情。")
    @GetMapping("/v1/admin/oj/problems/detail")
    @SaCheckPermission(value = "oj:problem:detail", type = StpKit.TYPE_ADMIN)
    public ApiResponse<OjProblem> detail(@Valid @ModelAttribute IdParam param) {
        OjProblem problem = ojProblemService.detail(param.getId());
        ojTagService.fillProblemTags(problem);
        return ApiResponse.ok(problem);
    }

    /** 分页查询。 */
    @Operation(summary = "分页查询。")
    @GetMapping("/v1/admin/oj/problems/page")
    @SaCheckPermission(value = "oj:problem:page", type = StpKit.TYPE_ADMIN)
    public ApiResponse<Page<OjProblem>> page(@Valid @ModelAttribute OjProblemPageParam param) {
        Page<OjProblem> page = ojProblemService.page(param);
        ojTagService.fillProblemTags(page.getRecords());
        ojProblemLanguageLimitService.fillLanguageLimits(page.getRecords());
        return ApiResponse.ok(page);
    }

    /** 整包替换测例并升 caseVersion。 */
    @Operation(summary = "整包替换测例并升 caseVersion。")
    @PostMapping("/v1/admin/oj/problems/replace-cases")
    @SaCheckPermission(value = "oj:problem:update", type = StpKit.TYPE_ADMIN)
    @OperationAudit(resourceType = "oj_problem", action = "replace-cases")
    public ApiResponse<Void> replaceCases(@Valid @RequestBody OjProblemReplaceCasesParam param) {
        ojProblemCaseService.replaceCasesForNewVersion(param.getProblemId(), param.getCases());
        return ApiResponse.ok();
    }

    /** 覆盖设置题目标签。 */
    @Operation(summary = "覆盖设置题目标签。")
    @PostMapping("/v1/admin/oj/problems/set-tags")
    @SaCheckPermission(value = "oj:problem:update", type = StpKit.TYPE_ADMIN)
    @OperationAudit(resourceType = "oj_problem", action = "set-tags")
    public ApiResponse<Void> setTags(@Valid @RequestBody OjProblemSetTagsParam param) {
        ojTagService.setProblemTags(param.getProblemId(), param.getTagIds());
        return ApiResponse.ok();
    }
}
