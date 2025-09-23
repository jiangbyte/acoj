package io.charlie.web.oj.modular.data.reports.controller;

import io.charlie.galaxy.result.Result;
import io.charlie.web.oj.modular.data.reports.param.DataReportsPageParam;
import io.charlie.web.oj.modular.data.reports.param.DataReportsAddParam;
import io.charlie.web.oj.modular.data.reports.param.DataReportsEditParam;
import io.charlie.web.oj.modular.data.reports.param.DataReportsIdParam;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import io.charlie.web.oj.modular.data.reports.service.DataReportsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

/**
* @author Charlie Zhang
* @version v1.0
* @date 2025-09-20
* @description 报告库表 控制器
*/
@Tag(name = "报告库表控制器")
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/api/v1")
@RestController
@Validated
public class DataReportsController {
    private final DataReportsService dataReportsService;

    @Operation(summary = "获取报告库分页")
    //@SaCheckPermission("/data/reports/page")
    @GetMapping("/data/reports/page")
    public Result<?> page(@ParameterObject DataReportsPageParam dataReportsPageParam) {
        return Result.success(dataReportsService.page(dataReportsPageParam));
    }

    @Operation(summary = "添加报告库")
    //@SaCheckPermission("/data/reports/add")
    @PostMapping("/data/reports/add")
    public Result<?> add(@RequestBody @Valid DataReportsAddParam dataReportsAddParam) {
        dataReportsService.add(dataReportsAddParam);
        return Result.success();
    }

    @Operation(summary = "编辑报告库")
    //@SaCheckPermission("/data/reports/edit")
    @PostMapping("/data/reports/edit")
    public Result<?> edit(@RequestBody @Valid DataReportsEditParam dataReportsEditParam) {
        dataReportsService.edit(dataReportsEditParam);
        return Result.success();
    }

    @Operation(summary = "删除报告库")
    //@SaCheckPermission("/data/reports/delete")
    @PostMapping("/data/reports/delete")
    public Result<?> delete(@RequestBody @Valid @NotEmpty(message = "集合不能为空") List<DataReportsIdParam> dataReportsIdParam) {
        dataReportsService.delete(dataReportsIdParam);
        return Result.success();
    }

    @Operation(summary = "获取报告库详情")
    //@SaCheckPermission("/data/reports/detail")
    @GetMapping("/data/reports/detail")
    public Result<?> detail(@ParameterObject @Valid DataReportsIdParam dataReportsIdParam) {
        return Result.success(dataReportsService.detail(dataReportsIdParam));
    }
}