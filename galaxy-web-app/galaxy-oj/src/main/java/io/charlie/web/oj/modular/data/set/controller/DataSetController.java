package io.charlie.web.oj.modular.data.set.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import io.charlie.galaxy.result.Result;
import io.charlie.web.oj.annotation.log.Log;
import io.charlie.web.oj.annotation.log.LogCategory;
import io.charlie.web.oj.annotation.log.LogModule;
import io.charlie.web.oj.modular.data.set.param.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import io.charlie.web.oj.modular.data.set.service.DataSetService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

/**
 * @author Charlie Zhang
 * @version v1.0
 * @date 2025-09-20
 * @description 题集 控制器
 */
@Tag(name = "题集控制器")
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/api/v1")
@RestController
@Validated
public class DataSetController {
    private final DataSetService dataSetService;

    @Operation(summary = "获取题集分页")
    @SaCheckPermission("/data/set/page")
    @GetMapping("/data/set/page")
    public Result<?> page(@ParameterObject DataSetPageParam dataSetPageParam) {
        return Result.success(dataSetService.page(dataSetPageParam));
    }

    @Operation(summary = "获取题集分页")
    @GetMapping("/data/set/page/client")
    public Result<?> pageClient(@ParameterObject DataSetPageParam dataSetPageParam) {
        return Result.success(dataSetService.pageClient(dataSetPageParam));
    }

    @Log(category = LogCategory.OPERATION, module = LogModule.DATA)
    @Operation(summary = "添加题集")
    @SaCheckPermission("/data/set/add")
    @PostMapping("/data/set/add")
    public Result<?> add(@RequestBody @Valid DataSetAddParam dataSetAddParam) {
        dataSetService.add(dataSetAddParam);
        return Result.success();
    }

    @Log(category = LogCategory.OPERATION, module = LogModule.DATA)
    @Operation(summary = "编辑题集")
    @SaCheckPermission("/data/set/edit")
    @PostMapping("/data/set/edit")
    public Result<?> edit(@RequestBody @Valid DataSetEditParam dataSetEditParam) {
        dataSetService.edit(dataSetEditParam);
        return Result.success();
    }

    @Log(category = LogCategory.OPERATION, module = LogModule.DATA)
    @Operation(summary = "删除题集")
    @SaCheckPermission("/data/set/delete")
    @PostMapping("/data/set/delete")
    public Result<?> delete(@RequestBody @Valid @NotEmpty(message = "集合不能为空") List<DataSetIdParam> dataSetIdParam) {
        dataSetService.delete(dataSetIdParam);
        return Result.success();
    }

    @Operation(summary = "获取题集详情")
    @SaCheckPermission("/data/set/detail")
    @GetMapping("/data/set/detail")
    public Result<?> detail(@ParameterObject @Valid DataSetIdParam dataSetIdParam) {
        return Result.success(dataSetService.detail(dataSetIdParam));
    }

    @Operation(summary = "C端-获取最新N题集")
    @GetMapping("/data/set/latest")
    public Result<?> latest10() {
        return Result.success(dataSetService.latestN(4));
    }

    @Operation(summary = "C端-获取题集题目")
    @GetMapping("/data/set/problem")
    public Result<?> getSetProblem(@ParameterObject @Valid DataSetProblemParam dataSetProblemParam) {
        return Result.success(dataSetService.getSetProblem(dataSetProblemParam));
    }

    @Operation(summary = "C端-获取题集用户")
    @GetMapping("/data/set/user")
    public Result<?> getSetUser(@ParameterObject @Valid DataSetUserParam dataSetUserParam) {
        return Result.success(dataSetService.getSetUser(dataSetUserParam));
    }


    @Operation(summary = "C端-获取题集题目详情")
    @GetMapping("/data/set/problem/detail")
    public Result<?> getSetProblemDetail(@ParameterObject @Valid DataSetProblemDetailParam dataSetProblemDetailParam) {
        return Result.success(dataSetService.getSetProblemDetail(dataSetProblemDetailParam));
    }

    @Operation(summary = "C端-获取最热10个题集")
    @GetMapping("/data/set/hot")
    public Result<?> getHot() {
        return Result.success(dataSetService.getHotN(10));
    }

    @Operation(summary = "C端-难度分布")
    @GetMapping("/data/set/difficulty/distribution")
    public Result<?> difficultyDistribution() {
        return Result.success(dataSetService.difficultyDistribution());
    }

}