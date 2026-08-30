package github.jiangbyte.io.profile.modules.identity.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import cn.dev33.satoken.annotation.SaCheckPermission;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import github.jiangbyte.io.common.core.domain.ApiResponse;
import github.jiangbyte.io.common.core.param.IdParam;
import github.jiangbyte.io.common.log.annotation.OperationAudit;
import github.jiangbyte.io.common.satoken.StpKit;
import github.jiangbyte.io.common.satoken.utils.LoginHelper;
import github.jiangbyte.io.profile.modules.identity.param.IdentityPageParam;
import github.jiangbyte.io.profile.modules.identity.param.IdentityRevokeParam;
import github.jiangbyte.io.profile.modules.identity.param.RealNameCaseApproveParam;
import github.jiangbyte.io.profile.modules.identity.param.RealNameCaseRejectParam;
import github.jiangbyte.io.profile.modules.identity.param.RealNameCaseReviewPageParam;
import github.jiangbyte.io.profile.modules.identity.result.IdentityPageResult;
import github.jiangbyte.io.profile.modules.identity.result.RealNameCaseDetailResult;
import github.jiangbyte.io.profile.modules.identity.result.RealNameCaseSummaryResult;
import github.jiangbyte.io.profile.modules.identity.service.ProfileIdentityService;
import github.jiangbyte.io.profile.modules.identity.service.RealNameCaseService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 管理端实名认证审核与快照管理 API。
 *
 * Author: Charlie
 */
@Tag(name = "管理端实名认证审核与快照管理 API")
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class AdminRealNameManageController {

    private final RealNameCaseService realNameCaseService;
    private final ProfileIdentityService profileIdentityService;

    @Operation(summary = "review page。")
    @GetMapping("/v1/admin/sys/real-name-case/review-page")
    @SaCheckPermission(value = "sys:realname:verify", type = StpKit.TYPE_ADMIN)
    public ApiResponse<Page<RealNameCaseSummaryResult>> reviewPage(@Valid @ModelAttribute RealNameCaseReviewPageParam param) {
        return ApiResponse.ok(realNameCaseService.reviewPage(param));
    }

    @Operation(summary = "查询详情。")
    @GetMapping("/v1/admin/sys/real-name-case/detail")
    @SaCheckPermission(value = "sys:realname:verify", type = StpKit.TYPE_ADMIN)
    public ApiResponse<RealNameCaseDetailResult> detail(@Valid @ModelAttribute IdParam param) {
        return ApiResponse.ok(realNameCaseService.detail(param.getId()));
    }

    @Operation(summary = "审核通过。")
    @PostMapping("/v1/admin/sys/real-name-case/approve")
    @SaCheckPermission(value = "sys:realname:verify", type = StpKit.TYPE_ADMIN)
    @OperationAudit(resourceType = "real_name_case", action = "approve")
    public ApiResponse<Void> approve(@Valid @RequestBody RealNameCaseApproveParam param) {
        realNameCaseService.approve(param);
        return ApiResponse.ok();
    }

    @Operation(summary = "审核驳回。")
    @PostMapping("/v1/admin/sys/real-name-case/reject")
    @SaCheckPermission(value = "sys:realname:verify", type = StpKit.TYPE_ADMIN)
    @OperationAudit(resourceType = "real_name_case", action = "reject")
    public ApiResponse<Void> reject(@Valid @RequestBody RealNameCaseRejectParam param) {
        realNameCaseService.reject(param);
        return ApiResponse.ok();
    }

    @Operation(summary = "分页查询。")
    @GetMapping("/v1/admin/sys/identity/page")
    @SaCheckPermission(value = "sys:realnameidentity:revoke", type = StpKit.TYPE_ADMIN)
    public ApiResponse<Page<IdentityPageResult>> identityPage(@Valid @ModelAttribute IdentityPageParam param) {
        return ApiResponse.ok(profileIdentityService.page(param));
    }

    @Operation(summary = "撤回。")
    @PostMapping("/v1/admin/sys/identity/revoke")
    @SaCheckPermission(value = "sys:realnameidentity:revoke", type = StpKit.TYPE_ADMIN)
    @OperationAudit(resourceType = "profile_identity", action = "revoke")
    public ApiResponse<Void> revoke(@Valid @RequestBody IdentityRevokeParam param) {
        profileIdentityService.revoke(param, LoginHelper.requireUser().getAccountId());
        return ApiResponse.ok();
    }
}
