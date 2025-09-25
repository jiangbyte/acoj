package io.charlie.web.oj.modular.data.similarity.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import io.charlie.galaxy.result.Result;
import io.charlie.web.oj.modular.data.similarity.param.TaskSimilarityPageParam;
import io.charlie.web.oj.modular.data.similarity.param.TaskSimilarityAddParam;
import io.charlie.web.oj.modular.data.similarity.param.TaskSimilarityEditParam;
import io.charlie.web.oj.modular.data.similarity.param.TaskSimilarityIdParam;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import io.charlie.web.oj.modular.data.similarity.service.TaskSimilarityService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

/**
* @author Charlie Zhang
* @version v1.0
* @date 2025-09-21
* @description 检测结果任务库 控制器
*/
@Tag(name = "检测结果任务库控制器")
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/api/v1")
@RestController
@Validated
public class TaskSimilarityController {
    private final TaskSimilarityService taskSimilarityService;

    @Operation(summary = "获取检测结果任务库分页")
    @SaCheckPermission("/task/similarity/page")
    @GetMapping("/task/similarity/page")
    public Result<?> page(@ParameterObject TaskSimilarityPageParam taskSimilarityPageParam) {
        return Result.success(taskSimilarityService.page(taskSimilarityPageParam));
    }

    @Operation(summary = "添加检测结果任务库")
    @SaCheckPermission("/task/similarity/add")
    @PostMapping("/task/similarity/add")
    public Result<?> add(@RequestBody @Valid TaskSimilarityAddParam taskSimilarityAddParam) {
        taskSimilarityService.add(taskSimilarityAddParam);
        return Result.success();
    }

    @Operation(summary = "编辑检测结果任务库")
    @SaCheckPermission("/task/similarity/edit")
    @PostMapping("/task/similarity/edit")
    public Result<?> edit(@RequestBody @Valid TaskSimilarityEditParam taskSimilarityEditParam) {
        taskSimilarityService.edit(taskSimilarityEditParam);
        return Result.success();
    }

    @Operation(summary = "删除检测结果任务库")
    @SaCheckPermission("/task/similarity/delete")
    @PostMapping("/task/similarity/delete")
    public Result<?> delete(@RequestBody @Valid @NotEmpty(message = "集合不能为空") List<TaskSimilarityIdParam> taskSimilarityIdParam) {
        taskSimilarityService.delete(taskSimilarityIdParam);
        return Result.success();
    }

    @Operation(summary = "获取检测结果任务库详情")
    @SaCheckPermission("/task/similarity/detail")
    @GetMapping("/task/similarity/detail")
    public Result<?> detail(@ParameterObject @Valid TaskSimilarityIdParam taskSimilarityIdParam) {
        return Result.success(taskSimilarityService.detail(taskSimilarityIdParam));
    }
}