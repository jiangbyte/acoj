package io.charlie.web.oj.modular.sys.category.controller;

import io.charlie.galaxy.result.Result;
import io.charlie.web.oj.modular.sys.category.param.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import io.charlie.web.oj.modular.sys.category.service.SysCategoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

/**
* @author Charlie Zhang
* @version v1.0
* @date 2025-09-20
* @description 分类表 控制器
*/
@Tag(name = "分类表控制器")
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/api/v1")
@RestController
@Validated
public class SysCategoryController {
    private final SysCategoryService sysCategoryService;

    @Operation(summary = "获取分类分页")
    //@SaCheckPermission("/sys/category/page")
    @GetMapping("/sys/category/page")
    public Result<?> page(@ParameterObject SysCategoryPageParam sysCategoryPageParam) {
        return Result.success(sysCategoryService.page(sysCategoryPageParam));
    }

    @Operation(summary = "添加分类")
    //@SaCheckPermission("/sys/category/add")
    @PostMapping("/sys/category/add")
    public Result<?> add(@RequestBody @Valid SysCategoryAddParam sysCategoryAddParam) {
        sysCategoryService.add(sysCategoryAddParam);
        return Result.success();
    }

    @Operation(summary = "编辑分类")
    //@SaCheckPermission("/sys/category/edit")
    @PostMapping("/sys/category/edit")
    public Result<?> edit(@RequestBody @Valid SysCategoryEditParam sysCategoryEditParam) {
        sysCategoryService.edit(sysCategoryEditParam);
        return Result.success();
    }

    @Operation(summary = "删除分类")
    //@SaCheckPermission("/sys/category/delete")
    @PostMapping("/sys/category/delete")
    public Result<?> delete(@RequestBody @Valid @NotEmpty(message = "集合不能为空") List<SysCategoryIdParam> sysCategoryIdParam) {
        sysCategoryService.delete(sysCategoryIdParam);
        return Result.success();
    }

    @Operation(summary = "获取分类详情")
    //@SaCheckPermission("/sys/category/detail")
    @GetMapping("/sys/category/detail")
    public Result<?> detail(@ParameterObject @Valid SysCategoryIdParam sysCategoryIdParam) {
        return Result.success(sysCategoryService.detail(sysCategoryIdParam));
    }

    @Operation(summary = "用户选项列表")
    @GetMapping("/sys/category/options")
    public Result<?> options(@ParameterObject SysCategoryOptionParam sysCategoryOptionParam) {
        return Result.success(sysCategoryService.options(sysCategoryOptionParam));
    }

}