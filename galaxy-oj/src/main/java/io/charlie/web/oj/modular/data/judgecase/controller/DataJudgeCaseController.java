package io.charlie.web.oj.modular.data.judgecase.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import io.charlie.galaxy.result.Result;
import io.charlie.web.oj.modular.data.judgecase.param.DataJudgeCasePageParam;
import io.charlie.web.oj.modular.data.judgecase.param.DataJudgeCaseAddParam;
import io.charlie.web.oj.modular.data.judgecase.param.DataJudgeCaseEditParam;
import io.charlie.web.oj.modular.data.judgecase.param.DataJudgeCaseIdParam;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import io.charlie.web.oj.modular.data.judgecase.service.DataJudgeCaseService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

/**
* @author Charlie Zhang
* @version v1.0
* @date 2025-10-26
* @description 判题结果用例表 控制器
*/
@Tag(name = "判题结果用例表控制器")
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/api/v1")
@RestController
@Validated
public class DataJudgeCaseController {
    private final DataJudgeCaseService dataJudgeCaseService;

    @Operation(summary = "获取判题结果用例分页")
    @SaCheckPermission("/data/judge/case/page")
    @GetMapping("/data/judge/case/page")
    public Result<?> page(@ParameterObject DataJudgeCasePageParam dataJudgeCasePageParam) {
        return Result.success(dataJudgeCaseService.page(dataJudgeCasePageParam));
    }

    @Operation(summary = "添加判题结果用例")
    @SaCheckPermission("/data/judge/case/add")
    @PostMapping("/data/judge/case/add")
    public Result<?> add(@RequestBody @Valid DataJudgeCaseAddParam dataJudgeCaseAddParam) {
        dataJudgeCaseService.add(dataJudgeCaseAddParam);
        return Result.success();
    }

    @Operation(summary = "编辑判题结果用例")
    @SaCheckPermission("/data/judge/case/edit")
    @PostMapping("/data/judge/case/edit")
    public Result<?> edit(@RequestBody @Valid DataJudgeCaseEditParam dataJudgeCaseEditParam) {
        dataJudgeCaseService.edit(dataJudgeCaseEditParam);
        return Result.success();
    }

    @Operation(summary = "删除判题结果用例")
    @SaCheckPermission("/data/judge/case/delete")
    @PostMapping("/data/judge/case/delete")
    public Result<?> delete(@RequestBody @Valid @NotEmpty(message = "集合不能为空") List<DataJudgeCaseIdParam> dataJudgeCaseIdParam) {
        dataJudgeCaseService.delete(dataJudgeCaseIdParam);
        return Result.success();
    }

    @Operation(summary = "获取判题结果用例详情")
    @SaCheckPermission("/data/judge/case/detail")
    @GetMapping("/data/judge/case/detail")
    public Result<?> detail(@ParameterObject @Valid DataJudgeCaseIdParam dataJudgeCaseIdParam) {
        return Result.success(dataJudgeCaseService.detail(dataJudgeCaseIdParam));
    }
}