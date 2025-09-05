package io.charlie.app.core.modular.set.ranking.controller;

import io.charlie.app.core.modular.set.ranking.param.SetUserRankingPageParam;
import io.charlie.app.core.modular.set.ranking.service.SetUserRankingService;
import io.charlie.galaxy.result.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.NotEmpty;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Tag(name = "题集排行榜控制器")
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/api/v1")
@RestController
@Validated
public class SetUserRankingController {

    private final SetUserRankingService setSubmitRankingService;

    @Operation(summary = "获取总排行榜分页")
    //@SaCheckPermission("/pro/problem/page")
    @GetMapping("/pro/set/ranking/page/total")
    public Result<?> totalRankingPage(@ParameterObject SetUserRankingPageParam setRankingPageParam) {
        return Result.success(setSubmitRankingService.totalRankingPage(setRankingPageParam));
    }

    @Operation(summary = "获取用户总排行榜")
    //@SaCheckPermission("/pro/problem/page")
    @GetMapping("/pro/set/ranking/total/user")
    public Result<?> totalRankingByUserId(@RequestParam @NotEmpty String setId, @RequestParam @NotEmpty String userId) {
        return Result.success(setSubmitRankingService.totalRankingByUserId(setId, userId));
    }

    @Operation(summary = "获取月排行榜分页")
    //@SaCheckPermission("/pro/problem/page")
    @GetMapping("/pro/set/ranking/page/monthly")
    public Result<?> monthlyRankingPage(@ParameterObject SetUserRankingPageParam setRankingPageParam) {
        return Result.success(setSubmitRankingService.monthlyRankingPage(setRankingPageParam));
    }

    @Operation(summary = "获取用户月排行榜")
    //@SaCheckPermission("/pro/problem/page")
    @GetMapping("/pro/set/ranking/monthly/user")
    public Result<?> monthlyRankingByUserId(@RequestParam @NotEmpty String setId, @RequestParam @NotEmpty String userId) {
        return Result.success(setSubmitRankingService.monthlyRankingByUserId(setId, userId));
    }

    @Operation(summary = "获取周排行榜分页")
    //@SaCheckPermission("/pro/problem/page")
    @GetMapping("/pro/set/ranking/page/weekly")
    public Result<?> weeklyRankingPage(@ParameterObject SetUserRankingPageParam setRankingPageParam) {
        return Result.success(setSubmitRankingService.weeklyRankingPage(setRankingPageParam));
    }

    @Operation(summary = "获取用户周排行榜")
    //@SaCheckPermission("/pro/problem/page")
    @GetMapping("/pro/set/ranking/weekly/user")
    public Result<?> weeklyRankingByUserId(@RequestParam @NotEmpty String setId, @RequestParam @NotEmpty String userId) {
        return Result.success(setSubmitRankingService.weeklyRankingByUserId(setId, userId));
    }

    @Operation(summary = "获取日排行榜分页")
    //@SaCheckPermission("/pro/problem/page")
    @GetMapping("/pro/set/ranking/page/daily")
    public Result<?> dailyRankingPage(@ParameterObject SetUserRankingPageParam setRankingPageParam) {
        return Result.success(setSubmitRankingService.dailyRankingPage(setRankingPageParam));
    }

    @Operation(summary = "获取用户日排行榜")
    //@SaCheckPermission("/pro/problem/page")
    @GetMapping("/pro/set/ranking/daily/user")
    public Result<?> dailyRankingByUserId(@RequestParam @NotEmpty String setId, @RequestParam @NotEmpty String userId) {
        return Result.success(setSubmitRankingService.dailyRankingByUserId(setId, userId));
    }

}