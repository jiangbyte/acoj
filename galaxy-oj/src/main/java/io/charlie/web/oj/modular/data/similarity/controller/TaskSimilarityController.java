package io.charlie.web.oj.modular.data.similarity.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import io.charlie.galaxy.result.Result;
import io.charlie.web.oj.annotation.log.Log;
import io.charlie.web.oj.annotation.log.LogCategory;
import io.charlie.web.oj.annotation.log.LogModule;
import io.charlie.web.oj.modular.data.library.param.BatchLibraryQueryParam;
import io.charlie.web.oj.modular.data.similarity.param.*;
import io.charlie.web.oj.modular.task.similarity.dto.SimilarityProgressDto;
import io.charlie.web.oj.modular.task.similarity.service.SimilarityService;
import io.charlie.web.oj.modular.task.similarity.service.SimilarityProgressService;
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
import java.util.Objects;
import java.util.stream.Collectors;

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
    private final SimilarityService problemsSimilarityService;

    private final SimilarityProgressService progressService;

    @Operation(summary = "获取检测结果任务库分页")
    @SaCheckPermission("/task/similarity/page")
    @GetMapping("/task/similarity/page")
    public Result<?> page(@ParameterObject TaskSimilarityPageParam taskSimilarityPageParam) {
        return Result.success(taskSimilarityService.page(taskSimilarityPageParam));
    }

    @Log(category = LogCategory.OPERATION, module = LogModule.DATA)
    @Operation(summary = "添加检测结果任务库")
    @SaCheckPermission("/task/similarity/add")
    @PostMapping("/task/similarity/add")
    public Result<?> add(@RequestBody @Valid TaskSimilarityAddParam taskSimilarityAddParam) {
        taskSimilarityService.add(taskSimilarityAddParam);
        return Result.success();
    }

    @Log(category = LogCategory.OPERATION, module = LogModule.DATA)
    @Operation(summary = "编辑检测结果任务库")
    @SaCheckPermission("/task/similarity/edit")
    @PostMapping("/task/similarity/edit")
    public Result<?> edit(@RequestBody @Valid TaskSimilarityEditParam taskSimilarityEditParam) {
        taskSimilarityService.edit(taskSimilarityEditParam);
        return Result.success();
    }

    @Log(category = LogCategory.OPERATION, module = LogModule.DATA)
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

    @PostMapping("/task/similarity/batch")
    public Result<?> batch(@RequestBody @Valid BatchLibraryQueryParam batchSimilarityParam) {
        return Result.success(problemsSimilarityService.batch(batchSimilarityParam));
    }


    /**
     * 查询相似度检测进度
     */
    @GetMapping("/task/similarity/progress/{taskId}")
    public Result<?> getProgress(@PathVariable String taskId) {
        try {
            SimilarityProgressDto progress = progressService.getProgress(taskId);
            if (progress == null) {
                return Result.failure("任务不存在或已过期");
            }
            return Result.success(progress);
        } catch (Exception e) {
            log.error("查询进度失败: {}", taskId, e);
            return Result.failure("查询进度失败");
        }
    }

    /**
     * 批量查询进度
     */
    @PostMapping("/task/similarity/progress/batch")
    public Result<?> getBatchProgress(@RequestBody List<String> taskIds) {
        try {
            List<SimilarityProgressDto> progresses = taskIds.stream()
                    .map(progressService::getProgress)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());
            return Result.success(progresses);
        } catch (Exception e) {
            log.error("批量查询进度失败", e);
            return Result.failure("批量查询进度失败");
        }
    }
}