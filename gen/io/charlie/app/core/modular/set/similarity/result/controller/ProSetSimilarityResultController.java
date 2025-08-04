package io.charlie.app.core.modular.set.similarity.result.controller;

import io.charlie.galaxy.result.Result;
import io.charlie.app.core.modular.set.similarity.result.param.ProSetSimilarityResultPageParam;
import io.charlie.app.core.modular.set.similarity.result.param.ProSetSimilarityResultAddParam;
import io.charlie.app.core.modular.set.similarity.result.param.ProSetSimilarityResultEditParam;
import io.charlie.app.core.modular.set.similarity.result.param.ProSetSimilarityResultIdParam;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import io.charlie.app.core.modular.set.similarity.result.service.ProSetSimilarityResultService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

/**
* @author Charlie Zhang
* @version v1.0
* @date 2025-07-27
* @description 题单代码相似度检测结果详情表 控制器
*/
@Tag(name = "题单代码相似度检测结果详情表控制器")
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/api/v1")
@RestController
@Validated
public class ProSetSimilarityResultController {
    private final ProSetSimilarityResultService proSetSimilarityResultService;

    @Operation(summary = "获取题单代码相似度检测结果详情分页")
    //@SaCheckPermission("/pro/set/similarity/result/page")
    @GetMapping("/pro/set/similarity/result/page")
    public Result<?> page(@ParameterObject ProSetSimilarityResultPageParam proSetSimilarityResultPageParam) {
        return Result.success(proSetSimilarityResultService.page(proSetSimilarityResultPageParam));
    }

    @Operation(summary = "添加题单代码相似度检测结果详情")
    //@SaCheckPermission("/pro/set/similarity/result/add")
    @PostMapping("/pro/set/similarity/result/add")
    public Result<?> add(@RequestBody @Valid ProSetSimilarityResultAddParam proSetSimilarityResultAddParam) {
        proSetSimilarityResultService.add(proSetSimilarityResultAddParam);
        return Result.success();
    }

    @Operation(summary = "编辑题单代码相似度检测结果详情")
    //@SaCheckPermission("/pro/set/similarity/result/edit")
    @PostMapping("/pro/set/similarity/result/edit")
    public Result<?> edit(@RequestBody @Valid ProSetSimilarityResultEditParam proSetSimilarityResultEditParam) {
        proSetSimilarityResultService.edit(proSetSimilarityResultEditParam);
        return Result.success();
    }

    @Operation(summary = "删除题单代码相似度检测结果详情")
    //@SaCheckPermission("/pro/set/similarity/result/delete")
    @PostMapping("/pro/set/similarity/result/delete")
    public Result<?> delete(@RequestBody @Valid @NotEmpty(message = "集合不能为空") List<ProSetSimilarityResultIdParam> proSetSimilarityResultIdParam) {
        proSetSimilarityResultService.delete(proSetSimilarityResultIdParam);
        return Result.success();
    }

    @Operation(summary = "获取题单代码相似度检测结果详情详情")
    //@SaCheckPermission("/pro/set/similarity/result/detail")
    @GetMapping("/pro/set/similarity/result/detail")
    public Result<?> detail(@ParameterObject @Valid ProSetSimilarityResultIdParam proSetSimilarityResultIdParam) {
        return Result.success(proSetSimilarityResultService.detail(proSetSimilarityResultIdParam));
    }
}