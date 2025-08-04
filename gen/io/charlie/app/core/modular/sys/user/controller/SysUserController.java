package io.charlie.app.core.modular.sys.user.controller;

import io.charlie.galaxy.result.Result;
import io.charlie.app.core.modular.sys.user.param.SysUserPageParam;
import io.charlie.app.core.modular.sys.user.param.SysUserAddParam;
import io.charlie.app.core.modular.sys.user.param.SysUserEditParam;
import io.charlie.app.core.modular.sys.user.param.SysUserIdParam;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import io.charlie.app.core.modular.sys.user.service.SysUserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

/**
* @author Charlie Zhang
* @version v1.0
* @date 2025-07-27
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
    //@SaCheckPermission("/sys/user/page")
    @GetMapping("/sys/user/page")
    public Result<?> page(@ParameterObject SysUserPageParam sysUserPageParam) {
        return Result.success(sysUserService.page(sysUserPageParam));
    }

    @Operation(summary = "添加用户")
    //@SaCheckPermission("/sys/user/add")
    @PostMapping("/sys/user/add")
    public Result<?> add(@RequestBody @Valid SysUserAddParam sysUserAddParam) {
        sysUserService.add(sysUserAddParam);
        return Result.success();
    }

    @Operation(summary = "编辑用户")
    //@SaCheckPermission("/sys/user/edit")
    @PostMapping("/sys/user/edit")
    public Result<?> edit(@RequestBody @Valid SysUserEditParam sysUserEditParam) {
        sysUserService.edit(sysUserEditParam);
        return Result.success();
    }

    @Operation(summary = "删除用户")
    //@SaCheckPermission("/sys/user/delete")
    @PostMapping("/sys/user/delete")
    public Result<?> delete(@RequestBody @Valid @NotEmpty(message = "集合不能为空") List<SysUserIdParam> sysUserIdParam) {
        sysUserService.delete(sysUserIdParam);
        return Result.success();
    }

    @Operation(summary = "获取用户详情")
    //@SaCheckPermission("/sys/user/detail")
    @GetMapping("/sys/user/detail")
    public Result<?> detail(@ParameterObject @Valid SysUserIdParam sysUserIdParam) {
        return Result.success(sysUserService.detail(sysUserIdParam));
    }
}