package io.charlie.web.oj.modular.sys.user.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import io.charlie.galaxy.result.Result;
import io.charlie.web.oj.annotation.log.Log;
import io.charlie.web.oj.annotation.log.LogCategory;
import io.charlie.web.oj.annotation.log.LogModule;
import io.charlie.web.oj.modular.sys.user.param.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import io.charlie.web.oj.modular.sys.user.service.SysUserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

/**
 * @author Charlie Zhang
 * @version v1.0
 * @date 2025-09-20
 * @description 用户表 控制器
 */
@Tag(name = "用户表控制器")
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/api/v1")
@RestController
@Validated
public class SysUserController {
    private final SysUserService sysUserService;

    @Operation(summary = "获取用户分页")
    @SaCheckPermission("/sys/user/page")
    @GetMapping("/sys/user/page")
    public Result<?> page(@ParameterObject SysUserPageParam sysUserPageParam) {
        return Result.success(sysUserService.page(sysUserPageParam));
    }

    @Log(category = LogCategory.OPERATION, module = LogModule.SYS)
    @Operation(summary = "添加用户")
    @SaCheckPermission("/sys/user/add")
    @PostMapping("/sys/user/add")
    public Result<?> add(@RequestBody @Valid SysUserAddParam sysUserAddParam) {
        sysUserService.add(sysUserAddParam);
        return Result.success();
    }

    @Log(category = LogCategory.OPERATION, module = LogModule.SYS)
    @Operation(summary = "编辑用户")
    @SaCheckPermission("/sys/user/edit")
    @PostMapping("/sys/user/edit")
    public Result<?> edit(@RequestBody @Valid SysUserEditParam sysUserEditParam) {
        sysUserService.edit(sysUserEditParam);
        return Result.success();
    }

    @Log(category = LogCategory.OPERATION, module = LogModule.SYS)
    @Operation(summary = "删除用户")
    @SaCheckPermission("/sys/user/delete")
    @PostMapping("/sys/user/delete")
    public Result<?> delete(@RequestBody @Valid @NotEmpty(message = "集合不能为空") List<SysUserIdParam> sysUserIdParam) {
        sysUserService.delete(sysUserIdParam);
        return Result.success();
    }

    @Operation(summary = "获取用户详情")
    @SaCheckPermission("/sys/user/detail")
    @GetMapping("/sys/user/detail")
    public Result<?> detail(@ParameterObject @Valid SysUserIdParam sysUserIdParam) {
        return Result.success(sysUserService.detail(sysUserIdParam));
    }

    @Operation(summary = "用户选项列表")
    @GetMapping("/sys/user/options")
    public Result<?> options(@ParameterObject SysUserOptionParam sysUserOptionParam) {
        return Result.success(sysUserService.options(sysUserOptionParam));
    }

    @Operation(summary = "C端-获取用户详情")
    @GetMapping("/sys/user/detail/client")
    public Result<?> detailC(@ParameterObject @Valid SysUserIdParam sysUserIdParam) {
        return Result.success(sysUserService.appDetail(sysUserIdParam));
    }

    @Operation(summary = "C端-更新用户")
    @PostMapping("/sys/user/update/profile")
    public Result<?> updateProfile(@RequestBody @Valid SysUserUpdateAppParam sysUserUpdateAppParam) {
        sysUserService.updateApp(sysUserUpdateAppParam);
        return Result.success();
    }

    @Operation(summary = "C端-更新头像")
    @PostMapping("/sys/user/update/avatar")
    public Result<?> updateAvatar(@RequestBody @Valid SysUserUpdateImageParam sysUserUpdateImageParam) {
        return Result.success(sysUserService.updateAvatar(sysUserUpdateImageParam));
    }

    @Operation(summary = "C端-更新背景")
    @PostMapping("/sys/user/update/background")
    public Result<?> updateBackground(@RequestBody @Valid SysUserUpdateImageParam sysUserUpdateImageParam) {
        return Result.success(sysUserService.updateBackground(sysUserUpdateImageParam));
    }

    @Operation(summary = "C端-修改密码")
    @PostMapping("/sys/user/update/password")
    public Result<?> updatePassword(@RequestBody @Valid SysUserPasswordUpdateParam sysUserPasswordUpdateParam) {
        return Result.success(sysUserService.updatePassword(sysUserPasswordUpdateParam));
    }


}