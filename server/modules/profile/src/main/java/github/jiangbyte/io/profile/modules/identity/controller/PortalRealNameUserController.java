package github.jiangbyte.io.profile.modules.identity.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import github.jiangbyte.io.common.core.domain.ApiResponse;
import github.jiangbyte.io.common.log.annotation.OperationAudit;
import github.jiangbyte.io.profile.modules.identity.param.RealNameCaseCallbackParam;
import github.jiangbyte.io.profile.modules.identity.param.RealNameCaseInitThirdPartyParam;
import github.jiangbyte.io.profile.modules.identity.param.RealNameCaseMyPageParam;
import github.jiangbyte.io.profile.modules.identity.param.RealNameCaseSubmitParam;
import github.jiangbyte.io.profile.modules.identity.result.IdentityStatusResult;
import github.jiangbyte.io.profile.modules.identity.result.RealNameCaseInitResult;
import github.jiangbyte.io.profile.modules.identity.result.RealNameCaseOptionsResult;
import github.jiangbyte.io.profile.modules.identity.result.RealNameCaseSummaryResult;
import github.jiangbyte.io.profile.modules.identity.service.ProfileIdentityService;
import github.jiangbyte.io.profile.modules.identity.service.RealNameCaseService;
import github.jiangbyte.io.common.satoken.utils.LoginHelper;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 门户端用户实名认证 API。
 *
 * Author: Charlie
 */
@Tag(name = "门户端用户实名认证 API")
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class PortalRealNameUserController {

    private final ProfileIdentityService profileIdentityService;
    private final RealNameCaseService realNameCaseService;

    @Operation(summary = "status。")
    @GetMapping("/v1/portal/profile/identity/status")
    public ApiResponse<IdentityStatusResult> identityStatus() {
        return ApiResponse.ok(profileIdentityService.getUserStatusForAccount(LoginHelper.requireUser().getAccountId()));
    }

    @Operation(summary = "查询选项。")
    @GetMapping("/v1/portal/real-name/case/options")
    public ApiResponse<RealNameCaseOptionsResult> options() {
        return ApiResponse.ok(realNameCaseService.options());
    }

    @Operation(summary = "submit。")
    @PostMapping("/v1/portal/real-name/case/submit")
    @OperationAudit(resourceType = "real_name_case", action = "submit")
    public ApiResponse<Void> submit(@Valid @RequestBody RealNameCaseSubmitParam param) {
        realNameCaseService.submit(param);
        return ApiResponse.ok();
    }

    @Operation(summary = "init third party。")
    @PostMapping("/v1/portal/real-name/case/init-third-party")
    @OperationAudit(resourceType = "real_name_case", action = "init_third_party")
    public ApiResponse<RealNameCaseInitResult> initThirdParty(@Valid @RequestBody RealNameCaseInitThirdPartyParam param) {
        return ApiResponse.ok(realNameCaseService.initThirdParty(param));
    }

    @Operation(summary = "callback。")
    @PostMapping("/v1/portal/real-name/case/callback")
    public ApiResponse<Void> callback(@Valid @RequestBody RealNameCaseCallbackParam param) {
        realNameCaseService.callback(param);
        return ApiResponse.ok();
    }

    @Operation(summary = "查询本人记录分页。")
    @GetMapping("/v1/portal/real-name/case/my-page")
    public ApiResponse<Page<RealNameCaseSummaryResult>> myPage(@Valid @ModelAttribute RealNameCaseMyPageParam param) {
        return ApiResponse.ok(realNameCaseService.myPage(param));
    }
}
