package github.jiangbyte.io.auth.modules.oauth.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import cn.dev33.satoken.annotation.SaCheckPermission;
import github.jiangbyte.io.auth.modules.oauth.param.AdminOauthUnbindParam;
import github.jiangbyte.io.auth.modules.oauth.result.OauthAuthorizeResult;
import github.jiangbyte.io.auth.modules.oauth.result.OauthBindingResult;
import github.jiangbyte.io.auth.modules.oauth.service.AuthOauthService;
import github.jiangbyte.io.common.core.domain.ApiResponse;
import github.jiangbyte.io.common.core.enums.AccountType;
import github.jiangbyte.io.common.log.annotation.OperationAudit;
import github.jiangbyte.io.common.satoken.StpKit;
import github.jiangbyte.io.common.security.ratelimit.RateLimit;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.List;

/**
 * 管理端三方登录 API。
 *
 * Author: Charlie
 */
@Tag(name = "管理端三方登录 API")
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class AdminOauthController {

    private final AuthOauthService authOauthService;

    @Operation(summary = "OAuth 授权。")
    @GetMapping("/v1/admin/oauth/{provider}/authorize")
    @RateLimit(key = "admin:oauth-authorize", permits = 30, windowSeconds = 60)
    public ApiResponse<OauthAuthorizeResult> authorize(
            @PathVariable("provider") String provider,
            @RequestParam(value = "intent", required = false, defaultValue = "LOGIN") String intent,
            @RequestParam(value = "redirect", required = false) String redirect) {
        return ApiResponse.ok(authOauthService.authorize(AccountType.ADMIN, provider, intent, redirect));
    }

    @Operation(summary = "callback。")
    @GetMapping("/v1/admin/oauth/{provider}/callback")
    @RateLimit(key = "admin:oauth-callback", permits = 30, windowSeconds = 60)
    public void callback(
            @PathVariable("provider") String provider,
            @RequestParam(value = "code", required = false) String code,
            @RequestParam(value = "state", required = false) String state,
            HttpServletResponse response) throws IOException {
        String location = authOauthService.handleCallback(AccountType.ADMIN, provider, code, state);
        response.sendRedirect(location);
    }

    @Operation(summary = "OAuth 换票。")
    @PostMapping("/v1/admin/oauth/exchange")
    @RateLimit(key = "admin:oauth-exchange", permits = 30, windowSeconds = 60)
    public ApiResponse<github.jiangbyte.io.auth.modules.login.result.LoginResult> exchange(
            @Valid @RequestBody github.jiangbyte.io.auth.modules.oauth.param.OauthExchangeParam request) {
        return ApiResponse.ok(authOauthService.exchange(request.getCode()));
    }

    @Operation(summary = "bindings。")
    @GetMapping("/v1/admin/oauth/bindings")
    public ApiResponse<List<OauthBindingResult>> bindings() {
        return ApiResponse.ok(authOauthService.listCurrentBindings());
    }

    @Operation(summary = "OAuth 授权。")
    @PostMapping("/v1/admin/oauth/{provider}/bind/authorize")
    @OperationAudit(resourceType = "auth", action = "oauth_bind_authorize")
    public ApiResponse<OauthAuthorizeResult> bindAuthorize(@PathVariable("provider") String provider) {
        return ApiResponse.ok(authOauthService.bindAuthorize(AccountType.ADMIN, provider));
    }

    @Operation(summary = "解绑。")
    @PostMapping("/v1/admin/oauth/{provider}/unbind")
    @OperationAudit(resourceType = "auth", action = "oauth_unbind")
    public ApiResponse<Void> unbind(@PathVariable("provider") String provider) {
        authOauthService.unbind(provider);
        return ApiResponse.ok();
    }

    @Operation(summary = "解绑。")
    @PostMapping("/v1/admin/sys/accounts/oauth/unbind")
    @SaCheckPermission(value = "iam:account:update", type = StpKit.TYPE_ADMIN)
    @OperationAudit(resourceType = "iam_account", action = "oauth_unbind")
    public ApiResponse<Void> adminUnbind(@Valid @RequestBody AdminOauthUnbindParam request) {
        authOauthService.adminUnbind(request.getAccountId(), request.getProvider());
        return ApiResponse.ok();
    }
}
