package io.charlie.app.core.modular.problem.similarity.result.controller;

import io.charlie.galaxy.result.Result;
import io.charlie.app.core.modular.problem.similarity.result.param.ProSimilarityResultPageParam;
import io.charlie.app.core.modular.problem.similarity.result.param.ProSimilarityResultAddParam;
import io.charlie.app.core.modular.problem.similarity.result.param.ProSimilarityResultEditParam;
import io.charlie.app.core.modular.problem.similarity.result.param.ProSimilarityResultIdParam;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import io.charlie.app.core.modular.problem.similarity.result.service.ProSimilarityResultService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

/**
* @author Charlie Zhang
* @version v1.0
* @date 2025-07-25
* @description 代码相似度检测结果详情表 控制器
*/
@Tag(name = "代码相似度检测结果详情表控制器")
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/api/v1")
@RestController
@Validated
public class ProSimilarityResultController {
    private final ProSimilarityResultService proSimilarityResultService;

    @Operation(summary = "获取代码相似度检测结果详情分页")
    //@SaCheckPermission("/pro/similarity/result/page")
    @GetMapping("/pro/similarity/result/page")
    public Result<?> page(@ParameterObject ProSimilarityResultPageParam proSimilarityResultPageParam) {
        return Result.success(proSimilarityResultService.page(proSimilarityResultPageParam));
    }

    @Operation(summary = "添加代码相似度检测结果详情")
    //@SaCheckPermission("/pro/similarity/result/add")
    @PostMapping("/pro/similarity/result/add")
    public Result<?> add(@RequestBody @Valid ProSimilarityResultAddParam proSimilarityResultAddParam) {
        proSimilarityResultService.add(proSimilarityResultAddParam);
        return Result.success();
    }

    @Operation(summary = "编辑代码相似度检测结果详情")
    //@SaCheckPermission("/pro/similarity/result/edit")
    @PostMapping("/pro/similarity/result/edit")
    public Result<?> edit(@RequestBody @Valid ProSimilarityResultEditParam proSimilarityResultEditParam) {
        proSimilarityResultService.edit(proSimilarityResultEditParam);
        return Result.success();
    }

    @Operation(summary = "删除代码相似度检测结果详情")
    //@SaCheckPermission("/pro/similarity/result/delete")
    @PostMapping("/pro/similarity/result/delete")
    public Result<?> delete(@RequestBody @Valid @NotEmpty(message = "集合不能为空") List<ProSimilarityResultIdParam> proSimilarityResultIdParam) {
        proSimilarityResultService.delete(proSimilarityResultIdParam);
        return Result.success();
    }

    @Operation(summary = "获取代码相似度检测结果详情详情")
    //@SaCheckPermission("/pro/similarity/result/detail")
    @GetMapping("/pro/similarity/result/detail")
    public Result<?> detail(@ParameterObject @Valid ProSimilarityResultIdParam proSimilarityResultIdParam) {
        return Result.success(proSimilarityResultService.detail(proSimilarityResultIdParam));
    }
}