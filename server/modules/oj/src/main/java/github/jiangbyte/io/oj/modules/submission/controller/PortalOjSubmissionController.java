package github.jiangbyte.io.oj.modules.submission.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import github.jiangbyte.io.common.core.domain.ApiResponse;
import github.jiangbyte.io.common.core.param.IdParam;
import github.jiangbyte.io.common.log.annotation.OperationAudit;
import github.jiangbyte.io.common.satoken.StpKit;
import github.jiangbyte.io.common.satoken.utils.LoginHelper;
import github.jiangbyte.io.oj.modules.submission.entity.OjSubmission;
import github.jiangbyte.io.oj.modules.submission.param.OjSubmissionCreateParam;
import github.jiangbyte.io.oj.modules.submission.param.OjSubmissionPageParam;
import github.jiangbyte.io.oj.modules.submission.service.OjSubmissionService;
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
 * 门户端 OJ 提交 API：创建、本人详情与分页。
 *
 * Author: Charlie
 */
@Tag(name = "门户端 OJ 提交 API")
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class PortalOjSubmissionController {

    private final OjSubmissionService ojSubmissionService;

    /** 提交代码并入队判题。 */
    @Operation(summary = "提交代码并入队判题。")
    @PostMapping("/v1/portal/oj/submissions/create")
    @OperationAudit(resourceType = "oj_submission", action = "create")
    public ApiResponse<OjSubmission> create(@Valid @RequestBody OjSubmissionCreateParam param) {
        StpKit.PORTAL.checkLogin();
        String accountId = LoginHelper.requireUser().getAccountId();
        return ApiResponse.ok(ojSubmissionService.createForPortal(accountId, param));
    }

    /** 查询本人提交详情。 */
    @Operation(summary = "查询本人提交详情。")
    @GetMapping("/v1/portal/oj/submissions/detail")
    public ApiResponse<OjSubmission> detail(@Valid @ModelAttribute IdParam param) {
        StpKit.PORTAL.checkLogin();
        String accountId = LoginHelper.requireUser().getAccountId();
        return ApiResponse.ok(ojSubmissionService.portalDetail(accountId, param.getId()));
    }

    /** 分页查询本人提交。 */
    @Operation(summary = "分页查询本人提交。")
    @GetMapping("/v1/portal/oj/submissions/page")
    public ApiResponse<Page<OjSubmission>> page(@Valid @ModelAttribute OjSubmissionPageParam param) {
        StpKit.PORTAL.checkLogin();
        String accountId = LoginHelper.requireUser().getAccountId();
        return ApiResponse.ok(ojSubmissionService.portalPage(accountId, param));
    }
}
