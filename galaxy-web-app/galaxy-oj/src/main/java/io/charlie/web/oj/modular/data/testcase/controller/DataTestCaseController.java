package io.charlie.web.oj.modular.data.testcase.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import io.charlie.galaxy.result.Result;
import io.charlie.web.oj.annotation.log.Log;
import io.charlie.web.oj.annotation.log.LogCategory;
import io.charlie.web.oj.annotation.log.LogModule;
import io.charlie.web.oj.modular.data.testcase.param.DataTestCasePageParam;
import io.charlie.web.oj.modular.data.testcase.param.DataTestCaseAddParam;
import io.charlie.web.oj.modular.data.testcase.param.DataTestCaseEditParam;
import io.charlie.web.oj.modular.data.testcase.param.DataTestCaseIdParam;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import io.charlie.web.oj.modular.data.testcase.service.DataTestCaseService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

/**
* @author Charlie Zhang
* @version v1.0
* @date 2025-10-26
* @description 题目测试用例表 控制器
*/
@Tag(name = "题目测试用例表控制器")
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/api/v1")
@RestController
@Validated
public class DataTestCaseController {
    private final DataTestCaseService dataTestCaseService;

    @Operation(summary = "获取题目测试用例分页")
    @SaCheckPermission("/data/test/case/page")
    @GetMapping("/data/test/case/page")
    public Result<?> page(@ParameterObject DataTestCasePageParam dataTestCasePageParam) {
        return Result.success(dataTestCaseService.page(dataTestCasePageParam));
    }


    @Log(category = LogCategory.OPERATION, module = LogModule.DATA)
    @Operation(summary = "添加题目测试用例")
    @SaCheckPermission("/data/test/case/add")
    @PostMapping("/data/test/case/add")
    public Result<?> add(@RequestBody @Valid DataTestCaseAddParam dataTestCaseAddParam) {
        dataTestCaseService.add(dataTestCaseAddParam);
        return Result.success();
    }


    @Log(category = LogCategory.OPERATION, module = LogModule.DATA)
    @Operation(summary = "编辑题目测试用例")
    @SaCheckPermission("/data/test/case/edit")
    @PostMapping("/data/test/case/edit")
    public Result<?> edit(@RequestBody @Valid DataTestCaseEditParam dataTestCaseEditParam) {
        dataTestCaseService.edit(dataTestCaseEditParam);
        return Result.success();
    }


    @Log(category = LogCategory.OPERATION, module = LogModule.DATA)
    @Operation(summary = "删除题目测试用例")
    @SaCheckPermission("/data/test/case/delete")
    @PostMapping("/data/test/case/delete")
    public Result<?> delete(@RequestBody @Valid @NotEmpty(message = "集合不能为空") List<DataTestCaseIdParam> dataTestCaseIdParam) {
        dataTestCaseService.delete(dataTestCaseIdParam);
        return Result.success();
    }

    @Operation(summary = "获取题目测试用例详情")
    @SaCheckPermission("/data/test/case/detail")
    @GetMapping("/data/test/case/detail")
    public Result<?> detail(@ParameterObject @Valid DataTestCaseIdParam dataTestCaseIdParam) {
        return Result.success(dataTestCaseService.detail(dataTestCaseIdParam));
    }
}