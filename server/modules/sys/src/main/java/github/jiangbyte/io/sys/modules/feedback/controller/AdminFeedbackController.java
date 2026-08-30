package github.jiangbyte.io.sys.modules.feedback.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import cn.dev33.satoken.annotation.SaCheckPermission;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import github.jiangbyte.io.common.core.domain.ApiResponse;
import github.jiangbyte.io.common.core.param.IdParam;
import github.jiangbyte.io.common.core.param.IdsParam;
import github.jiangbyte.io.common.log.annotation.OperationAudit;
import github.jiangbyte.io.common.satoken.StpKit;
import github.jiangbyte.io.sys.modules.feedback.entity.SysFeedback;
import github.jiangbyte.io.sys.modules.feedback.param.SysFeedbackAddParam;
import github.jiangbyte.io.sys.modules.feedback.param.SysFeedbackEditParam;
import github.jiangbyte.io.sys.modules.feedback.param.SysFeedbackPageParam;
import github.jiangbyte.io.sys.modules.feedback.service.FeedbackService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 管理端反馈 API：用户提交与查看本人反馈，以及管理员分页/详情/回复更新与删除。
 *
 * Author: Charlie
 */
@Tag(name = "管理端反馈 API")
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class AdminFeedbackController {

    private final FeedbackService feedbackService;

    /** 当前管理用户提交反馈。 */
    @Operation(summary = "当前管理用户提交反馈。")
    @PostMapping("/v1/admin/sys/feedbacks/submit")
    @OperationAudit(resourceType = "sys_feedback", action = "submit")
    public ApiResponse<Void> submit(@Valid @RequestBody SysFeedbackAddParam param) {
        feedbackService.submit(param);
        return ApiResponse.ok();
    }

    /** 分页查询当前用户提交的反馈。 */
    @Operation(summary = "分页查询当前用户提交的反馈。")
    @GetMapping("/v1/admin/sys/feedbacks/my-page")
    public ApiResponse<Page<SysFeedback>> myPage(@Valid @ModelAttribute SysFeedbackPageParam param) {
        return ApiResponse.ok(feedbackService.myPage(param));
    }

    /** 查询当前用户本人的反馈详情。 */
    @Operation(summary = "查询当前用户本人的反馈详情。")
    @GetMapping("/v1/admin/sys/feedbacks/my-detail")
    public ApiResponse<SysFeedback> myDetail(@Valid @ModelAttribute IdParam param) {
        return ApiResponse.ok(feedbackService.myDetail(param.getId()));
    }

    /** 管理端分页查询全部反馈。 */
    @Operation(summary = "管理端分页查询全部反馈。")
    @GetMapping("/v1/admin/sys/feedbacks/page")
    @SaCheckPermission(value = "sys:feedback:page", type = StpKit.TYPE_ADMIN)
    public ApiResponse<Page<SysFeedback>> page(@Valid @ModelAttribute SysFeedbackPageParam param) {
        return ApiResponse.ok(feedbackService.page(param));
    }

    /** 管理端查询反馈详情。 */
    @Operation(summary = "管理端查询反馈详情。")
    @GetMapping("/v1/admin/sys/feedbacks/detail")
    @SaCheckPermission(value = "sys:feedback:detail", type = StpKit.TYPE_ADMIN)
    public ApiResponse<SysFeedback> detail(@Valid @ModelAttribute IdParam param) {
        return ApiResponse.ok(feedbackService.detail(param.getId()));
    }

    /** 管理端更新反馈状态或回复。 */
    @Operation(summary = "管理端更新反馈状态或回复。")
    @PostMapping("/v1/admin/sys/feedbacks/update")
    @SaCheckPermission(value = "sys:feedback:update", type = StpKit.TYPE_ADMIN)
    @OperationAudit(resourceType = "sys_feedback", action = "update")
    public ApiResponse<Void> update(@Valid @RequestBody SysFeedbackEditParam param) {
        feedbackService.update(param);
        return ApiResponse.ok();
    }

    /** 管理端批量删除反馈。 */
    @Operation(summary = "管理端批量删除反馈。")
    @PostMapping("/v1/admin/sys/feedbacks/delete")
    @SaCheckPermission(value = "sys:feedback:delete", type = StpKit.TYPE_ADMIN)
    @OperationAudit(resourceType = "sys_feedback", action = "delete")
    public ApiResponse<Void> delete(@Valid @RequestBody IdsParam param) {
        feedbackService.delete(param);
        return ApiResponse.ok();
    }
}