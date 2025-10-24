package io.charlie.web.oj.modular.data.ranking.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.charlie.galaxy.result.Result;
import io.charlie.web.oj.modular.data.ranking.UserActivityService;
import io.charlie.web.oj.modular.data.ranking.param.RankingPageParam;
import io.charlie.web.oj.modular.data.ranking.data.PageResult;
import io.charlie.web.oj.modular.data.ranking.data.RankItem;
import io.charlie.web.oj.modular.data.ranking.service.ProblemCacheService;
import io.charlie.web.oj.modular.data.ranking.service.UserCacheService;
import io.charlie.web.oj.modular.data.ranking.service.UserRankService;
import io.charlie.web.oj.modular.data.submit.entity.DataSubmit;
import io.charlie.web.oj.modular.data.submit.mapper.DataSubmitMapper;
import io.charlie.web.oj.modular.sys.user.entity.SysUser;
import io.charlie.web.oj.modular.sys.user.param.SysUserIdParam;
import io.charlie.web.oj.modular.sys.user.service.SysUserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@Tag(name = "排行榜控制器")
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/api/v1")
@RestController
@Validated
public class RankController {
    private final UserCacheService userCacheService;
    private final ProblemCacheService problemCacheService;
    private final SysUserService sysUserService;
    private final DataSubmitMapper dataSubmitMapper;


    @Autowired
    private UserActivityService userActivityService;

    private final UserRankService userRankService;

    @Operation(summary = "获取用户排行榜")
    @GetMapping("/data/user/rank/top")
    public Result<?> getUserTopN() {
        return Result.success(userRankService.getTopNRankingUsers(10));
    }

    @GetMapping("/data/user/rank/active/top")
    public Result<?> getUserRankActiveTop() {
//        List<RankItem> userRankTopN = userCacheService.getUserActivityTopN(10);
//        List<SysUser> sysUsers = new ArrayList<>();
//        for (RankItem rankItem : userRankTopN) {
//            SysUserIdParam sysUserIdParam = new SysUserIdParam();
//            sysUserIdParam.setId(rankItem.getId());
//            SysUser sysUser = sysUserService.appDetail(sysUserIdParam);
//            sysUser.setRank(rankItem.getRank());
//            // TODO
////            sysUser.setScore(userCacheService.getUserActivity(sysUser.getId()));
//            Long l = dataSubmitMapper.selectCount(new LambdaQueryWrapper<DataSubmit>()
//                    .eq(DataSubmit::getUserId, sysUser.getId())
//                    .eq(DataSubmit::getIsSet, false)
//                    .eq(DataSubmit::getSubmitType, true)
//            );
//            sysUser.setSubmitCount(l);
//            sysUsers.add(sysUser);
//        }
//
//        return Result.success(sysUsers);
        return Result.success(userActivityService.getTopNActiveUsers(10));
    }

    @GetMapping("/data/user/rank/page")
    public Result<?> getUserRankPage(@ParameterObject RankingPageParam rankingPageParam) {
        return Result.success(userRankService.getRankingPage(rankingPageParam));

    }

//    /**
//     * 触发活跃度计算（登录、提交等操作后调用）
//     */
//    @PostMapping("/trigger")
//    public Result<Boolean> triggerActivity(@RequestParam String actionType,
//                                           @RequestParam(required = false) Boolean isSolved) {
//        // 从登录上下文获取用户ID
//        String userId = SecurityUtils.getCurrentUserId();
//        if (userId == null) {
//            return Result.error("用户未登录");
//        }
//
//        userActivityService.addActivity(userId, actionType,
//                isSolved != null ? isSolved : false);
//        return Result.success(true);
//    }
//
//    /**
//     * 获取当前用户活跃度
//     */
//    @GetMapping("/my")
//    public Result<Map<String, Object>> getMyActivity() {
//        String userId = SecurityUtils.getCurrentUserId();
//        BigDecimal score = userActivityService.getUserActivityScore(userId);
//        Long rank = userActivityService.getUserActivityRank(userId);
//
//        Map<String, Object> result = new HashMap<>();
//        result.put("score", score);
//        result.put("rank", rank);
//
//        return Result.success(result);
//    }
//
//    /**
//     * 分页查询活跃用户
//     */
//    @GetMapping("/page")
//    public Result<Page<SysUser>> getActiveUsersPage(
//            @RequestParam(defaultValue = "1") long current,
//            @RequestParam(defaultValue = "10") long size) {
//
//        Page<SysUser> page = new Page<>(current, size);
//        return Result.success(userActivityService.getActiveUsersPage(page));
//    }
//
//    /**
//     * 获取TopN活跃用户
//     */
//    @GetMapping("/top/{n}")
//    public Result<List<SysUser>> getTopNActiveUsers(@PathVariable int n) {
//        return Result.success(userActivityService.getTopNActiveUsers(n));
//    }

}