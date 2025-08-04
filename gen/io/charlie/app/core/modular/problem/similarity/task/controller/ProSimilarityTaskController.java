package io.charlie.app.core.modular.problem.similarity.task.controller;

import io.charlie.galaxy.result.Result;
import io.charlie.app.core.modular.problem.similarity.task.param.ProSimilarityTaskPageParam;
import io.charlie.app.core.modular.problem.similarity.task.param.ProSimilarityTaskAddParam;
import io.charlie.app.core.modular.problem.similarity.task.param.ProSimilarityTaskEditParam;
import io.charlie.app.core.modular.problem.similarity.task.param.ProSimilarityTaskIdParam;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import io.charlie.app.core.modular.problem.similarity.task.service.ProSimilarityTaskService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

/**
* @author Charlie Zhang
* @version v1.0
* @date 2025-07-27
* @description 代码相似度检测任务表 控制器
*/
@Tag(name = "代码相似度检测任务表控制器")
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/api/v1")
@RestController
@Validated
public class ProSimilarityTaskController {
    private final ProSimilarityTaskService proSimilarityTaskService;

    @Operation(summary = "获取代码相似度检测任务分页")
    //@SaCheckPermission("/pro/similarity/task/page")
    @GetMapping("/pro/similarity/task/page")
    public Result<?> page(@ParameterObject ProSimilarityTaskPageParam proSimilarityTaskPageParam) {
        return Result.success(proSimilarityTaskService.page(proSimilarityTaskPageParam));
    }

    @Operation(summary = "添加代码相似度检测任务")
    //@SaCheckPermission("/pro/similarity/task/add")
    @PostMapping("/pro/similarity/task/add")
    public Result<?> add(@RequestBody @Valid ProSimilarityTaskAddParam proSimilarityTaskAddParam) {
        proSimilarityTaskService.add(proSimilarityTaskAddParam);
        return Result.success();
    }

    @Operation(summary = "编辑代码相似度检测任务")
    //@SaCheckPermission("/pro/similarity/task/edit")
    @PostMapping("/pro/similarity/task/edit")
    public Result<?> edit(@RequestBody @Valid ProSimilarityTaskEditParam proSimilarityTaskEditParam) {
        proSimilarityTaskService.edit(proSimilarityTaskEditParam);
        return Result.success();
    }

    @Operation(summary = "删除代码相似度检测任务")
    //@SaCheckPermission("/pro/similarity/task/delete")
    @PostMapping("/pro/similarity/task/delete")
    public Result<?> delete(@RequestBody @Valid @NotEmpty(message = "集合不能为空") List<ProSimilarityTaskIdParam> proSimilarityTaskIdParam) {
        proSimilarityTaskService.delete(proSimilarityTaskIdParam);
        return Result.success();
    }

    @Operation(summary = "获取代码相似度检测任务详情")
    //@SaCheckPermission("/pro/similarity/task/detail")
    @GetMapping("/pro/similarity/task/detail")
    public Result<?> detail(@ParameterObject @Valid ProSimilarityTaskIdParam proSimilarityTaskIdParam) {
        return Result.success(proSimilarityTaskService.detail(proSimilarityTaskIdParam));
    }
}