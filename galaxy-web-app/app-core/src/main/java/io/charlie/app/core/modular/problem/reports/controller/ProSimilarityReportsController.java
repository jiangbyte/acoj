package io.charlie.app.core.modular.problem.reports.controller;

import io.charlie.app.core.modular.similarity.param.ProblemReportConfigParam;
import io.charlie.app.core.modular.similarity.service.ProblemSimilarityMessageService;
import io.charlie.galaxy.result.Result;
import io.charlie.app.core.modular.problem.reports.param.ProSimilarityReportsPageParam;
import io.charlie.app.core.modular.problem.reports.param.ProSimilarityReportsAddParam;
import io.charlie.app.core.modular.problem.reports.param.ProSimilarityReportsEditParam;
import io.charlie.app.core.modular.problem.reports.param.ProSimilarityReportsIdParam;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import io.charlie.app.core.modular.problem.reports.service.ProSimilarityReportsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

/**
* @author Charlie Zhang
* @version v1.0
* @date 2025-09-08
* @description 题目报告库表 控制器
*/
@Tag(name = "题目报告库表控制器")
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/api/v1")
@RestController
@Validated
public class ProSimilarityReportsController {
    private final ProSimilarityReportsService proSimilarityReportsService;
    private final ProblemSimilarityMessageService problemSimilarityMessageService;

    @Operation(summary = "获取题目报告库分页")
    //@SaCheckPermission("/pro/similarity/reports/page")
    @GetMapping("/pro/similarity/reports/page")
    public Result<?> page(@ParameterObject ProSimilarityReportsPageParam proSimilarityReportsPageParam) {
        return Result.success(proSimilarityReportsService.page(proSimilarityReportsPageParam));
    }

    @Operation(summary = "添加题目报告库")
    //@SaCheckPermission("/pro/similarity/reports/add")
    @PostMapping("/pro/similarity/reports/add")
    public Result<?> add(@RequestBody @Valid ProSimilarityReportsAddParam proSimilarityReportsAddParam) {
        proSimilarityReportsService.add(proSimilarityReportsAddParam);
        return Result.success();
    }

    @Operation(summary = "编辑题目报告库")
    //@SaCheckPermission("/pro/similarity/reports/edit")
    @PostMapping("/pro/similarity/reports/edit")
    public Result<?> edit(@RequestBody @Valid ProSimilarityReportsEditParam proSimilarityReportsEditParam) {
        proSimilarityReportsService.edit(proSimilarityReportsEditParam);
        return Result.success();
    }

    @Operation(summary = "删除题目报告库")
    //@SaCheckPermission("/pro/similarity/reports/delete")
    @PostMapping("/pro/similarity/reports/delete")
    public Result<?> delete(@RequestBody @Valid @NotEmpty(message = "集合不能为空") List<ProSimilarityReportsIdParam> proSimilarityReportsIdParam) {
        proSimilarityReportsService.delete(proSimilarityReportsIdParam);
        return Result.success();
    }

    @Operation(summary = "获取题目报告库详情")
    //@SaCheckPermission("/pro/similarity/reports/detail")
    @GetMapping("/pro/similarity/reports/detail")
    public Result<?> detail(@ParameterObject @Valid ProSimilarityReportsIdParam proSimilarityReportsIdParam) {
        return Result.success(proSimilarityReportsService.detail(proSimilarityReportsIdParam));
    }

    @Operation(summary = "题目报告生成")
    @PostMapping("/pro/similarity/reports/generate")
    public Result<?> generate(@RequestBody @Valid ProblemReportConfigParam problemReportConfigParam) {
        problemSimilarityMessageService.problemSimilarityReport(problemReportConfigParam);
        return Result.success();
    }
}