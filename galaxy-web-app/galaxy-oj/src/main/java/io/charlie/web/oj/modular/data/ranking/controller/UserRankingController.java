package io.charlie.web.oj.modular.data.ranking.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.charlie.galaxy.result.Result;
import io.charlie.web.oj.modular.data.ranking.UserRankingIdParam;
import io.charlie.web.oj.modular.data.ranking.UserRankingPageParam;
import io.charlie.web.oj.modular.data.ranking.service.UserRankingService;
import io.charlie.web.oj.modular.sys.user.entity.SysUser;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * @author ZhangJiangHu
 * @version v1.0
 * @date 21/09/2025
 * @description TODO
 */
@Tag(name = "用户排行榜控制器")
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/api/v1")
@RestController
@Validated
public class UserRankingController {
    private final UserRankingService userRankingService;

    @Operation(summary = "用户TopN排行榜")
    @GetMapping("/data/user/rank/top")
    public Result<?> getTopUsers() {
        List<SysUser> topUsers = userRankingService.getTopUsers(10);
        return Result.success(topUsers);
    }

    @Operation(summary = "用户排行榜分页")
    @GetMapping("/data/user/rank/page")
    public Result<?> getRankingPage(@ParameterObject UserRankingPageParam userRankingPageParam) {
        Page<SysUser> rankingPage = userRankingService.getRankingPage(userRankingPageParam);
        return Result.success(rankingPage);
    }

    @Operation(summary = "用户排行榜详情")
    @GetMapping("/data/user/rank/detail")
    public Result<?> getUserRank(@ParameterObject UserRankingIdParam idParam) {
        SysUser userRank = userRankingService.getUserRank(idParam.getId());
        return Result.success(userRank);
    }

    @Operation(summary = "用户排行榜活跃TopN")
    @GetMapping("/data/user/rank/active/top")
    public Result<?> getActiveTop() {
        List<SysUser> activeTop = userRankingService.getActiveTop(10);
        return Result.success(activeTop);
    }
}
