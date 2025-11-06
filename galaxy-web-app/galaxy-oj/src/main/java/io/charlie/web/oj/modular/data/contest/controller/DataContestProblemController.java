package io.charlie.web.oj.modular.data.contest.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import io.charlie.galaxy.result.Result;
import io.charlie.web.oj.modular.data.contest.param.DataContestProblemPageParam;
import io.charlie.web.oj.modular.data.contest.param.DataContestProblemAddParam;
import io.charlie.web.oj.modular.data.contest.param.DataContestProblemEditParam;
import io.charlie.web.oj.modular.data.contest.param.DataContestProblemIdParam;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import io.charlie.web.oj.modular.data.contest.service.DataContestProblemService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

/**
* @author Charlie Zhang
* @version v1.0
* @date 2025-11-06
* @description 竞赛题目表 控制器
*/
@Tag(name = "竞赛题目表控制器")
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/api/v1")
@RestController
@Validated
public class DataContestProblemController {
    private final DataContestProblemService dataContestProblemService;

    @Operation(summary = "获取竞赛题目分页")
//    @SaCheckPermission("/data/contest/problem/page")
    @GetMapping("/data/contest/problem/page")
    public Result<?> page(@ParameterObject DataContestProblemPageParam dataContestProblemPageParam) {
        return Result.success(dataContestProblemService.page(dataContestProblemPageParam));
    }

    @Operation(summary = "添加竞赛题目")
//    @SaCheckPermission("/data/contest/problem/add")
    @PostMapping("/data/contest/problem/add")
    public Result<?> add(@RequestBody @Valid DataContestProblemAddParam dataContestProblemAddParam) {
        dataContestProblemService.add(dataContestProblemAddParam);
        return Result.success();
    }

    @Operation(summary = "编辑竞赛题目")
//    @SaCheckPermission("/data/contest/problem/edit")
    @PostMapping("/data/contest/problem/edit")
    public Result<?> edit(@RequestBody @Valid DataContestProblemEditParam dataContestProblemEditParam) {
        dataContestProblemService.edit(dataContestProblemEditParam);
        return Result.success();
    }

    @Operation(summary = "删除竞赛题目")
//    @SaCheckPermission("/data/contest/problem/delete")
    @PostMapping("/data/contest/problem/delete")
    public Result<?> delete(@RequestBody @Valid @NotEmpty(message = "集合不能为空") List<DataContestProblemIdParam> dataContestProblemIdParam) {
        dataContestProblemService.delete(dataContestProblemIdParam);
        return Result.success();
    }

    @Operation(summary = "获取竞赛题目详情")
//    @SaCheckPermission("/data/contest/problem/detail")
    @GetMapping("/data/contest/problem/detail")
    public Result<?> detail(@ParameterObject @Valid DataContestProblemIdParam dataContestProblemIdParam) {
        return Result.success(dataContestProblemService.detail(dataContestProblemIdParam));
    }
}