package io.charlie.app.core.modular.problem.similarity.controller;

import io.charlie.galaxy.result.Result;
import io.charlie.app.core.modular.problem.similarity.param.ProSimilarityDetailPageParam;
import io.charlie.app.core.modular.problem.similarity.param.ProSimilarityDetailAddParam;
import io.charlie.app.core.modular.problem.similarity.param.ProSimilarityDetailEditParam;
import io.charlie.app.core.modular.problem.similarity.param.ProSimilarityDetailIdParam;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import io.charlie.app.core.modular.problem.similarity.service.ProSimilarityDetailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

/**
* @author Charlie Zhang
* @version v1.0
* @date 2025-09-05
* @description 题目检测结果任务库 控制器
*/
@Tag(name = "题目检测结果任务库控制器")
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/api/v1")
@RestController
@Validated
public class ProSimilarityDetailController {
    private final ProSimilarityDetailService proSimilarityDetailService;

    @Operation(summary = "获取题目检测结果任务库分页")
    //@SaCheckPermission("/pro/similarity/detail/page")
    @GetMapping("/pro/similarity/detail/page")
    public Result<?> page(@ParameterObject ProSimilarityDetailPageParam proSimilarityDetailPageParam) {
        return Result.success(proSimilarityDetailService.page(proSimilarityDetailPageParam));
    }

    @Operation(summary = "添加题目检测结果任务库")
    //@SaCheckPermission("/pro/similarity/detail/add")
    @PostMapping("/pro/similarity/detail/add")
    public Result<?> add(@RequestBody @Valid ProSimilarityDetailAddParam proSimilarityDetailAddParam) {
        proSimilarityDetailService.add(proSimilarityDetailAddParam);
        return Result.success();
    }

    @Operation(summary = "编辑题目检测结果任务库")
    //@SaCheckPermission("/pro/similarity/detail/edit")
    @PostMapping("/pro/similarity/detail/edit")
    public Result<?> edit(@RequestBody @Valid ProSimilarityDetailEditParam proSimilarityDetailEditParam) {
        proSimilarityDetailService.edit(proSimilarityDetailEditParam);
        return Result.success();
    }

    @Operation(summary = "删除题目检测结果任务库")
    //@SaCheckPermission("/pro/similarity/detail/delete")
    @PostMapping("/pro/similarity/detail/delete")
    public Result<?> delete(@RequestBody @Valid @NotEmpty(message = "集合不能为空") List<ProSimilarityDetailIdParam> proSimilarityDetailIdParam) {
        proSimilarityDetailService.delete(proSimilarityDetailIdParam);
        return Result.success();
    }

    @Operation(summary = "获取题目检测结果任务库详情")
    //@SaCheckPermission("/pro/similarity/detail/detail")
    @GetMapping("/pro/similarity/detail/detail")
    public Result<?> detail(@ParameterObject @Valid ProSimilarityDetailIdParam proSimilarityDetailIdParam) {
        return Result.success(proSimilarityDetailService.detail(proSimilarityDetailIdParam));
    }
}