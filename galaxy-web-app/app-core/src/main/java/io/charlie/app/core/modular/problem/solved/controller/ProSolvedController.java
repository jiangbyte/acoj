package io.charlie.app.core.modular.problem.solved.controller;

import io.charlie.galaxy.result.Result;
import io.charlie.app.core.modular.problem.solved.param.ProSolvedPageParam;
import io.charlie.app.core.modular.problem.solved.param.ProSolvedAddParam;
import io.charlie.app.core.modular.problem.solved.param.ProSolvedEditParam;
import io.charlie.app.core.modular.problem.solved.param.ProSolvedIdParam;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import io.charlie.app.core.modular.problem.solved.service.ProSolvedService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

/**
* @author Charlie Zhang
* @version v1.0
* @date 2025-07-25
* @description 用户解决表 控制器
*/
@Tag(name = "用户解决表控制器")
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/api/v1")
@RestController
@Validated
public class ProSolvedController {
    private final ProSolvedService proSolvedService;

    @Operation(summary = "获取用户解决分页")
    //@SaCheckPermission("/pro/solved/page")
    @GetMapping("/pro/solved/page")
    public Result<?> page(@ParameterObject ProSolvedPageParam proSolvedPageParam) {
        return Result.success(proSolvedService.page(proSolvedPageParam));
    }

    @Operation(summary = "添加用户解决")
    //@SaCheckPermission("/pro/solved/add")
    @PostMapping("/pro/solved/add")
    public Result<?> add(@RequestBody @Valid ProSolvedAddParam proSolvedAddParam) {
        proSolvedService.add(proSolvedAddParam);
        return Result.success();
    }

    @Operation(summary = "编辑用户解决")
    //@SaCheckPermission("/pro/solved/edit")
    @PostMapping("/pro/solved/edit")
    public Result<?> edit(@RequestBody @Valid ProSolvedEditParam proSolvedEditParam) {
        proSolvedService.edit(proSolvedEditParam);
        return Result.success();
    }

    @Operation(summary = "删除用户解决")
    //@SaCheckPermission("/pro/solved/delete")
    @PostMapping("/pro/solved/delete")
    public Result<?> delete(@RequestBody @Valid @NotEmpty(message = "集合不能为空") List<ProSolvedIdParam> proSolvedIdParam) {
        proSolvedService.delete(proSolvedIdParam);
        return Result.success();
    }

    @Operation(summary = "获取用户解决详情")
    //@SaCheckPermission("/pro/solved/detail")
    @GetMapping("/pro/solved/detail")
    public Result<?> detail(@ParameterObject @Valid ProSolvedIdParam proSolvedIdParam) {
        return Result.success(proSolvedService.detail(proSolvedIdParam));
    }
}