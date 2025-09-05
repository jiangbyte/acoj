package io.charlie.app.core.modular.problem.sample.controller;

import io.charlie.galaxy.result.Result;
import io.charlie.app.core.modular.problem.sample.param.ProSampleLibraryPageParam;
import io.charlie.app.core.modular.problem.sample.param.ProSampleLibraryAddParam;
import io.charlie.app.core.modular.problem.sample.param.ProSampleLibraryEditParam;
import io.charlie.app.core.modular.problem.sample.param.ProSampleLibraryIdParam;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import io.charlie.app.core.modular.problem.sample.service.ProSampleLibraryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

/**
* @author Charlie Zhang
* @version v1.0
* @date 2025-09-05
* @description 题目提交样本库 控制器
*/
@Tag(name = "题目提交样本库控制器")
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/api/v1")
@RestController
@Validated
public class ProSampleLibraryController {
    private final ProSampleLibraryService proSampleLibraryService;

    @Operation(summary = "获取题目提交样本库分页")
    //@SaCheckPermission("/pro/sample/library/page")
    @GetMapping("/pro/sample/library/page")
    public Result<?> page(@ParameterObject ProSampleLibraryPageParam proSampleLibraryPageParam) {
        return Result.success(proSampleLibraryService.page(proSampleLibraryPageParam));
    }

    @Operation(summary = "添加题目提交样本库")
    //@SaCheckPermission("/pro/sample/library/add")
    @PostMapping("/pro/sample/library/add")
    public Result<?> add(@RequestBody @Valid ProSampleLibraryAddParam proSampleLibraryAddParam) {
        proSampleLibraryService.add(proSampleLibraryAddParam);
        return Result.success();
    }

    @Operation(summary = "编辑题目提交样本库")
    //@SaCheckPermission("/pro/sample/library/edit")
    @PostMapping("/pro/sample/library/edit")
    public Result<?> edit(@RequestBody @Valid ProSampleLibraryEditParam proSampleLibraryEditParam) {
        proSampleLibraryService.edit(proSampleLibraryEditParam);
        return Result.success();
    }

    @Operation(summary = "删除题目提交样本库")
    //@SaCheckPermission("/pro/sample/library/delete")
    @PostMapping("/pro/sample/library/delete")
    public Result<?> delete(@RequestBody @Valid @NotEmpty(message = "集合不能为空") List<ProSampleLibraryIdParam> proSampleLibraryIdParam) {
        proSampleLibraryService.delete(proSampleLibraryIdParam);
        return Result.success();
    }

    @Operation(summary = "获取题目提交样本库详情")
    //@SaCheckPermission("/pro/sample/library/detail")
    @GetMapping("/pro/sample/library/detail")
    public Result<?> detail(@ParameterObject @Valid ProSampleLibraryIdParam proSampleLibraryIdParam) {
        return Result.success(proSampleLibraryService.detail(proSampleLibraryIdParam));
    }
}