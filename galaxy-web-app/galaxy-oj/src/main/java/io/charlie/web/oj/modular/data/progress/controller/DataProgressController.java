package io.charlie.web.oj.modular.data.progress.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import io.charlie.galaxy.result.Result;
import io.charlie.web.oj.annotation.log.Log;
import io.charlie.web.oj.annotation.log.LogCategory;
import io.charlie.web.oj.annotation.log.LogModule;
import io.charlie.web.oj.modular.data.progress.param.DataProgressPageParam;
import io.charlie.web.oj.modular.data.progress.param.DataProgressAddParam;
import io.charlie.web.oj.modular.data.progress.param.DataProgressEditParam;
import io.charlie.web.oj.modular.data.progress.param.DataProgressIdParam;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import io.charlie.web.oj.modular.data.progress.service.DataProgressService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

/**
* @author Charlie Zhang
* @version v1.0
* @date 2025-09-20
* @description 题集进度表 控制器
*/
@Tag(name = "题集进度表控制器")
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/api/v1")
@RestController
@Validated
public class DataProgressController {
    private final DataProgressService dataProgressService;

    @Operation(summary = "获取题集进度分页")
    @SaCheckPermission("/data/progress/page")
    @GetMapping("/data/progress/page")
    public Result<?> page(@ParameterObject DataProgressPageParam dataProgressPageParam) {
        return Result.success(dataProgressService.page(dataProgressPageParam));
    }

    @Log(category = LogCategory.OPERATION, module = LogModule.DATA)
    @Operation(summary = "添加题集进度")
    @SaCheckPermission("/data/progress/add")
    @PostMapping("/data/progress/add")
    public Result<?> add(@RequestBody @Valid DataProgressAddParam dataProgressAddParam) {
        dataProgressService.add(dataProgressAddParam);
        return Result.success();
    }

    @Log(category = LogCategory.OPERATION, module = LogModule.DATA)
    @Operation(summary = "编辑题集进度")
    @SaCheckPermission("/data/progress/edit")
    @PostMapping("/data/progress/edit")
    public Result<?> edit(@RequestBody @Valid DataProgressEditParam dataProgressEditParam) {
        dataProgressService.edit(dataProgressEditParam);
        return Result.success();
    }

    @Log(category = LogCategory.OPERATION, module = LogModule.DATA)
    @Operation(summary = "删除题集进度")
    @SaCheckPermission("/data/progress/delete")
    @PostMapping("/data/progress/delete")
    public Result<?> delete(@RequestBody @Valid @NotEmpty(message = "集合不能为空") List<DataProgressIdParam> dataProgressIdParam) {
        dataProgressService.delete(dataProgressIdParam);
        return Result.success();
    }

    @Operation(summary = "获取题集进度详情")
    @SaCheckPermission("/data/progress/detail")
    @GetMapping("/data/progress/detail")
    public Result<?> detail(@ParameterObject @Valid DataProgressIdParam dataProgressIdParam) {
        return Result.success(dataProgressService.detail(dataProgressIdParam));
    }
}