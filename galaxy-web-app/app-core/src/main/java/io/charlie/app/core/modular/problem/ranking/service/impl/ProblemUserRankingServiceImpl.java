package io.charlie.app.core.modular.problem.ranking.service.impl;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import io.charlie.app.core.modular.problem.ranking.entity.ProblemUserRanking;
import io.charlie.app.core.modular.problem.ranking.mapper.ProblemUserRankingMapper;
import io.charlie.app.core.modular.problem.ranking.param.ActiveUser;
import io.charlie.app.core.modular.problem.ranking.param.ProblemUserRankingPageParam;
import io.charlie.app.core.modular.problem.ranking.param.UserActivityRank;
import io.charlie.app.core.modular.problem.ranking.service.ProblemUserRankingService;
import io.charlie.app.core.modular.sys.user.entity.SysUser;
import io.charlie.app.core.modular.sys.user.service.SysUserService;
import io.charlie.galaxy.enums.ISortOrderEnum;
import io.charlie.galaxy.pojo.CommonPageRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * @author Charlie Zhang
 * @version v1.0
 * @date 28/07/2025
 * @description 提交排行榜服务实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ProblemUserRankingServiceImpl extends ServiceImpl<ProblemUserRankingMapper, ProblemUserRanking> implements ProblemUserRankingService {
    private final RedisTemplate<String, Object> redisTemplate;
    private final SysUserService sysUserService;

    @Override
    public Page<ProblemUserRanking> totalRankingPage(ProblemUserRankingPageParam problemRankingPageParam) {
        QueryWrapper<ProblemUserRanking> queryWrapper = new QueryWrapper<ProblemUserRanking>().checkSqlInjection();
        // 关键字
        if (ObjectUtil.isNotEmpty(problemRankingPageParam.getKeyword())) {
            queryWrapper.lambda().like(ProblemUserRanking::getNickname, problemRankingPageParam.getKeyword());
        }

        if (ObjectUtil.isAllNotEmpty(problemRankingPageParam.getSortField(), problemRankingPageParam.getSortOrder()) && ISortOrderEnum.isValid(problemRankingPageParam.getSortOrder())) {
            queryWrapper.orderBy(
                    true,
                    problemRankingPageParam.getSortOrder().equals(ISortOrderEnum.ASCEND.getValue()),
                    StrUtil.toUnderlineCase(problemRankingPageParam.getSortField()));
        }

        return (Page<ProblemUserRanking>) this.baseMapper.selectTotalRankingPage(CommonPageRequest.Page(
                Optional.ofNullable(problemRankingPageParam.getCurrent()).orElse(1),
                Optional.ofNullable(problemRankingPageParam.getSize()).orElse(20),
                null
        ), queryWrapper);
    }

    @Override
    public Page<ProblemUserRanking> totalRankingPageWithParam(ProblemUserRankingPageParam problemRankingPageParam) {
        QueryWrapper<ProblemUserRanking> queryWrapper = new QueryWrapper<ProblemUserRanking>().checkSqlInjection();
        // 关键字
        if (ObjectUtil.isNotEmpty(problemRankingPageParam.getKeyword())) {
            queryWrapper.lambda().like(ProblemUserRanking::getNickname, problemRankingPageParam.getKeyword());
        }

        if (ObjectUtil.isAllNotEmpty(problemRankingPageParam.getSortField(), problemRankingPageParam.getSortOrder()) && ISortOrderEnum.isValid(problemRankingPageParam.getSortOrder())) {
            queryWrapper.orderBy(
                    true,
                    problemRankingPageParam.getSortOrder().equals(ISortOrderEnum.ASCEND.getValue()),
                    StrUtil.toUnderlineCase(problemRankingPageParam.getSortField()));
        }

        return (Page<ProblemUserRanking>) this.baseMapper.selectTotalRankingPageWithParam(CommonPageRequest.Page(
                Optional.ofNullable(problemRankingPageParam.getCurrent()).orElse(1),
                Optional.ofNullable(problemRankingPageParam.getSize()).orElse(20),
                null
        ), queryWrapper, problemRankingPageParam);
    }

    @Override
    public Page<ProblemUserRanking> monthlyRankingPage(ProblemUserRankingPageParam problemRankingPageParam) {
        QueryWrapper<ProblemUserRanking> queryWrapper = new QueryWrapper<ProblemUserRanking>().checkSqlInjection();
        // 关键字
        if (ObjectUtil.isNotEmpty(problemRankingPageParam.getKeyword())) {
            queryWrapper.lambda().like(ProblemUserRanking::getNickname, problemRankingPageParam.getKeyword());
        }

        if (ObjectUtil.isAllNotEmpty(problemRankingPageParam.getSortField(), problemRankingPageParam.getSortOrder()) && ISortOrderEnum.isValid(problemRankingPageParam.getSortOrder())) {
            queryWrapper.orderBy(
                    true,
                    problemRankingPageParam.getSortOrder().equals(ISortOrderEnum.ASCEND.getValue()),
                    StrUtil.toUnderlineCase(problemRankingPageParam.getSortField()));
        }

        return (Page<ProblemUserRanking>) this.baseMapper.selectMonthlyRankingPage(CommonPageRequest.Page(
                Optional.ofNullable(problemRankingPageParam.getCurrent()).orElse(1),
                Optional.ofNullable(problemRankingPageParam.getSize()).orElse(20),
                null
        ), queryWrapper);
    }

    @Override
    public Page<ProblemUserRanking> weeklyRankingPage(ProblemUserRankingPageParam problemRankingPageParam) {
        QueryWrapper<ProblemUserRanking> queryWrapper = new QueryWrapper<ProblemUserRanking>().checkSqlInjection();
        // 关键字
        if (ObjectUtil.isNotEmpty(problemRankingPageParam.getKeyword())) {
            queryWrapper.lambda().like(ProblemUserRanking::getNickname, problemRankingPageParam.getKeyword());
        }

        if (ObjectUtil.isAllNotEmpty(problemRankingPageParam.getSortField(), problemRankingPageParam.getSortOrder()) && ISortOrderEnum.isValid(problemRankingPageParam.getSortOrder())) {
            queryWrapper.orderBy(
                    true,
                    problemRankingPageParam.getSortOrder().equals(ISortOrderEnum.ASCEND.getValue()),
                    StrUtil.toUnderlineCase(problemRankingPageParam.getSortField()));
        }

        return (Page<ProblemUserRanking>) this.baseMapper.selectWeeklyRankingPage(CommonPageRequest.Page(
                Optional.ofNullable(problemRankingPageParam.getCurrent()).orElse(1),
                Optional.ofNullable(problemRankingPageParam.getSize()).orElse(20),
                null
        ), queryWrapper);
    }

    @Override
    public Page<ProblemUserRanking> dailyRankingPage(ProblemUserRankingPageParam problemRankingPageParam) {
        QueryWrapper<ProblemUserRanking> queryWrapper = new QueryWrapper<ProblemUserRanking>().checkSqlInjection();
        // 关键字
        if (ObjectUtil.isNotEmpty(problemRankingPageParam.getKeyword())) {
            queryWrapper.lambda().like(ProblemUserRanking::getNickname, problemRankingPageParam.getKeyword());
        }

        if (ObjectUtil.isAllNotEmpty(problemRankingPageParam.getSortField(), problemRankingPageParam.getSortOrder()) && ISortOrderEnum.isValid(problemRankingPageParam.getSortOrder())) {
            queryWrapper.orderBy(
                    true,
                    problemRankingPageParam.getSortOrder().equals(ISortOrderEnum.ASCEND.getValue()),
                    StrUtil.toUnderlineCase(problemRankingPageParam.getSortField()));
        }

        return (Page<ProblemUserRanking>) this.baseMapper.selectDailyRankingPage(CommonPageRequest.Page(
                Optional.ofNullable(problemRankingPageParam.getCurrent()).orElse(1),
                Optional.ofNullable(problemRankingPageParam.getSize()).orElse(20),
                null
        ), queryWrapper);
    }

    @Override
    public ProblemUserRanking totalRankingByUserId(String userId) {
        return this.baseMapper.selectTotalRankingByUserId(userId);
    }

    @Override
    public ProblemUserRanking monthlyRankingByUserId(String userId) {
        return this.baseMapper.selectMonthlyRankingByUserId(userId);
    }

    @Override
    public ProblemUserRanking weeklyRankingByUserId(String userId) {
        return this.baseMapper.selectWeeklyRankingByUserId(userId);
    }

    @Override
    public ProblemUserRanking dailyRankingByUserId(String userId) {
        return this.baseMapper.selectDailyRankingByUserId(userId);
    }

    @Override
    public List<ProblemUserRanking> topN(Integer n) {
        return this.baseMapper.selectTopN(n);
    }

    @Override
    public List<String> getActiveUsersTop10() {
        return getTopNActiveUsers(10);
    }

    @Override
    public List<ActiveUser> getActiveUsersTopN(int n) {
        Map<String, Double> topNActiveUsersWithScores = getTopNActiveUsersWithScores(n);
        // 遍历获取用户信息
        List<ActiveUser> activeUsers = new ArrayList<>();
        for (Map.Entry<String, Double> entry : topNActiveUsersWithScores.entrySet()) {
            String userId = entry.getKey();
            SysUser byId = sysUserService.getById(userId);
            if (ObjectUtil.isNotEmpty(byId)) {
                ActiveUser activeUser = new ActiveUser();
                activeUser.setUserId(byId.getId());
                activeUser.setNickname(byId.getNickname());
                activeUser.setAvatar(byId.getAvatar());
                activeUser.setScore(entry.getValue().toString());
                activeUsers.add(activeUser);
            }
        }
        return activeUsers;
    }

    /**
     * 获取活跃度前N的用户ID（按分数降序）
     */
    public List<String> getTopNActiveUsers(int n) {
        String globalRankKey = "user:activity:global:rank";

        // 使用reverseRangeWithScores获取分数和成员，按分数降序
        Set<ZSetOperations.TypedTuple<Object>> tuples = redisTemplate.opsForZSet()
                .reverseRangeWithScores(globalRankKey, 0, n - 1);

        if (tuples == null || tuples.isEmpty()) {
            return Collections.emptyList();
        }

        List<String> topUsers = new ArrayList<>();
        for (ZSetOperations.TypedTuple<Object> tuple : tuples) {
            if (tuple.getValue() != null) {
                topUsers.add(tuple.getValue().toString());
                // 如果同时获取分数
//                 double score = tuple.getScore();
//                 System.out.println("用户ID: " + tuple.getValue() + ", 分数: " + score);
            }
        }

        return topUsers;
    }

    /**
     * 获取活跃度前10的用户ID及其分数（按分数降序）
     */
    public Map<String, Double> getTop10ActiveUsersWithScores() {
        return getTopNActiveUsersWithScores(10);
    }

    /**
     * 获取活跃度前N的用户ID及其分数（按分数降序）
     */
    public Map<String, Double> getTopNActiveUsersWithScores(int n) {
        String globalRankKey = "user:activity:global:rank";

        Set<ZSetOperations.TypedTuple<Object>> tuples = redisTemplate.opsForZSet()
                .reverseRangeWithScores(globalRankKey, 0, n - 1);

        if (tuples == null || tuples.isEmpty()) {
            return Collections.emptyMap();
        }

        Map<String, Double> result = new LinkedHashMap<>();
        for (ZSetOperations.TypedTuple<Object> tuple : tuples) {
            if (tuple.getValue() != null && tuple.getScore() != null) {
                result.put(tuple.getValue().toString(), tuple.getScore());
            }
        }

        return result;
    }

    /**
     * 获取指定用户的活跃度排名和分数
     */
    @Override
    public UserActivityRank getUserActivityRank(String userId) {
        String globalRankKey = "user:activity:global:rank";

        Long rank = redisTemplate.opsForZSet().reverseRank(globalRankKey, userId);
        Double score = redisTemplate.opsForZSet().score(globalRankKey, userId);

        UserActivityRank result = new UserActivityRank();
        result.setUserId(userId);
        result.setRank(rank != null ? rank + 1 : null); // 转为1-based排名
        result.setScore(score != null ? score : 0.0);

        return result;
    }


}