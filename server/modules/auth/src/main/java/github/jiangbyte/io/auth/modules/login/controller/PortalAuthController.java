package github.jiangbyte.io.auth.modules.login.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import github.jiangbyte.io.auth.modules.login.service.AuthService;
import github.jiangbyte.io.auth.modules.login.param.CancelAccountParam;
import github.jiangbyte.io.auth.modules.login.result.AuthOptionsResult;
import github.jiangbyte.io.auth.modules.login.result.CaptchaResult;
import github.jiangbyte.io.auth.modules.login.param.ForgotPasswordByPhoneParam;
import github.jiangbyte.io.auth.modules.login.param.ForgotPasswordParam;
import github.jiangbyte.io.auth.modules.login.param.LoginParam;
import github.jiangbyte.io.auth.modules.login.result.LoginResult;
import github.jiangbyte.io.auth.modules.login.result.PasswordKeyResult;
import github.jiangbyte.io.auth.modules.login.param.RegisterParam;
import github.jiangbyte.io.auth.modules.login.result.RegisterResult;
import github.jiangbyte.io.auth.modules.login.param.ResetPasswordByPhoneParam;
import github.jiangbyte.io.auth.modules.login.param.ResetPasswordParam;
import github.jiangbyte.io.auth.modules.login.param.SendLoginCodeParam;
import github.jiangbyte.io.common.core.domain.ApiResponse;
import github.jiangbyte.io.common.core.enums.AccountType;
import github.jiangbyte.io.common.log.annotation.OperationAudit;
import github.jiangbyte.io.common.security.ratelimit.RateLimit;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import lombok.RequiredArgsConstructor;

/**
 * 门户端认证 API：登录选项、注册、登录/登出、刷新会话、找回密码与注销账号。
 *
 * Author: Charlie
 */
@Tag(name = "门户端认证 API")
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class PortalAuthController {

    private final AuthService authService;

    /** 获取门户登录页公开配置。 */
    @Operation(summary = "获取门户登录页公开配置。")
    @GetMapping("/v1/portal/public/auth-options")
    public ApiResponse<AuthOptionsResult> authOptions() {
        return ApiResponse.ok(authService.authOptions(AccountType.PORTAL));
    }

    /** 获取图形验证码。 */
    @Operation(summary = "获取图形验证码。")
    @GetMapping("/v1/portal/captcha")
    @RateLimit(key = "portal:captcha", permits = 30, windowSeconds = 60)
    public ApiResponse<CaptchaResult> captcha(@RequestParam(value = "format", defaultValue = "svg") String format) {
        return ApiResponse.ok(authService.captcha(format));
    }

    /** 获取密码传输 RSA 公钥。 */
    @Operation(summary = "获取密码传输 RSA 公钥。")
    @GetMapping("/v1/portal/password-key")
    @RateLimit(key = "portal:password-key", permits = 30, windowSeconds = 60)
    public ApiResponse<PasswordKeyResult> passwordKey() {
        return ApiResponse.ok(authService.passwordKey());
    }

    /** 发送门户登录 OTP。 */
    @Operation(summary = "发送门户登录 OTP。")
    @PostMapping("/v1/portal/send-login-code")
    @RateLimit(key = "portal:send-login-code", permits = 10, windowSeconds = 60)
    public ApiResponse<Void> sendLoginCode(@Valid @RequestBody SendLoginCodeParam request) {
        authService.sendLoginCode(request, AccountType.PORTAL);
        return ApiResponse.ok();
    }

    /** 门户登录。 */
    @Operation(summary = "门户登录。")
    @PostMapping("/v1/portal/login")
    @RateLimit(key = "portal:login", permits = 20, windowSeconds = 60)
    @OperationAudit(resourceType = "auth", action = "login")
    public ApiResponse<LoginResult> login(@Valid @RequestBody LoginParam request) {
        request.setAccountType(AccountType.PORTAL);
        return ApiResponse.ok(authService.login(request));
    }

    /** 刷新门户会话 Token。 */
    @Operation(summary = "刷新门户会话 Token。")
    @PostMapping("/v1/portal/auth/refresh")
    public ApiResponse<LoginResult> refresh() {
        return ApiResponse.ok(authService.refreshSession());
    }

    /** 门户用户注册。 */
    @Operation(summary = "门户用户注册。")
    @PostMapping("/v1/portal/register")
    @RateLimit(key = "portal:register", permits = 10, windowSeconds = 60)
    @OperationAudit(resourceType = "auth", action = "register")
    public ApiResponse<RegisterResult> register(@Valid @RequestBody RegisterParam request) {
        return ApiResponse.ok(authService.registerPortal(request));
    }

    /** 发送门户注册 OTP。 */
    @Operation(summary = "发送门户注册 OTP。")
    @PostMapping("/v1/portal/register/send-code")
    @RateLimit(key = "portal:register-send-code", permits = 10, windowSeconds = 60)
    public ApiResponse<Void> sendRegisterCode(@Valid @RequestBody SendLoginCodeParam request) {
        authService.sendRegisterCode(request);
        return ApiResponse.ok();
    }

    /** 门户登出。 */
    @Operation(summary = "门户登出。")
    @PostMapping("/v1/portal/logout")
    @OperationAudit(resourceType = "auth", action = "logout")
    public ApiResponse<Void> logout() {
        authService.logout();
        return ApiResponse.ok();
    }

    /** 门户忘记密码（发重置邮件）。 */
    @Operation(summary = "门户忘记密码（发重置邮件）。")
    @PostMapping("/v1/portal/forgot-password")
    @RateLimit(key = "portal:forgot-password", permits = 5, windowSeconds = 60)
    @OperationAudit(resourceType = "auth", action = "forgot_password")
    public ApiResponse<Void> forgotPassword(@Valid @RequestBody ForgotPasswordParam request) {
        authService.forgotPassword(request, AccountType.PORTAL);
        return ApiResponse.ok();
    }

    /** 门户忘记密码（向绑定手机发送 OTP）。 */
    @Operation(summary = "门户忘记密码（向绑定手机发送 OTP）。")
    @PostMapping("/v1/portal/forgot-password/phone")
    @RateLimit(key = "portal:forgot-password-phone", permits = 5, windowSeconds = 60)
    @OperationAudit(resourceType = "auth", action = "forgot_password_phone")
    public ApiResponse<Void> forgotPasswordByPhone(@Valid @RequestBody ForgotPasswordByPhoneParam request) {
        authService.forgotPasswordByPhone(request, AccountType.PORTAL);
        return ApiResponse.ok();
    }

    /** 门户通过令牌重置密码。 */
    @Operation(summary = "门户通过令牌重置密码。")
    @PostMapping("/v1/portal/reset-password")
    @RateLimit(key = "portal:reset-password", permits = 10, windowSeconds = 60)
    @OperationAudit(resourceType = "auth", action = "reset_password")
    public ApiResponse<Void> resetPassword(@Valid @RequestBody ResetPasswordParam request) {
        authService.resetPassword(request, AccountType.PORTAL);
        return ApiResponse.ok();
    }

    /** 门户通过手机 OTP 重置密码。 */
    @Operation(summary = "门户通过手机 OTP 重置密码。")
    @PostMapping("/v1/portal/reset-password/phone")
    @RateLimit(key = "portal:reset-password-phone", permits = 10, windowSeconds = 60)
    @OperationAudit(resourceType = "auth", action = "reset_password_phone")
    public ApiResponse<Void> resetPasswordByPhone(@Valid @RequestBody ResetPasswordByPhoneParam request) {
        authService.resetPasswordByPhone(request, AccountType.PORTAL);
        return ApiResponse.ok();
    }

    /** 注销当前门户账号。 */
    @Operation(summary = "注销当前门户账号。")
    @PostMapping("/v1/portal/cancel")
    @OperationAudit(resourceType = "auth", action = "cancel")
    public ApiResponse<Void> cancel(@RequestBody(required = false) CancelAccountParam request) {
        authService.cancelAccount(request == null ? new CancelAccountParam() : request);
        return ApiResponse.ok();
    }
}
