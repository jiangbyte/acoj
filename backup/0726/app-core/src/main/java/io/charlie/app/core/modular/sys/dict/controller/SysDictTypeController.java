package io.charlie.app.core.modular.sys.dict.controller;

import io.charlie.app.core.modular.sys.dict.param.*;
import io.charlie.app.core.modular.sys.dict.service.SysDictTypeService;
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
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

/**
* @author Charlie Zhang
* @version v1.0
* @date 2025-07-25
* @description 字典类型表 控制器
*/
@Tag(name = "字典类型表控制器")
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/api/v1")
@RestController
@Validated
public class SysDictTypeController {
    private final SysDictTypeService sysDictTypeService;

    @Operation(summary = "获取字典类型分页")
    //@SaCheckPermission("/sys/dict/type/page")
    @GetMapping("/sys/dict/type/page")
    public Result<?> page(@ParameterObject SysDictTypePageParam sysDictTypePageParam) {
        return Result.success(sysDictTypeService.page(sysDictTypePageParam));
    }

    @Operation(summary = "添加字典类型")
    //@SaCheckPermission("/sys/dict/type/add")
    @PostMapping("/sys/dict/type/add")
    public Result<?> add(@RequestBody @Valid SysDictTypeAddParam sysDictTypeAddParam) {
        sysDictTypeService.add(sysDictTypeAddParam);
        return Result.success();
    }

    @Operation(summary = "编辑字典类型")
    //@SaCheckPermission("/sys/dict/type/edit")
    @PostMapping("/sys/dict/type/edit")
    public Result<?> edit(@RequestBody @Valid SysDictTypeEditParam sysDictTypeEditParam) {
        sysDictTypeService.edit(sysDictTypeEditParam);
        return Result.success();
    }

    @Operation(summary = "删除字典类型")
    //@SaCheckPermission("/sys/dict/type/delete")
    @PostMapping("/sys/dict/type/delete")
    public Result<?> delete(@RequestBody @Valid @NotEmpty(message = "集合不能为空") List<SysDictTypeIdParam> sysDictTypeIdParam) {
        sysDictTypeService.delete(sysDictTypeIdParam);
        return Result.success();
    }

    @Operation(summary = "获取字典类型详情")
    //@SaCheckPermission("/sys/dict/type/detail")
    @GetMapping("/sys/dict/type/detail")
    public Result<?> detail(@ParameterObject @Valid SysDictTypeIdParam sysDictTypeIdParam) {
        return Result.success(sysDictTypeService.detail(sysDictTypeIdParam));
    }

    @Operation(summary = "获取字典类型树")
    //@SaCheckPermission("/sys/dict/type/detail")
    @GetMapping("/sys/dict/type/tree/options")
    public Result<?> treeOptions(@ParameterObject @Valid SysDictTypeOptionParam sysDictTypeOptionParam) {
        return Result.success(sysDictTypeService.treeOptions(sysDictTypeOptionParam));
    }
}