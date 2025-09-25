package io.charlie.web.oj.modular.data.submit.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import io.charlie.galaxy.result.Result;
import io.charlie.web.oj.modular.data.submit.param.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import io.charlie.web.oj.modular.data.submit.service.DataSubmitService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

/**
* @author Charlie Zhang
* @version v1.0
* @date 2025-09-20
* @description 提交表 控制器
*/
@Tag(name = "提交表控制器")
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/api/v1")
@RestController
@Validated
public class DataSubmitController {
    private final DataSubmitService dataSubmitService;

    @Operation(summary = "获取提交分页")
    @SaCheckPermission("/data/submit/page")
    @GetMapping("/data/submit/page")
    public Result<?> page(@ParameterObject DataSubmitPageParam dataSubmitPageParam) {
        return Result.success(dataSubmitService.page(dataSubmitPageParam));
    }

    @Operation(summary = "获取题目提交分页")
//    @SaCheckPermission("/data/submit/problem/page")
    @GetMapping("/data/submit/problem/page")
    public Result<?> problemPage(@ParameterObject DataSubmitPageParam dataSubmitPageParam) {
        return Result.success(dataSubmitService.problemPage(dataSubmitPageParam));
    }

    @Operation(summary = "获取题集提交分页")
//    @SaCheckPermission("/data/submit/set/page")
    @GetMapping("/data/submit/set/page")
    public Result<?> setPage(@ParameterObject DataSubmitPageParam dataSubmitPageParam) {
        return Result.success(dataSubmitService.setPage(dataSubmitPageParam));
    }

    @Operation(summary = "添加提交")
    @SaCheckPermission("/data/submit/add")
    @PostMapping("/data/submit/add")
    public Result<?> add(@RequestBody @Valid DataSubmitAddParam dataSubmitAddParam) {
        dataSubmitService.add(dataSubmitAddParam);
        return Result.success();
    }

    @Operation(summary = "编辑提交")
    @SaCheckPermission("/data/submit/edit")
    @PostMapping("/data/submit/edit")
    public Result<?> edit(@RequestBody @Valid DataSubmitEditParam dataSubmitEditParam) {
        dataSubmitService.edit(dataSubmitEditParam);
        return Result.success();
    }

    @Operation(summary = "删除提交")
    @SaCheckPermission("/data/submit/delete")
    @PostMapping("/data/submit/delete")
    public Result<?> delete(@RequestBody @Valid @NotEmpty(message = "集合不能为空") List<DataSubmitIdParam> dataSubmitIdParam) {
        dataSubmitService.delete(dataSubmitIdParam);
        return Result.success();
    }

    @Operation(summary = "获取提交详情")
    @SaCheckPermission("/data/submit/detail")
    @GetMapping("/data/submit/detail")
    public Result<?> detail(@ParameterObject @Valid DataSubmitIdParam dataSubmitIdParam) {
        return Result.success(dataSubmitService.detail(dataSubmitIdParam));
    }

    @Operation(summary = "执行题集提交")
    @PostMapping("/data/submit/set/execute")
    public Result<?> setExecute(@RequestBody @Valid DataSubmitExeParam dataSubmitExeParam) {
        return Result.success(dataSubmitService.handleSetSubmit(dataSubmitExeParam));
    }

    @Operation(summary = "执行题目提交")
    @PostMapping("/data/submit/execute")
    public Result<?> execute(@RequestBody @Valid DataSubmitExeParam dataSubmitExeParam) {
        return Result.success(dataSubmitService.handleProblemSubmit(dataSubmitExeParam));
    }

    @Operation(summary = "获取状态统计")
    @GetMapping("/data/submit/status/count")
    public Result<?> problemUserSubmitStatusCount() {
        return Result.success(dataSubmitService.countStatusStatistics());
    }

}