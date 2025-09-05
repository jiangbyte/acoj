package io.charlie.app.core.modular.set.similarity.controller;

import io.charlie.galaxy.result.Result;
import io.charlie.app.core.modular.set.similarity.param.ProSetSimilarityDetailPageParam;
import io.charlie.app.core.modular.set.similarity.param.ProSetSimilarityDetailAddParam;
import io.charlie.app.core.modular.set.similarity.param.ProSetSimilarityDetailEditParam;
import io.charlie.app.core.modular.set.similarity.param.ProSetSimilarityDetailIdParam;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import io.charlie.app.core.modular.set.similarity.service.ProSetSimilarityDetailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

/**
* @author Charlie Zhang
* @version v1.0
* @date 2025-09-05
* @description 题集题目检测结果任务库 控制器
*/
@Tag(name = "题集题目检测结果任务库控制器")
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/api/v1")
@RestController
@Validated
public class ProSetSimilarityDetailController {
    private final ProSetSimilarityDetailService proSetSimilarityDetailService;

    @Operation(summary = "获取题集题目检测结果任务库分页")
    //@SaCheckPermission("/pro/set/similarity/detail/page")
    @GetMapping("/pro/set/similarity/detail/page")
    public Result<?> page(@ParameterObject ProSetSimilarityDetailPageParam proSetSimilarityDetailPageParam) {
        return Result.success(proSetSimilarityDetailService.page(proSetSimilarityDetailPageParam));
    }

    @Operation(summary = "添加题集题目检测结果任务库")
    //@SaCheckPermission("/pro/set/similarity/detail/add")
    @PostMapping("/pro/set/similarity/detail/add")
    public Result<?> add(@RequestBody @Valid ProSetSimilarityDetailAddParam proSetSimilarityDetailAddParam) {
        proSetSimilarityDetailService.add(proSetSimilarityDetailAddParam);
        return Result.success();
    }

    @Operation(summary = "编辑题集题目检测结果任务库")
    //@SaCheckPermission("/pro/set/similarity/detail/edit")
    @PostMapping("/pro/set/similarity/detail/edit")
    public Result<?> edit(@RequestBody @Valid ProSetSimilarityDetailEditParam proSetSimilarityDetailEditParam) {
        proSetSimilarityDetailService.edit(proSetSimilarityDetailEditParam);
        return Result.success();
    }

    @Operation(summary = "删除题集题目检测结果任务库")
    //@SaCheckPermission("/pro/set/similarity/detail/delete")
    @PostMapping("/pro/set/similarity/detail/delete")
    public Result<?> delete(@RequestBody @Valid @NotEmpty(message = "集合不能为空") List<ProSetSimilarityDetailIdParam> proSetSimilarityDetailIdParam) {
        proSetSimilarityDetailService.delete(proSetSimilarityDetailIdParam);
        return Result.success();
    }

    @Operation(summary = "获取题集题目检测结果任务库详情")
    //@SaCheckPermission("/pro/set/similarity/detail/detail")
    @GetMapping("/pro/set/similarity/detail/detail")
    public Result<?> detail(@ParameterObject @Valid ProSetSimilarityDetailIdParam proSetSimilarityDetailIdParam) {
        return Result.success(proSetSimilarityDetailService.detail(proSetSimilarityDetailIdParam));
    }
}