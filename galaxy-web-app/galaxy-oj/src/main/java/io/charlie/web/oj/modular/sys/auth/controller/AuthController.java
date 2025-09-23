package io.charlie.web.oj.modular.sys.auth.controller;

import io.charlie.web.oj.modular.sys.auth.param.PasswordChangeParam;
import io.charlie.web.oj.modular.sys.auth.param.UsernamePasswordLoginParam;
import io.charlie.web.oj.modular.sys.auth.result.LoginUser;
import io.charlie.web.oj.modular.sys.auth.service.AuthService;
import io.charlie.web.oj.modular.sys.auth.param.UsernamePasswordEmailRegisterParam;
import io.charlie.galaxy.result.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * @author Charlie Zhang
 * @version v1.0
 * @date 01/07/2025
 * @description 登录控制
 */
@Tag(name = "登录控制器")
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/api/v1")
@RestController
@Validated
public class AuthController {
    private final AuthService authService;

    @Operation(summary = "获取验证码")
    @GetMapping("/sys/auth/captcha")
    public Result<?> captcha() {
        return Result.success(authService.captcha());
    }

    @Operation(summary = "登录")
    @PostMapping("/sys/auth/login")
    public Result<?> login(@RequestBody @Valid UsernamePasswordLoginParam usernamePasswordLoginParam) {
        return Result.success(authService.doLogin(usernamePasswordLoginParam));
    }

    @Operation(summary = "注册")
    @PostMapping("/sys/auth/register")
    public Result<?> register(@RequestBody @Valid UsernamePasswordEmailRegisterParam usernamePasswordEmailRegisterParam) {
        return Result.success(authService.doRegister(usernamePasswordEmailRegisterParam));
    }

    @Operation(summary = "退出登录")
    @PostMapping("/sys/auth/logout")
    public Result<?> logout() {
        authService.doLogout();
        return Result.success();
    }

    @Operation(summary = "获取登录用户信息")
    @GetMapping("/sys/user/profile")
    public Result<?> profile() {
        return Result.success(authService.getLoginUser());
    }

    @Operation(summary = "获取登录用户信息")
    @GetMapping("/sys/user/profile/noe")
    public Result<?> profileNoe() {
        LoginUser loginUser = authService.getLoginUser();
        loginUser.setTelephone(null);
        return Result.success(loginUser);
    }

    @Operation(summary = "修改密码")
    @PostMapping("/sys/user/password/change")
    public Result<?> changePassword(@RequestBody @Valid PasswordChangeParam passwordChangeParam) {
        authService.changePassword(passwordChangeParam);
        return Result.success();
    }
}
