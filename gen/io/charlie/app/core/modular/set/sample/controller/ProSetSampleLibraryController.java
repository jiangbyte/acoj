package io.charlie.app.core.modular.set.sample.controller;

import io.charlie.galaxy.result.Result;
import io.charlie.app.core.modular.set.sample.param.ProSetSampleLibraryPageParam;
import io.charlie.app.core.modular.set.sample.param.ProSetSampleLibraryAddParam;
import io.charlie.app.core.modular.set.sample.param.ProSetSampleLibraryEditParam;
import io.charlie.app.core.modular.set.sample.param.ProSetSampleLibraryIdParam;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import io.charlie.app.core.modular.set.sample.service.ProSetSampleLibraryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

/**
* @author Charlie Zhang
* @version v1.0
* @date 2025-09-05
* @description 题目题集提交样本库 控制器
*/
@Tag(name = "题目题集提交样本库控制器")
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/api/v1")
@RestController
@Validated
public class ProSetSampleLibraryController {
    private final ProSetSampleLibraryService proSetSampleLibraryService;

    @Operation(summary = "获取题目题集提交样本库分页")
    //@SaCheckPermission("/pro/set/sample/library/page")
    @GetMapping("/pro/set/sample/library/page")
    public Result<?> page(@ParameterObject ProSetSampleLibraryPageParam proSetSampleLibraryPageParam) {
        return Result.success(proSetSampleLibraryService.page(proSetSampleLibraryPageParam));
    }

    @Operation(summary = "添加题目题集提交样本库")
    //@SaCheckPermission("/pro/set/sample/library/add")
    @PostMapping("/pro/set/sample/library/add")
    public Result<?> add(@RequestBody @Valid ProSetSampleLibraryAddParam proSetSampleLibraryAddParam) {
        proSetSampleLibraryService.add(proSetSampleLibraryAddParam);
        return Result.success();
    }

    @Operation(summary = "编辑题目题集提交样本库")
    //@SaCheckPermission("/pro/set/sample/library/edit")
    @PostMapping("/pro/set/sample/library/edit")
    public Result<?> edit(@RequestBody @Valid ProSetSampleLibraryEditParam proSetSampleLibraryEditParam) {
        proSetSampleLibraryService.edit(proSetSampleLibraryEditParam);
        return Result.success();
    }

    @Operation(summary = "删除题目题集提交样本库")
    //@SaCheckPermission("/pro/set/sample/library/delete")
    @PostMapping("/pro/set/sample/library/delete")
    public Result<?> delete(@RequestBody @Valid @NotEmpty(message = "集合不能为空") List<ProSetSampleLibraryIdParam> proSetSampleLibraryIdParam) {
        proSetSampleLibraryService.delete(proSetSampleLibraryIdParam);
        return Result.success();
    }

    @Operation(summary = "获取题目题集提交样本库详情")
    //@SaCheckPermission("/pro/set/sample/library/detail")
    @GetMapping("/pro/set/sample/library/detail")
    public Result<?> detail(@ParameterObject @Valid ProSetSampleLibraryIdParam proSetSampleLibraryIdParam) {
        return Result.success(proSetSampleLibraryService.detail(proSetSampleLibraryIdParam));
    }
}