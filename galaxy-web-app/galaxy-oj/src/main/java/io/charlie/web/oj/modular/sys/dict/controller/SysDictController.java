package io.charlie.web.oj.modular.sys.dict.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import io.charlie.galaxy.result.Result;
import io.charlie.web.oj.modular.sys.dict.param.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import io.charlie.web.oj.modular.sys.dict.service.SysDictService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

/**
* @author Charlie Zhang
* @version v1.0
* @date 2025-09-20
* @description 系统字典表 控制器
*/
@Tag(name = "系统字典表控制器")
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/api/v1")
@RestController
@Validated
public class SysDictController {
    private final SysDictService sysDictService;

    @Operation(summary = "获取系统字典分页")
    @SaCheckPermission("/sys/dict/page")
    @GetMapping("/sys/dict/page")
    public Result<?> page(@ParameterObject SysDictPageParam sysDictPageParam) {
        return Result.success(sysDictService.page(sysDictPageParam));
    }

    @Operation(summary = "添加系统字典")
    @SaCheckPermission("/sys/dict/add")
    @PostMapping("/sys/dict/add")
    public Result<?> add(@RequestBody @Valid SysDictAddParam sysDictAddParam) {
        sysDictService.add(sysDictAddParam);
        return Result.success();
    }

    @Operation(summary = "编辑系统字典")
    @SaCheckPermission("/sys/dict/edit")
    @PostMapping("/sys/dict/edit")
    public Result<?> edit(@RequestBody @Valid SysDictEditParam sysDictEditParam) {
        sysDictService.edit(sysDictEditParam);
        return Result.success();
    }

    @Operation(summary = "删除系统字典")
    @SaCheckPermission("/sys/dict/delete")
    @PostMapping("/sys/dict/delete")
    public Result<?> delete(@RequestBody @Valid @NotEmpty(message = "集合不能为空") List<SysDictIdParam> sysDictIdParam) {
        sysDictService.delete(sysDictIdParam);
        return Result.success();
    }

    @Operation(summary = "获取系统字典详情")
    @SaCheckPermission("/sys/dict/detail")
    @GetMapping("/sys/dict/detail")
    public Result<?> detail(@ParameterObject @Valid SysDictIdParam sysDictIdParam) {
        return Result.success(sysDictService.detail(sysDictIdParam));
    }

    @Operation(summary = "获取系统字典树")
    @GetMapping("/sys/dict/tree")
    public Result<?> tree(@ParameterObject SysDictTreeParam sysDictTreeParam) {
        return Result.success(sysDictService.treeLabelOption(sysDictTreeParam));
    }

    @Operation(summary = "获取系统字典树")
    @GetMapping("/sys/dict/list/option")
    public Result<?> listLabelOption(@ParameterObject SysDictOptionParam sysDictOptionParam) {
        return Result.success(sysDictService.listLabelOption(sysDictOptionParam));
    }

    @Operation(summary = "根据类型分组获取列表")
    @GetMapping("/sys/dict/list/group")
    public Result<?> listGroup(@RequestParam String dictType) {
        return Result.success(sysDictService.listByDictType(dictType));
    }

    @Operation(summary = "获取系统字典树")
    @GetMapping("/sys/dict/options")
    public Result<?> option(@RequestParam @NotEmpty String dictType) {
        return Result.success(sysDictService.options(dictType));
    }
}