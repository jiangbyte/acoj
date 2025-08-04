package io.charlie.app.core.modular.set.similarity.task.controller;

import io.charlie.galaxy.result.Result;
import io.charlie.app.core.modular.set.similarity.task.param.ProSetSimilarityTaskPageParam;
import io.charlie.app.core.modular.set.similarity.task.param.ProSetSimilarityTaskAddParam;
import io.charlie.app.core.modular.set.similarity.task.param.ProSetSimilarityTaskEditParam;
import io.charlie.app.core.modular.set.similarity.task.param.ProSetSimilarityTaskIdParam;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import io.charlie.app.core.modular.set.similarity.task.service.ProSetSimilarityTaskService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

/**
* @author Charlie Zhang
* @version v1.0
* @date 2025-07-27
* @description 题单代码相似度检测任务表 控制器
*/
@Tag(name = "题单代码相似度检测任务表控制器")
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/api/v1")
@RestController
@Validated
public class ProSetSimilarityTaskController {
    private final ProSetSimilarityTaskService proSetSimilarityTaskService;

    @Operation(summary = "获取题单代码相似度检测任务分页")
    //@SaCheckPermission("/pro/set/similarity/task/page")
    @GetMapping("/pro/set/similarity/task/page")
    public Result<?> page(@ParameterObject ProSetSimilarityTaskPageParam proSetSimilarityTaskPageParam) {
        return Result.success(proSetSimilarityTaskService.page(proSetSimilarityTaskPageParam));
    }

    @Operation(summary = "添加题单代码相似度检测任务")
    //@SaCheckPermission("/pro/set/similarity/task/add")
    @PostMapping("/pro/set/similarity/task/add")
    public Result<?> add(@RequestBody @Valid ProSetSimilarityTaskAddParam proSetSimilarityTaskAddParam) {
        proSetSimilarityTaskService.add(proSetSimilarityTaskAddParam);
        return Result.success();
    }

    @Operation(summary = "编辑题单代码相似度检测任务")
    //@SaCheckPermission("/pro/set/similarity/task/edit")
    @PostMapping("/pro/set/similarity/task/edit")
    public Result<?> edit(@RequestBody @Valid ProSetSimilarityTaskEditParam proSetSimilarityTaskEditParam) {
        proSetSimilarityTaskService.edit(proSetSimilarityTaskEditParam);
        return Result.success();
    }

    @Operation(summary = "删除题单代码相似度检测任务")
    //@SaCheckPermission("/pro/set/similarity/task/delete")
    @PostMapping("/pro/set/similarity/task/delete")
    public Result<?> delete(@RequestBody @Valid @NotEmpty(message = "集合不能为空") List<ProSetSimilarityTaskIdParam> proSetSimilarityTaskIdParam) {
        proSetSimilarityTaskService.delete(proSetSimilarityTaskIdParam);
        return Result.success();
    }

    @Operation(summary = "获取题单代码相似度检测任务详情")
    //@SaCheckPermission("/pro/set/similarity/task/detail")
    @GetMapping("/pro/set/similarity/task/detail")
    public Result<?> detail(@ParameterObject @Valid ProSetSimilarityTaskIdParam proSetSimilarityTaskIdParam) {
        return Result.success(proSetSimilarityTaskService.detail(proSetSimilarityTaskIdParam));
    }
}