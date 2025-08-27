package io.charlie.app.core.modular.problem.problem.controller;

import io.charlie.app.core.annotation.UserActivity;
import io.charlie.galaxy.result.Result;
import io.charlie.app.core.modular.problem.problem.param.ProProblemPageParam;
import io.charlie.app.core.modular.problem.problem.param.ProProblemAddParam;
import io.charlie.app.core.modular.problem.problem.param.ProProblemEditParam;
import io.charlie.app.core.modular.problem.problem.param.ProProblemIdParam;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import io.charlie.app.core.modular.problem.problem.service.ProProblemService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

/**
 * @author Charlie Zhang
 * @version v1.0
 * @date 2025-07-25
 * @description 题目表 控制器
 */
@Tag(name = "题目表控制器")
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/api/v1")
@RestController
@Validated
public class ProProblemController {
    private final ProProblemService proProblemService;

    @Operation(summary = "获取题目分页")
    //@SaCheckPermission("/pro/problem/page")
    @GetMapping("/pro/problem/page")
    public Result<?> page(@ParameterObject ProProblemPageParam proProblemPageParam) {
        return Result.success(proProblemService.page(proProblemPageParam));
    }

    @Operation(summary = "添加题目")
    //@SaCheckPermission("/pro/problem/add")
    @PostMapping("/pro/problem/add")
    public Result<?> add(@RequestBody @Valid ProProblemAddParam proProblemAddParam) {
        proProblemService.add(proProblemAddParam);
        return Result.success();
    }

    @Operation(summary = "编辑题目")
    //@SaCheckPermission("/pro/problem/edit")
    @PostMapping("/pro/problem/edit")
    public Result<?> edit(@RequestBody @Valid ProProblemEditParam proProblemEditParam) {
        proProblemService.edit(proProblemEditParam);
        return Result.success();
    }

    @Operation(summary = "删除题目")
    //@SaCheckPermission("/pro/problem/delete")
    @PostMapping("/pro/problem/delete")
    public Result<?> delete(@RequestBody @Valid @NotEmpty(message = "集合不能为空") List<ProProblemIdParam> proProblemIdParam) {
        proProblemService.delete(proProblemIdParam);
        return Result.success();
    }

    @Operation(summary = "获取题目详情")
    //@SaCheckPermission("/pro/problem/detail")
    @GetMapping("/pro/problem/detail")
    public Result<?> detail(@ParameterObject @Valid ProProblemIdParam proProblemIdParam) {
        return Result.success(proProblemService.detail(proProblemIdParam));
    }

    @Operation(summary = "C端-获取题目详情")
    //@SaCheckPermission("/pro/problem/detail/client")
    @GetMapping("/pro/problem/detail/client")
    @UserActivity
    public Result<?> detailC(@ParameterObject @Valid ProProblemIdParam proProblemIdParam) {
        return Result.success(proProblemService.appDetail(proProblemIdParam));
    }

    @Operation(summary = "C端-获取题目分页")
    //@SaCheckPermission("/pro/problem/detail/client")
    @GetMapping("/pro/problem/page/client")
    @UserActivity
    public Result<?> pageC(@ParameterObject @Valid ProProblemPageParam proProblemPageParam) {
        return Result.success(proProblemService.appPage(proProblemPageParam));
    }

    @Operation(summary = "C端-获取最新10道题目")
    @GetMapping("/pro/problem/latest")
    public Result<?> latest10() {
        return Result.success(proProblemService.latestN(10));
    }

    @Operation(summary = "C端-难度分布")
    @GetMapping("/pro/problem/difficulty/distribution")
    public Result<?> difficultyDistribution() {
        return Result.success(proProblemService.difficultyDistribution());
    }

    @Operation(summary = "C端-获取题目统计和增长百分比")
    @GetMapping("/pro/problem/problemcountandpercentage")
    public Result<?> getProblemCountAndPercentage() {
        return Result.success(proProblemService.getProblemCountAndPercentage());
    }

    @Operation(summary = "C端-获取今日题目数量")
    @GetMapping("/pro/problem/today/problemcount")
    public Result<?> getTodayProblemCount() {
        return Result.success(proProblemService.getTodayProblemCount());
    }
}