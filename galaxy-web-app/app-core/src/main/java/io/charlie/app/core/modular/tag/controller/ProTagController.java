package io.charlie.app.core.modular.tag.controller;

import io.charlie.app.core.modular.category.param.ProCategoryOptionParam;
import io.charlie.app.core.modular.tag.param.*;
import io.charlie.galaxy.result.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import io.charlie.app.core.modular.tag.service.ProTagService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

/**
* @author Charlie Zhang
* @version v1.0
* @date 2025-07-25
* @description 标签表 控制器
*/
@Tag(name = "标签表控制器")
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/api/v1")
@RestController
@Validated
public class ProTagController {
    private final ProTagService proTagService;

    @Operation(summary = "获取标签分页")
    //@SaCheckPermission("/pro/tag/page")
    @GetMapping("/pro/tag/page")
    public Result<?> page(@ParameterObject ProTagPageParam proTagPageParam) {
        return Result.success(proTagService.page(proTagPageParam));
    }

    @Operation(summary = "添加标签")
    //@SaCheckPermission("/pro/tag/add")
    @PostMapping("/pro/tag/add")
    public Result<?> add(@RequestBody @Valid ProTagAddParam proTagAddParam) {
        proTagService.add(proTagAddParam);
        return Result.success();
    }

    @Operation(summary = "编辑标签")
    //@SaCheckPermission("/pro/tag/edit")
    @PostMapping("/pro/tag/edit")
    public Result<?> edit(@RequestBody @Valid ProTagEditParam proTagEditParam) {
        proTagService.edit(proTagEditParam);
        return Result.success();
    }

    @Operation(summary = "删除标签")
    //@SaCheckPermission("/pro/tag/delete")
    @PostMapping("/pro/tag/delete")
    public Result<?> delete(@RequestBody @Valid @NotEmpty(message = "集合不能为空") List<ProTagIdParam> proTagIdParam) {
        proTagService.delete(proTagIdParam);
        return Result.success();
    }

    @Operation(summary = "获取标签详情")
    //@SaCheckPermission("/pro/tag/detail")
    @GetMapping("/pro/tag/detail")
    public Result<?> detail(@ParameterObject @Valid ProTagIdParam proTagIdParam) {
        return Result.success(proTagService.detail(proTagIdParam));
    }

    @Operation(summary = "标签选项列表")
    @GetMapping("/pro/tag/options")
    public Result<?> options(@ParameterObject ProTagOptionParam proTagOptionParam) {
        return Result.success(proTagService.options(proTagOptionParam));
    }
}