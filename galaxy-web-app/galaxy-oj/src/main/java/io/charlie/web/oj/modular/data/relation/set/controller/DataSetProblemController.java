package io.charlie.web.oj.modular.data.relation.set.controller;

import io.charlie.galaxy.result.Result;
import io.charlie.web.oj.modular.data.relation.set.param.DataSetProblemAddParam;
import io.charlie.web.oj.modular.data.relation.set.param.DataSetProblemEditParam;
import io.charlie.web.oj.modular.data.relation.set.param.DataSetProblemIdParam;
import io.charlie.web.oj.modular.data.relation.set.param.DataSetProblemPageParam;
import io.charlie.web.oj.modular.data.relation.set.service.DataSetProblemService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

/**
 * @author Charlie Zhang
 * @version v1.0
 * @date 2025-11-07
 * @description 题集题目 控制器
 */
@Tag(name = "题集题目控制器")
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/api/v1")
@RestController
@Validated
public class DataSetProblemController {
    private final DataSetProblemService dataSetProblemService;

    @Operation(summary = "获取题集题目分页")
    //@SaCheckPermission("/data/set/problem/page")
    @GetMapping("/data/set/problem/page")
    public Result<?> page(@ParameterObject DataSetProblemPageParam dataSetProblemPageParam) {
        return Result.success(dataSetProblemService.page(dataSetProblemPageParam));
    }

    @Operation(summary = "添加题集题目")
    //@SaCheckPermission("/data/set/problem/add")
    @PostMapping("/data/set/problem/add")
    public Result<?> add(@RequestBody @Valid DataSetProblemAddParam dataSetProblemAddParam) {
        dataSetProblemService.add(dataSetProblemAddParam);
        return Result.success();
    }

    @Operation(summary = "编辑题集题目")
    //@SaCheckPermission("/data/set/problem/edit")
    @PostMapping("/data/set/problem/edit")
    public Result<?> edit(@RequestBody @Valid DataSetProblemEditParam dataSetProblemEditParam) {
        dataSetProblemService.edit(dataSetProblemEditParam);
        return Result.success();
    }

    @Operation(summary = "删除题集题目")
    //@SaCheckPermission("/data/set/problem/delete")
    @PostMapping("/data/set/problem/delete")
    public Result<?> delete(@RequestBody @Valid @NotEmpty(message = "集合不能为空") List<DataSetProblemIdParam> dataSetProblemIdParam) {
        dataSetProblemService.delete(dataSetProblemIdParam);
        return Result.success();
    }

    @Operation(summary = "获取题集题目")
    @GetMapping("/data/set/problem/manageLists")
    public Result<?> manageLists(@RequestParam String setId) {
        return Result.success(dataSetProblemService.getSetProblems(setId));
    }

}