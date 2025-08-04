package io.charlie.app.core.modular.category.controller;

import io.charlie.app.core.modular.category.param.*;
import io.charlie.app.core.modular.sys.user.param.SysUserOptionParam;
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
import io.charlie.app.core.modular.category.service.ProCategoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

/**
* @author Charlie Zhang
* @version v1.0
* @date 2025-07-25
* @description 分类表 控制器
*/
@Tag(name = "分类表控制器")
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/api/v1")
@RestController
@Validated
public class ProCategoryController {
    private final ProCategoryService proCategoryService;

    @Operation(summary = "获取分类分页")
    //@SaCheckPermission("/pro/category/page")
    @GetMapping("/pro/category/page")
    public Result<?> page(@ParameterObject ProCategoryPageParam proCategoryPageParam) {
        return Result.success(proCategoryService.page(proCategoryPageParam));
    }

    @Operation(summary = "添加分类")
    //@SaCheckPermission("/pro/category/add")
    @PostMapping("/pro/category/add")
    public Result<?> add(@RequestBody @Valid ProCategoryAddParam proCategoryAddParam) {
        proCategoryService.add(proCategoryAddParam);
        return Result.success();
    }

    @Operation(summary = "编辑分类")
    //@SaCheckPermission("/pro/category/edit")
    @PostMapping("/pro/category/edit")
    public Result<?> edit(@RequestBody @Valid ProCategoryEditParam proCategoryEditParam) {
        proCategoryService.edit(proCategoryEditParam);
        return Result.success();
    }

    @Operation(summary = "删除分类")
    //@SaCheckPermission("/pro/category/delete")
    @PostMapping("/pro/category/delete")
    public Result<?> delete(@RequestBody @Valid @NotEmpty(message = "集合不能为空") List<ProCategoryIdParam> proCategoryIdParam) {
        proCategoryService.delete(proCategoryIdParam);
        return Result.success();
    }

    @Operation(summary = "获取分类详情")
    //@SaCheckPermission("/pro/category/detail")
    @GetMapping("/pro/category/detail")
    public Result<?> detail(@ParameterObject @Valid ProCategoryIdParam proCategoryIdParam) {
        return Result.success(proCategoryService.detail(proCategoryIdParam));
    }

    @Operation(summary = "用户选项列表")
    @GetMapping("/pro/category/options")
    public Result<?> options(@ParameterObject ProCategoryOptionParam proCategoryOptionParam) {
        return Result.success(proCategoryService.options(proCategoryOptionParam));
    }
}