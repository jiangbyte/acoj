package io.charlie.web.oj.modular.data.reports.controller;

import io.charlie.galaxy.result.Result;
import io.charlie.web.oj.modular.data.reports.param.TaskReportsPageParam;
import io.charlie.web.oj.modular.data.reports.param.TaskReportsAddParam;
import io.charlie.web.oj.modular.data.reports.param.TaskReportsEditParam;
import io.charlie.web.oj.modular.data.reports.param.TaskReportsIdParam;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import io.charlie.web.oj.modular.data.reports.service.TaskReportsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

/**
* @author Charlie Zhang
* @version v1.0
* @date 2025-09-21
* @description 报告库表 控制器
*/
@Tag(name = "报告库表控制器")
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/api/v1")
@RestController
@Validated
public class TaskReportsController {
    private final TaskReportsService taskReportsService;

    @Operation(summary = "获取报告库分页")
    //@SaCheckPermission("/task/reports/page")
    @GetMapping("/task/reports/page")
    public Result<?> page(@ParameterObject TaskReportsPageParam taskReportsPageParam) {
        return Result.success(taskReportsService.page(taskReportsPageParam));
    }

    @Operation(summary = "添加报告库")
    //@SaCheckPermission("/task/reports/add")
    @PostMapping("/task/reports/add")
    public Result<?> add(@RequestBody @Valid TaskReportsAddParam taskReportsAddParam) {
        taskReportsService.add(taskReportsAddParam);
        return Result.success();
    }

    @Operation(summary = "编辑报告库")
    //@SaCheckPermission("/task/reports/edit")
    @PostMapping("/task/reports/edit")
    public Result<?> edit(@RequestBody @Valid TaskReportsEditParam taskReportsEditParam) {
        taskReportsService.edit(taskReportsEditParam);
        return Result.success();
    }

    @Operation(summary = "删除报告库")
    //@SaCheckPermission("/task/reports/delete")
    @PostMapping("/task/reports/delete")
    public Result<?> delete(@RequestBody @Valid @NotEmpty(message = "集合不能为空") List<TaskReportsIdParam> taskReportsIdParam) {
        taskReportsService.delete(taskReportsIdParam);
        return Result.success();
    }

    @Operation(summary = "获取报告库详情")
    //@SaCheckPermission("/task/reports/detail")
    @GetMapping("/task/reports/detail")
    public Result<?> detail(@ParameterObject @Valid TaskReportsIdParam taskReportsIdParam) {
        return Result.success(taskReportsService.detail(taskReportsIdParam));
    }
}