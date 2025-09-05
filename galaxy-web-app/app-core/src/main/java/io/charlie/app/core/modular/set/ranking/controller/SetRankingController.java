package io.charlie.app.core.modular.set.ranking.controller;

import io.charlie.app.core.modular.problem.ranking.param.ProblemRankingPageParam;
import io.charlie.app.core.modular.set.ranking.param.SetRankingPageParam;
import io.charlie.app.core.modular.set.ranking.service.SetRankingService;
import io.charlie.galaxy.result.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "题目排行榜控制器")
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/api/v1")
@RestController
@Validated
public class SetRankingController {

    private final SetRankingService setRankingService;

    @Operation(summary = "获取题集排行榜分页")
    //@SaCheckPermission("/pro/problem/page")
    @GetMapping("/pro/set/ranking/page")
    public Result<?> rankingPage(@ParameterObject SetRankingPageParam setRankingPageParam) {
        return Result.success(setRankingService.rankingPage(setRankingPageParam));
    }

    @Operation(summary = "获取排行榜前N")
    //@SaCheckPermission("/pro/problem/page")
    @GetMapping("/pro/set/set/ranking/top")
    public Result<?> top10() {
        return Result.success(setRankingService.topN(10));
    }
}