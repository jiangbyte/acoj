package io.charlie.app.core.modular.sys.dict.controller;

import io.charlie.app.core.modular.sys.dict.param.SysDictDataEditParam;
import io.charlie.app.core.modular.sys.dict.param.SysDictDataIdParam;
import io.charlie.app.core.modular.sys.dict.param.SysDictDataPageParam;
import io.charlie.app.core.modular.sys.dict.service.SysDictDataService;
import io.charlie.galaxy.result.Result;
import io.charlie.app.core.modular.sys.dict.param.SysDictDataAddParam;
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
 * @description 字典数据表 控制器
 */
@Tag(name = "字典数据表控制器")
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/api/v1")
@RestController
@Validated
public class SysDictDataController {
    private final SysDictDataService sysDictDataService;

    @Operation(summary = "获取字典数据分页")
    //@SaCheckPermission("/sys/dict/data/page")
    @GetMapping("/sys/dict/data/page")
    public Result<?> page(@ParameterObject SysDictDataPageParam sysDictDataPageParam) {
        return Result.success(sysDictDataService.page(sysDictDataPageParam));
    }

    @Operation(summary = "添加字典数据")
    //@SaCheckPermission("/sys/dict/data/add")
    @PostMapping("/sys/dict/data/add")
    public Result<?> add(@RequestBody @Valid SysDictDataAddParam sysDictDataAddParam) {
        sysDictDataService.add(sysDictDataAddParam);
        return Result.success();
    }

    @Operation(summary = "编辑字典数据")
    //@SaCheckPermission("/sys/dict/data/edit")
    @PostMapping("/sys/dict/data/edit")
    public Result<?> edit(@RequestBody @Valid SysDictDataEditParam sysDictDataEditParam) {
        sysDictDataService.edit(sysDictDataEditParam);
        return Result.success();
    }

    @Operation(summary = "删除字典数据")
    //@SaCheckPermission("/sys/dict/data/delete")
    @PostMapping("/sys/dict/data/delete")
    public Result<?> delete(@RequestBody @Valid @NotEmpty(message = "集合不能为空") List<SysDictDataIdParam> sysDictDataIdParam) {
        sysDictDataService.delete(sysDictDataIdParam);
        return Result.success();
    }

    @Operation(summary = "获取字典数据详情")
    //@SaCheckPermission("/sys/dict/data/detail")
    @GetMapping("/sys/dict/data/detail")
    public Result<?> detail(@ParameterObject @Valid SysDictDataIdParam sysDictDataIdParam) {
        return Result.success(sysDictDataService.detail(sysDictDataIdParam));
    }

    @Operation(summary = "获取字典列表详情")
    //@SaCheckPermission("/sys/dict/data/code")
    @GetMapping("/sys/dict/data/list/id")
    public Result<?> listById(@RequestParam @NotEmpty String id) {
        return Result.success(sysDictDataService.listById(id));
    }

    @Operation(summary = "获取字典列表详情")
    //@SaCheckPermission("/sys/dict/data/code")
    @GetMapping("/sys/dict/data/list/code")
    public Result<?> listByCode(@RequestParam @NotEmpty String typeCode) {
        return Result.success(sysDictDataService.listByCode(typeCode));
    }

}