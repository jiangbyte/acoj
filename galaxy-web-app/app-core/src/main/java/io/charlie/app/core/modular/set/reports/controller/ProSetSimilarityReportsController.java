package io.charlie.app.core.modular.set.reports.controller;

import io.charlie.galaxy.result.Result;
import io.charlie.app.core.modular.set.reports.param.ProSetSimilarityReportsPageParam;
import io.charlie.app.core.modular.set.reports.param.ProSetSimilarityReportsAddParam;
import io.charlie.app.core.modular.set.reports.param.ProSetSimilarityReportsEditParam;
import io.charlie.app.core.modular.set.reports.param.ProSetSimilarityReportsIdParam;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import io.charlie.app.core.modular.set.reports.service.ProSetSimilarityReportsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

/**
* @author Charlie Zhang
* @version v1.0
* @date 2025-09-08
* @description 题库题目报告库表 控制器
*/
@Tag(name = "题库题目报告库表控制器")
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/api/v1")
@RestController
@Validated
public class ProSetSimilarityReportsController {
    private final ProSetSimilarityReportsService proSetSimilarityReportsService;

    @Operation(summary = "获取题库题目报告库分页")
    //@SaCheckPermission("/pro/set/similarity/reports/page")
    @GetMapping("/pro/set/similarity/reports/page")
    public Result<?> page(@ParameterObject ProSetSimilarityReportsPageParam proSetSimilarityReportsPageParam) {
        return Result.success(proSetSimilarityReportsService.page(proSetSimilarityReportsPageParam));
    }

    @Operation(summary = "添加题库题目报告库")
    //@SaCheckPermission("/pro/set/similarity/reports/add")
    @PostMapping("/pro/set/similarity/reports/add")
    public Result<?> add(@RequestBody @Valid ProSetSimilarityReportsAddParam proSetSimilarityReportsAddParam) {
        proSetSimilarityReportsService.add(proSetSimilarityReportsAddParam);
        return Result.success();
    }

    @Operation(summary = "编辑题库题目报告库")
    //@SaCheckPermission("/pro/set/similarity/reports/edit")
    @PostMapping("/pro/set/similarity/reports/edit")
    public Result<?> edit(@RequestBody @Valid ProSetSimilarityReportsEditParam proSetSimilarityReportsEditParam) {
        proSetSimilarityReportsService.edit(proSetSimilarityReportsEditParam);
        return Result.success();
    }

    @Operation(summary = "删除题库题目报告库")
    //@SaCheckPermission("/pro/set/similarity/reports/delete")
    @PostMapping("/pro/set/similarity/reports/delete")
    public Result<?> delete(@RequestBody @Valid @NotEmpty(message = "集合不能为空") List<ProSetSimilarityReportsIdParam> proSetSimilarityReportsIdParam) {
        proSetSimilarityReportsService.delete(proSetSimilarityReportsIdParam);
        return Result.success();
    }

    @Operation(summary = "获取题库题目报告库详情")
    //@SaCheckPermission("/pro/set/similarity/reports/detail")
    @GetMapping("/pro/set/similarity/reports/detail")
    public Result<?> detail(@ParameterObject @Valid ProSetSimilarityReportsIdParam proSetSimilarityReportsIdParam) {
        return Result.success(proSetSimilarityReportsService.detail(proSetSimilarityReportsIdParam));
    }
}