package io.charlie.app.core.modular.problem.ranking.controller;

import io.charlie.app.core.modular.problem.ranking.param.ProblemRankingPageParam;
import io.charlie.app.core.modular.problem.ranking.param.ProblemUserRankingPageParam;
import io.charlie.app.core.modular.problem.ranking.service.ProblemRankingService;
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
public class ProblemRankingController {

    private final ProblemRankingService problemRankingService;

    @Operation(summary = "获取题目排行榜分页")
    //@SaCheckPermission("/pro/problem/page")
    @GetMapping("/pro/problem/ranking/page")
    public Result<?> rankingPage(@ParameterObject ProblemRankingPageParam rankingPageParam) {
        return Result.success(problemRankingService.rankingPage(rankingPageParam));
    }

    @Operation(summary = "获取排行榜前N")
    //@SaCheckPermission("/pro/problem/page")
    @GetMapping("/pro/problem/problem/ranking/top")
    public Result<?> top10() {
        return Result.success(problemRankingService.topN(10));
    }
}