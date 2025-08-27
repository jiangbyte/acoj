package io.charlie.app.core.modular.problem.ranking.controller;

import io.charlie.app.core.modular.problem.ranking.param.ProblemUserRankingPageParam;
import io.charlie.app.core.modular.problem.ranking.service.ProblemUserRankingService;
import io.charlie.galaxy.result.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.NotEmpty;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Tag(name = "题目排行榜控制器")
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/api/v1")
@RestController
@Validated
public class ProblemUserRankingController {

    private final ProblemUserRankingService problemSubmitRankingService;

    @Operation(summary = "获取总排行榜分页")
    //@SaCheckPermission("/pro/problem/page")
    @GetMapping("/pro/problem/user/ranking/page/total")
    public Result<?> totalRankingPage(@ParameterObject ProblemUserRankingPageParam problemRankingPageParam) {
        return Result.success(problemSubmitRankingService.totalRankingPage(problemRankingPageParam));
    }

    @Operation(summary = "获取用户总排行榜")
    //@SaCheckPermission("/pro/problem/page")
    @GetMapping("/pro/problem/user/ranking/total/user")
    public Result<?> totalRankingByUserId(@RequestParam @NotEmpty String userId) {
        return Result.success(problemSubmitRankingService.totalRankingByUserId(userId));
    }

    @Operation(summary = "获取月排行榜分页")
    //@SaCheckPermission("/pro/problem/page")
    @GetMapping("/pro/problem/user/ranking/page/monthly")
    public Result<?> monthlyRankingPage(@ParameterObject ProblemUserRankingPageParam problemRankingPageParam) {
        return Result.success(problemSubmitRankingService.monthlyRankingPage(problemRankingPageParam));
    }

    @Operation(summary = "获取用户月排行榜")
    //@SaCheckPermission("/pro/problem/page")
    @GetMapping("/pro/problem/user/ranking/monthly/user")
    public Result<?> monthlyRankingByUserId(@RequestParam @NotEmpty String userId) {
        return Result.success(problemSubmitRankingService.monthlyRankingByUserId(userId));
    }

    @Operation(summary = "获取周排行榜分页")
    //@SaCheckPermission("/pro/problem/page")
    @GetMapping("/pro/problem/user/ranking/page/weekly")
    public Result<?> weeklyRankingPage(@ParameterObject ProblemUserRankingPageParam problemRankingPageParam) {
        return Result.success(problemSubmitRankingService.weeklyRankingPage(problemRankingPageParam));
    }

    @Operation(summary = "获取用户周排行榜")
    //@SaCheckPermission("/pro/problem/page")
    @GetMapping("/pro/problem/user/ranking/weekly/user")
    public Result<?> weeklyRankingByUserId(@RequestParam @NotEmpty String userId) {
        return Result.success(problemSubmitRankingService.weeklyRankingByUserId(userId));
    }

    @Operation(summary = "获取日排行榜分页")
    //@SaCheckPermission("/pro/problem/page")
    @GetMapping("/pro/problem/user/ranking/page/daily")
    public Result<?> dailyRankingPage(@ParameterObject ProblemUserRankingPageParam problemRankingPageParam) {
        return Result.success(problemSubmitRankingService.dailyRankingPage(problemRankingPageParam));
    }

    @Operation(summary = "获取用户日排行榜")
    //@SaCheckPermission("/pro/problem/page")
    @GetMapping("/pro/problem/user/ranking/daily/user")
    public Result<?> dailyRankingByUserId(@RequestParam @NotEmpty String userId) {
        return Result.success(problemSubmitRankingService.dailyRankingByUserId(userId));
    }

    @Operation(summary = "获取排行榜前N")
    //@SaCheckPermission("/pro/problem/page")
    @GetMapping("/pro/problem/user/ranking/top")
    public Result<?> top10() {
        return Result.success(problemSubmitRankingService.topN(10));
    }

    @Operation(summary = "获取活跃用户Top10")
    @GetMapping("/pro/problem/user/ranking/active/top10")
    public Result<?> getActiveUsersTop10() {
        return Result.success(problemSubmitRankingService.getActiveUsersTopN(10));
    }
}