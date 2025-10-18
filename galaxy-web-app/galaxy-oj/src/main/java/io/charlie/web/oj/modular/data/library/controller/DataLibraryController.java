package io.charlie.web.oj.modular.data.library.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import io.charlie.galaxy.result.Result;
import io.charlie.web.oj.annotation.log.Log;
import io.charlie.web.oj.annotation.log.LogCategory;
import io.charlie.web.oj.annotation.log.LogModule;
import io.charlie.web.oj.modular.data.library.param.DataLibraryPageParam;
import io.charlie.web.oj.modular.data.library.param.DataLibraryAddParam;
import io.charlie.web.oj.modular.data.library.param.DataLibraryEditParam;
import io.charlie.web.oj.modular.data.library.param.DataLibraryIdParam;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import io.charlie.web.oj.modular.data.library.service.DataLibraryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

/**
* @author Charlie Zhang
* @version v1.0
* @date 2025-09-20
* @description 提交样本库 控制器
*/
@Tag(name = "提交样本库控制器")
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/api/v1")
@RestController
@Validated
public class DataLibraryController {
    private final DataLibraryService dataLibraryService;

    @Operation(summary = "获取提交样本库分页")
    @SaCheckPermission("/data/library/page")
    @GetMapping("/data/library/page")
    public Result<?> page(@ParameterObject DataLibraryPageParam dataLibraryPageParam) {
        return Result.success(dataLibraryService.page(dataLibraryPageParam));
    }


    @Log(category = LogCategory.OPERATION, module = LogModule.DATA)
    @Operation(summary = "添加提交样本库")
    @SaCheckPermission("/data/library/add")
    @PostMapping("/data/library/add")
    public Result<?> add(@RequestBody @Valid DataLibraryAddParam dataLibraryAddParam) {
        dataLibraryService.add(dataLibraryAddParam);
        return Result.success();
    }

    @Log(category = LogCategory.OPERATION, module = LogModule.DATA)
    @Operation(summary = "编辑提交样本库")
    @SaCheckPermission("/data/library/edit")
    @PostMapping("/data/library/edit")
    public Result<?> edit(@RequestBody @Valid DataLibraryEditParam dataLibraryEditParam) {
        dataLibraryService.edit(dataLibraryEditParam);
        return Result.success();
    }

    @Log(category = LogCategory.OPERATION, module = LogModule.DATA)
    @Operation(summary = "删除提交样本库")
    @SaCheckPermission("/data/library/delete")
    @PostMapping("/data/library/delete")
    public Result<?> delete(@RequestBody @Valid @NotEmpty(message = "集合不能为空") List<DataLibraryIdParam> dataLibraryIdParam) {
        dataLibraryService.delete(dataLibraryIdParam);
        return Result.success();
    }

    @Operation(summary = "获取提交样本库详情")
    @SaCheckPermission("/data/library/detail")
    @GetMapping("/data/library/detail")
    public Result<?> detail(@ParameterObject @Valid DataLibraryIdParam dataLibraryIdParam) {
        return Result.success(dataLibraryService.detail(dataLibraryIdParam));
    }
}