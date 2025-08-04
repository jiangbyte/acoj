package io.charlie.app.core.modular.set.controller;

import io.charlie.galaxy.result.Result;
import io.charlie.app.core.modular.set.param.solved.ProSetSolvedPageParam;
import io.charlie.app.core.modular.set.param.solved.ProSetSolvedAddParam;
import io.charlie.app.core.modular.set.param.solved.ProSetSolvedEditParam;
import io.charlie.app.core.modular.set.param.solved.ProSetSolvedIdParam;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import io.charlie.app.core.modular.set.service.ProSetSolvedService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

/**
* @author Charlie Zhang
* @version v1.0
* @date 2025-07-22
* @description 用户题集解决记录表 控制器
*/
@Tag(name = "用户题集解决记录表控制器")
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/api/v1")
@RestController
@Validated
public class ProSetSolvedController {
    private final ProSetSolvedService proSetSolvedService;

    @Operation(summary = "获取用户题集解决记录分页")
    //@SaCheckPermission("/pro/set/solved/page")
    @GetMapping("/pro/set/solved/page")
    public Result<?> page(@ParameterObject ProSetSolvedPageParam proSetSolvedPageParam) {
        return Result.success(proSetSolvedService.page(proSetSolvedPageParam));
    }

    @Operation(summary = "添加用户题集解决记录")
    //@SaCheckPermission("/pro/set/solved/add")
    @PostMapping("/pro/set/solved/add")
    public Result<?> add(@RequestBody @Valid ProSetSolvedAddParam proSetSolvedAddParam) {
        proSetSolvedService.add(proSetSolvedAddParam);
        return Result.success();
    }

    @Operation(summary = "编辑用户题集解决记录")
    //@SaCheckPermission("/pro/set/solved/edit")
    @PostMapping("/pro/set/solved/edit")
    public Result<?> edit(@RequestBody @Valid ProSetSolvedEditParam proSetSolvedEditParam) {
        proSetSolvedService.edit(proSetSolvedEditParam);
        return Result.success();
    }

    @Operation(summary = "删除用户题集解决记录")
    //@SaCheckPermission("/pro/set/solved/delete")
    @PostMapping("/pro/set/solved/delete")
    public Result<?> delete(@RequestBody @Valid @NotEmpty(message = "集合不能为空") List<ProSetSolvedIdParam> proSetSolvedIdParam) {
        proSetSolvedService.delete(proSetSolvedIdParam);
        return Result.success();
    }

    @Operation(summary = "获取用户题集解决记录详情")
    //@SaCheckPermission("/pro/set/solved/detail")
    @GetMapping("/pro/set/solved/detail")
    public Result<?> detail(@Valid ProSetSolvedIdParam proSetSolvedIdParam) {
        return Result.success(proSetSolvedService.detail(proSetSolvedIdParam));
    }
}