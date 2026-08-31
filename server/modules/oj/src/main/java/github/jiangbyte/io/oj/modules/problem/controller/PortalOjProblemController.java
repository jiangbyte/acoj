package github.jiangbyte.io.oj.modules.problem.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import github.jiangbyte.io.common.core.domain.ApiResponse;
import github.jiangbyte.io.common.core.param.IdParam;
import github.jiangbyte.io.common.satoken.StpKit;
import github.jiangbyte.io.oj.modules.problem.entity.OjProblem;
import github.jiangbyte.io.oj.modules.problem.param.OjProblemPageParam;
import github.jiangbyte.io.oj.modules.problem.param.OjProblemPortalRunParam;
import github.jiangbyte.io.oj.modules.problem.result.OjProblemPortalRunResult;
import github.jiangbyte.io.oj.modules.problem.service.OjProblemService;
import github.jiangbyte.io.oj.modules.problem.service.PortalProblemRunService;
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
 * 门户端 OJ 题目 API：已发布题目分页与详情（不含私有测例）；样例试跑需登录。
 *
 * Author: Charlie
 */
@Tag(name = "门户端 OJ 题目 API")
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class PortalOjProblemController {

    private final OjProblemService ojProblemService;
    private final OjTagService ojTagService;
    private final PortalProblemRunService portalProblemRunService;

    /**
     * 分页查询已发布题目（匿名可读）。列表不填 tags，避免多余关联查询。
     */
    @Operation(summary = "分页查询已发布题目。")
    @GetMapping("/v1/portal/oj/problems/page")
    public ApiResponse<Page<OjProblem>> page(@Valid @ModelAttribute OjProblemPageParam param) {
        return ApiResponse.ok(ojProblemService.portalPage(param));
    }

    /** 查询已发布题目详情（题面/样例；不含私有测例；匿名可读）。 */
    @Operation(summary = "查询已发布题目详情。")
    @GetMapping("/v1/portal/oj/problems/detail")
    public ApiResponse<OjProblem> detail(@Valid @ModelAttribute IdParam param) {
        OjProblem problem = ojProblemService.portalDetail(param.getId());
        ojTagService.fillProblemTags(problem);
        return ApiResponse.ok(problem);
    }

    /**
     * 样例/自定义用例同步试跑（需登录，不入提交记录）。
     */
    @Operation(summary = "样例试跑。")
    @PostMapping("/v1/portal/oj/problems/run")
    public ApiResponse<OjProblemPortalRunResult> run(@Valid @RequestBody OjProblemPortalRunParam param) {
        StpKit.PORTAL.checkLogin();
        return ApiResponse.ok(portalProblemRunService.run(param));
    }
}
