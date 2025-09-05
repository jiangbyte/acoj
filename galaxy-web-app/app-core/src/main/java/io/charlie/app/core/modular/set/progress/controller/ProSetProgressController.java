package io.charlie.app.core.modular.set.progress.controller;

import io.charlie.app.core.modular.set.progress.param.*;
import io.charlie.galaxy.result.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import io.charlie.app.core.modular.set.progress.service.ProSetProgressService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

/**
* @author Charlie Zhang
* @version v1.0
* @date 2025-07-25
* @description 题集进度表 控制器
*/
@Tag(name = "题集进度表控制器")
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/api/v1")
@RestController
@Validated
public class ProSetProgressController {
    private final ProSetProgressService proSetProgressService;

    @Operation(summary = "获取题集进度分页")
    //@SaCheckPermission("/pro/set/progress/page")
    @GetMapping("/pro/set/progress/page")
    public Result<?> page(@ParameterObject ProSetProgressPageParam proSetProgressPageParam) {
        return Result.success(proSetProgressService.page(proSetProgressPageParam));
    }

    @Operation(summary = "添加题集进度")
    //@SaCheckPermission("/pro/set/progress/add")
    @PostMapping("/pro/set/progress/add")
    public Result<?> add(@RequestBody @Valid ProSetProgressAddParam proSetProgressAddParam) {
        proSetProgressService.add(proSetProgressAddParam);
        return Result.success();
    }

    @Operation(summary = "编辑题集进度")
    //@SaCheckPermission("/pro/set/progress/edit")
    @PostMapping("/pro/set/progress/edit")
    public Result<?> edit(@RequestBody @Valid ProSetProgressEditParam proSetProgressEditParam) {
        proSetProgressService.edit(proSetProgressEditParam);
        return Result.success();
    }

    @Operation(summary = "删除题集进度")
    //@SaCheckPermission("/pro/set/progress/delete")
    @PostMapping("/pro/set/progress/delete")
    public Result<?> delete(@RequestBody @Valid @NotEmpty(message = "集合不能为空") List<ProSetProgressIdParam> proSetProgressIdParam) {
        proSetProgressService.delete(proSetProgressIdParam);
        return Result.success();
    }

    @Operation(summary = "获取题集进度详情")
    //@SaCheckPermission("/pro/set/progress/detail")
    @GetMapping("/pro/set/progress/detail")
    public Result<?> detail(@ParameterObject @Valid ProSetProgressIdParam proSetProgressIdParam) {
        return Result.success(proSetProgressService.detail(proSetProgressIdParam));
    }

    @Operation(summary = "获取题集进度进度数据分页")
    //@SaCheckPermission("/pro/set/progress/datapage")
    @GetMapping("/pro/set/progress/datapage")
    public Result<?> progressDataPage(@ParameterObject @Valid ProSetProgressDataPageParam proSetProgressDataPageParam) {
        return Result.success(proSetProgressService.progressDataPage(proSetProgressDataPageParam));
    }
}