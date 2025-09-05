package io.charlie.app.core.modular.analyse.controller;

import io.charlie.app.core.modular.analyse.service.ProblemAnalyseService;
import io.charlie.app.core.modular.analyse.service.ProblemSubmitAnalyseService;
import io.charlie.app.core.modular.analyse.service.TotalAnalyseService;
import io.charlie.app.core.modular.analyse.service.UserAnalyseService;
import io.charlie.galaxy.result.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author Charlie Zhang
 * @version v1.0
 * @date 25/07/2025
 * @description 统计分析
 */
@Tag(name = "分析控制器")
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/api/v1")
@RestController
@Validated
public class AnalyseController {
    private final UserAnalyseService userAnalyseService;
    private final ProblemAnalyseService problemAnalyseService;
    private final ProblemSubmitAnalyseService problemSubmitAnalyseService;
    private final TotalAnalyseService totalAnalyseService;

    @Operation(summary = "获取总统计信息")
    @GetMapping("/analyse/total")
    public Result<?> getTotalAnalyse() {
        return Result.success(totalAnalyseService.getTotalAnalyse());
    }

    @Operation(summary = "获取用户统计信息")
    @GetMapping("/analyse/user/total")
    public Result<?> getTotalUserAnalyse() {
        return Result.success(userAnalyseService.getTotalUserAnalyse());
    }

    @Operation(summary = "获取题目统计信息")
    @GetMapping("/analyse/problem/total")
    public Result<?> getTotalProblemAnalyse() {
        return Result.success(problemAnalyseService.getTotalProblemAnalyse());
    }

    @Operation(summary = "获取提交统计信息")
    @GetMapping("/analyse/problem/submit/total")
    public Result<?> getTotalProblemSubmitAnalyse() {
        return Result.success(problemSubmitAnalyseService.getTotalProblemSubmitAnalyse());
    }

    @Operation(summary = "获取语言分布统计信息")
    @GetMapping("/analyse/problem/submit/language")
    public Result<?> getLanguageDistribution() {
        return Result.success(problemSubmitAnalyseService.getLanguageDistribution());
    }

    @Operation(summary = "获取题目通过率分布统计信息")
    @GetMapping("/analyse/problem/pass/rate")
    public Result<?> getProblemRateDistribution() {
        return Result.success(problemSubmitAnalyseService.getProblemRateDistribution());
    }

    @Operation(summary = "获取一周提交趋势统计信息")
    @GetMapping("/analyse/problem/submit/trend")
    public Result<?> getWeeklySubmitTrend() {
        return Result.success(problemSubmitAnalyseService.getWeeklySubmitTrend());
    }


    @Operation(summary = "获取24小时提交趋势统计信息")
    @GetMapping("/analyse/problem/submit/today/trend")
    public Result<?> getTodayHourlySubmitTrend() {
        return Result.success(problemSubmitAnalyseService.getTodayHourlySubmitTrend());
    }
}
