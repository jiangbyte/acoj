package io.charlie.web.oj.modular.sys.config.controller;

import io.charlie.galaxy.result.Result;
import io.charlie.web.oj.modular.sys.config.param.SysConfigPageParam;
import io.charlie.web.oj.modular.sys.config.param.SysConfigAddParam;
import io.charlie.web.oj.modular.sys.config.param.SysConfigEditParam;
import io.charlie.web.oj.modular.sys.config.param.SysConfigIdParam;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import io.charlie.web.oj.modular.sys.config.service.SysConfigService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

/**
* @author Charlie Zhang
* @version v1.0
* @date 2025-09-20
* @description 系统配置表 控制器
*/
@Tag(name = "系统配置表控制器")
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/api/v1")
@RestController
@Validated
public class SysConfigController {
    private final SysConfigService sysConfigService;

    @Operation(summary = "获取系统配置分页")
    //@SaCheckPermission("/sys/config/page")
    @GetMapping("/sys/config/page")
    public Result<?> page(@ParameterObject SysConfigPageParam sysConfigPageParam) {
        return Result.success(sysConfigService.page(sysConfigPageParam));
    }

    @Operation(summary = "添加系统配置")
    //@SaCheckPermission("/sys/config/add")
    @PostMapping("/sys/config/add")
    public Result<?> add(@RequestBody @Valid SysConfigAddParam sysConfigAddParam) {
        sysConfigService.add(sysConfigAddParam);
        return Result.success();
    }

    @Operation(summary = "编辑系统配置")
    //@SaCheckPermission("/sys/config/edit")
    @PostMapping("/sys/config/edit")
    public Result<?> edit(@RequestBody @Valid SysConfigEditParam sysConfigEditParam) {
        sysConfigService.edit(sysConfigEditParam);
        return Result.success();
    }

    @Operation(summary = "删除系统配置")
    //@SaCheckPermission("/sys/config/delete")
    @PostMapping("/sys/config/delete")
    public Result<?> delete(@RequestBody @Valid @NotEmpty(message = "集合不能为空") List<SysConfigIdParam> sysConfigIdParam) {
        sysConfigService.delete(sysConfigIdParam);
        return Result.success();
    }

    @Operation(summary = "获取系统配置详情")
    //@SaCheckPermission("/sys/config/detail")
    @GetMapping("/sys/config/detail")
    public Result<?> detail(@ParameterObject @Valid SysConfigIdParam sysConfigIdParam) {
        return Result.success(sysConfigService.detail(sysConfigIdParam));
    }

    @Operation(summary = "获取系统配置值")
    @GetMapping("/sys/config/code")
    public Result<?> getValueByCode(@RequestParam @NotBlank(message = "系统配置编码不能为空") String code) {
        return Result.success(sysConfigService.getValueByCode(code));
    }
}