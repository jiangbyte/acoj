package io.charlie.app.core.modular.test;

import io.charlie.galaxy.enums.ISortOrderEnum;
import io.charlie.galaxy.result.Result;
import io.charlie.galaxy.utils.ranking.RankingInfo;
import io.charlie.galaxy.utils.ranking.RankingUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.List;

/**
 * 排行榜控制器
 *
 * @author ZhangJiangHu
 * @version v1.0
 * @date 18/09/2025
 */
@Tag(name = "排行榜管理", description = "提供排行榜的增删改查和相关查询功能")
@RestController
@RequestMapping("/api/ranking")
@RequiredArgsConstructor
public class RankingController {

    private final RankingUtil rankingUtil;

    @Operation(summary = "添加或更新排行榜数据", description = "向指定排行榜中添加或更新实体数据")
    @PostMapping("/{rankingKey}/{entityId}")
    public Result<Void> addOrUpdate(
            @Parameter(description = "排行榜键") @PathVariable String rankingKey,
            @Parameter(description = "实体ID") @PathVariable String entityId,
            @Parameter(description = "分数") @RequestParam double score) {
        rankingUtil.addOrUpdate(rankingKey, entityId, score);
        return Result.success();
    }

    @Operation(summary = "自增更新排行榜数据", description = "对指定排行榜中的实体分数进行自增操作")
    @PostMapping("/auto-score/{rankingKey}/{entityId}")
    public Result<Void> addOrUpdateAutoScore(
            @Parameter(description = "排行榜键") @PathVariable String rankingKey,
            @Parameter(description = "实体ID") @PathVariable String entityId,
            @Parameter(description = "自增分数") @RequestParam double score) {
        rankingUtil.addOrUpdateAutoScore(rankingKey, entityId, score);
        return Result.success();
    }

    @Operation(summary = "获取单个实体排名信息", description = "获取指定实体在排行榜中的排名信息")
    @GetMapping("/{rankingKey}/{entityId}")
    public Result<RankingInfo> getRanking(
            @Parameter(description = "排行榜键") @PathVariable String rankingKey,
            @Parameter(description = "实体ID") @PathVariable String entityId) {
        RankingInfo rankingInfo = rankingUtil.getRanking(rankingKey, entityId);
        return Result.success(rankingInfo);
    }

    @Operation(summary = "获取排行榜前N名", description = "获取指定排行榜的前N名数据")
    @GetMapping("/top/{rankingKey}")
    public Result<List<RankingInfo>> getTopNRanking(
            @Parameter(description = "排行榜键") @PathVariable String rankingKey,
            @Parameter(description = "前N名数量") @RequestParam int topN) {
        List<RankingInfo> rankingInfos = rankingUtil.getTopNRanking(rankingKey, topN);
        return Result.success(rankingInfos);
    }

    @Operation(summary = "获取时间范围内排行榜前N名", description = "获取指定时间范围内的排行榜前N名数据")
    @GetMapping("/top-time/{rankingKey}")
    public Result<List<RankingInfo>> getTopNRankingWithTime(
            @Parameter(description = "排行榜键") @PathVariable String rankingKey,
            @Parameter(description = "前N名数量") @RequestParam int topN,
            @Parameter(description = "开始时间") @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") Date startTime,
            @Parameter(description = "结束时间") @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") Date endTime) {
        List<RankingInfo> rankingInfos = rankingUtil.getTopNRanking(rankingKey, topN, startTime, endTime);
        return Result.success(rankingInfos);
    }

    @Operation(summary = "分页获取排行榜数据", description = "分页获取排行榜数据，支持排序")
    @GetMapping("/page/{rankingKey}")
    public Result<Result.PageData<RankingInfo>> getRankingPage(
            @Parameter(description = "排行榜键") @PathVariable String rankingKey,
            @Parameter(description = "页码，从0开始") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "每页大小") @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "排序方式") @RequestParam(required = false) ISortOrderEnum order) {
        Result.PageData<RankingInfo> pageData = rankingUtil.getRankingPage(rankingKey, page, size, order);
        return Result.success(pageData);
    }

    @Operation(summary = "根据实体ID列表分页获取排名信息", description = "根据指定的实体ID列表分页获取其在排行榜中的排名信息")
    @PostMapping("/page-by-entities/{rankingKey}")
    public Result<Result.PageData<RankingInfo>> getRankingPageByEntityIds(
            @Parameter(description = "排行榜键") @PathVariable String rankingKey,
            @Parameter(description = "实体ID列表") @RequestBody List<String> entityIds,
            @Parameter(description = "页码，从0开始") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "每页大小") @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "排序方式") @RequestParam(required = false) ISortOrderEnum order) {
        Result.PageData<RankingInfo> pageData = rankingUtil.getRankingPageByEntityIds(rankingKey, entityIds, page, size, order);
        return Result.success(pageData);
    }

    @Operation(summary = "根据时间范围分页获取排行榜数据", description = "根据时间范围分页获取排行榜数据，支持排序")
    @GetMapping("/page-time/{rankingKey}")
    public Result<Result.PageData<RankingInfo>> getRankingPageByTime(
            @Parameter(description = "排行榜键") @PathVariable String rankingKey,
            @Parameter(description = "页码，从0开始") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "每页大小") @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "开始时间") @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") Date startTime,
            @Parameter(description = "结束时间") @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") Date endTime,
            @Parameter(description = "排序方式") @RequestParam(required = false) ISortOrderEnum order) {
        Result.PageData<RankingInfo> pageData = rankingUtil.getRankingPageByTime(rankingKey, page, size, startTime, endTime, order);
        return Result.success(pageData);
    }

    @Operation(summary = "检查实体是否存在排行榜数据", description = "检查指定实体在排行榜中是否存在数据")
    @GetMapping("/exists/{rankingKey}/{entityId}")
    public Result<Boolean> exists(
            @Parameter(description = "排行榜键") @PathVariable String rankingKey,
            @Parameter(description = "实体ID") @PathVariable String entityId) {
        boolean exists = rankingUtil.exists(rankingKey, entityId);
        return Result.success(exists);
    }

    @Operation(summary = "检查实体是否存在有效数据", description = "检查指定实体在排行榜中是否存在有效数据（带时间验证）")
    @GetMapping("/exists-time/{rankingKey}/{entityId}")
    public Result<Boolean> existsWithTime(
            @Parameter(description = "排行榜键") @PathVariable String rankingKey,
            @Parameter(description = "实体ID") @PathVariable String entityId,
            @Parameter(description = "有效时间点") @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") Date validTime) {
        boolean exists = rankingUtil.exists(rankingKey, entityId, validTime);
        return Result.success(exists);
    }

    @Operation(summary = "批量检查实体是否存在", description = "批量检查多个实体在排行榜中是否存在数据")
    @PostMapping("/exists-batch/{rankingKey}")
    public Result<List<String>> existsBatch(
            @Parameter(description = "排行榜键") @PathVariable String rankingKey,
            @Parameter(description = "实体ID列表") @RequestBody List<String> entityIds) {
        List<String> existingEntities = rankingUtil.existsBatch(rankingKey, entityIds);
        return Result.success(existingEntities);
    }

    @Operation(summary = "清理过期数据", description = "清理指定时间点之前的排行榜数据")
    @DeleteMapping("/cleanup/{rankingKey}")
    public Result<Void> cleanupOldData(
            @Parameter(description = "排行榜键") @PathVariable String rankingKey,
            @Parameter(description = "过期时间点") @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") Date expireBefore) {
        rankingUtil.cleanupOldData(rankingKey, expireBefore);
        return Result.success();
    }
}