package io.charlie.app.core.modular.set.controller;

import io.charlie.galaxy.result.Result;
import io.charlie.app.core.modular.set.param.ProSetPageParam;
import io.charlie.app.core.modular.set.param.ProSetAddParam;
import io.charlie.app.core.modular.set.param.ProSetEditParam;
import io.charlie.app.core.modular.set.param.ProSetIdParam;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import io.charlie.app.core.modular.set.service.ProSetService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

/**
* @author Charlie Zhang
* @version v1.0
* @date 2025-07-22
* @description 题集 控制器
*/
@Tag(name = "题集控制器")
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/api/v1")
@RestController
@Validated
public class ProSetController {
    private final ProSetService proSetService;

    @Operation(summary = "获取题集分页")
    //@SaCheckPermission("/pro/set/page")
    @GetMapping("/pro/set/page")
    public Result<?> page(@ParameterObject ProSetPageParam proSetPageParam) {
        return Result.success(proSetService.page(proSetPageParam));
    }

    @Operation(summary = "添加题集")
    //@SaCheckPermission("/pro/set/add")
    @PostMapping("/pro/set/add")
    public Result<?> add(@RequestBody @Valid ProSetAddParam proSetAddParam) {
        proSetService.add(proSetAddParam);
        return Result.success();
    }

    @Operation(summary = "编辑题集")
    //@SaCheckPermission("/pro/set/edit")
    @PostMapping("/pro/set/edit")
    public Result<?> edit(@RequestBody @Valid ProSetEditParam proSetEditParam) {
        proSetService.edit(proSetEditParam);
        return Result.success();
    }

    @Operation(summary = "删除题集")
    //@SaCheckPermission("/pro/set/delete")
    @PostMapping("/pro/set/delete")
    public Result<?> delete(@RequestBody @Valid @NotEmpty(message = "集合不能为空") List<ProSetIdParam> proSetIdParam) {
        proSetService.delete(proSetIdParam);
        return Result.success();
    }

    @Operation(summary = "获取题集详情")
    //@SaCheckPermission("/pro/set/detail")
    @GetMapping("/pro/set/detail")
    public Result<?> detail(@Valid ProSetIdParam proSetIdParam) {
        return Result.success(proSetService.detail(proSetIdParam));
    }
}