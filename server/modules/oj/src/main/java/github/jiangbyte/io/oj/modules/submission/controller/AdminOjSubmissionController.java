package github.jiangbyte.io.oj.modules.submission.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import github.jiangbyte.io.common.core.domain.ApiResponse;
import github.jiangbyte.io.common.core.param.IdParam;
import github.jiangbyte.io.common.satoken.StpKit;
import github.jiangbyte.io.oj.modules.submission.entity.OjSubmission;
import github.jiangbyte.io.oj.modules.submission.param.OjSubmissionPageParam;
import github.jiangbyte.io.oj.modules.submission.service.OjSubmissionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 管理端 OJ 提交 API：分页与详情查询。
 *
 * Author: Charlie
 */
@Tag(name = "管理端 OJ 提交 API")
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class AdminOjSubmissionController {

    private final OjSubmissionService ojSubmissionService;

    /** 查询详情。 */
    @Operation(summary = "查询详情。")
    @GetMapping("/v1/admin/oj/submissions/detail")
    @SaCheckPermission(value = "oj:submission:detail", type = StpKit.TYPE_ADMIN)
    public ApiResponse<OjSubmission> detail(@Valid @ModelAttribute IdParam param) {
        return ApiResponse.ok(ojSubmissionService.detail(param.getId()));
    }

    /** 分页查询。 */
    @Operation(summary = "分页查询。")
    @GetMapping("/v1/admin/oj/submissions/page")
    @SaCheckPermission(value = "oj:submission:page", type = StpKit.TYPE_ADMIN)
    public ApiResponse<Page<OjSubmission>> page(@Valid @ModelAttribute OjSubmissionPageParam param) {
        return ApiResponse.ok(ojSubmissionService.page(param));
    }
}
