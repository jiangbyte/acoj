package io.charlie.web.oj.modular.sys.log.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import io.charlie.galaxy.result.Result;
import io.charlie.web.oj.modular.sys.log.param.SysLogPageParam;
import io.charlie.web.oj.modular.sys.log.param.SysLogAddParam;
import io.charlie.web.oj.modular.sys.log.param.SysLogEditParam;
import io.charlie.web.oj.modular.sys.log.param.SysLogIdParam;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import io.charlie.web.oj.modular.sys.log.service.SysLogService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

/**
* @author Charlie Zhang
* @version v1.0
* @date 2025-10-17
* @description 系统活动/日志记录表 控制器
*/
@Tag(name = "系统活动/日志记录表控制器")
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/api/v1")
@RestController
@Validated
public class SysLogController {
    private final SysLogService sysLogService;

    @Operation(summary = "获取系统活动/日志记录分页")
    @SaCheckPermission("/sys/log/page")
    @GetMapping("/sys/log/page")
    public Result<?> page(@ParameterObject SysLogPageParam sysLogPageParam) {
        return Result.success(sysLogService.page(sysLogPageParam));
    }

    @Operation(summary = "添加系统活动/日志记录")
    @SaCheckPermission("/sys/log/add")
    @PostMapping("/sys/log/add")
    public Result<?> add(@RequestBody @Valid SysLogAddParam sysLogAddParam) {
        sysLogService.add(sysLogAddParam);
        return Result.success();
    }

    @Operation(summary = "编辑系统活动/日志记录")
    @SaCheckPermission("/sys/log/edit")
    @PostMapping("/sys/log/edit")
    public Result<?> edit(@RequestBody @Valid SysLogEditParam sysLogEditParam) {
        sysLogService.edit(sysLogEditParam);
        return Result.success();
    }

    @Operation(summary = "删除系统活动/日志记录")
    @SaCheckPermission("/sys/log/delete")
    @PostMapping("/sys/log/delete")
    public Result<?> delete(@RequestBody @Valid @NotEmpty(message = "集合不能为空") List<SysLogIdParam> sysLogIdParam) {
        sysLogService.delete(sysLogIdParam);
        return Result.success();
    }

    @Operation(summary = "获取系统活动/日志记录详情")
    @SaCheckPermission("/sys/log/detail")
    @GetMapping("/sys/log/detail")
    public Result<?> detail(@ParameterObject @Valid SysLogIdParam sysLogIdParam) {
        return Result.success(sysLogService.detail(sysLogIdParam));
    }

    @Operation(summary = "获取系统活动/日志记录最近")
    @GetMapping("/sys/log/recent")
    public Result<?> recent() {
        return Result.success(sysLogService.recent(4));
    }
}