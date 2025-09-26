package io.charlie.web.oj.modular.data.ranking.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.charlie.galaxy.utils.ranking.RankingInfo;
import io.charlie.galaxy.utils.ranking.RankingUtil;
import io.charlie.web.oj.modular.data.ranking.UserRankingPageParam;
import io.charlie.web.oj.modular.data.ranking.enums.RankingEnums;
import io.charlie.web.oj.modular.data.ranking.service.UserRankingService;
import io.charlie.web.oj.modular.data.submit.entity.DataSubmit;
import io.charlie.web.oj.modular.sys.user.entity.SysUser;
import io.charlie.web.oj.modular.sys.user.mapper.SysUserMapper;
import io.charlie.web.oj.modular.task.judge.enums.JudgeStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

/**
 * @author ZhangJiangHu
 * @version v1.0
 * @date 21/09/2025
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserRankingServiceImpl implements UserRankingService {
    private static final String RANKING_KEY = "oj:ta:ranking";
    private static final String USER_ACCEPTED_PROBLEMS = "oj:ta:user:accepted:";

    private final RedisTemplate<String, Object> redisTemplate;
    private final RankingUtil rankingUtil;
    private final SysUserMapper sysUserMapper;

    @Override
    public void processSubmission(DataSubmit dataSubmit) {
        if (!dataSubmit.getStatus().equals(JudgeStatus.ACCEPTED.getValue())) {
            log.info("提交不是AC，不处理");
            return; // 只处理AC的提交
        }

        String userId = dataSubmit.getUserId();
        String problemId = dataSubmit.getProblemId();

        // 检查用户是否已经AC过此题
        String userAcceptedKey = USER_ACCEPTED_PROBLEMS + userId;
        Boolean hasAccepted = redisTemplate.opsForSet().isMember(userAcceptedKey, problemId);
        if (Boolean.TRUE.equals(hasAccepted)) {
            log.info("用户已经AC过此题，不再处理");
            return; // 已经AC过此题，不再计分
        }

        // 记录用户AC的题目
        redisTemplate.opsForSet().add(userAcceptedKey, problemId);

        // 增加用户分数
        redisTemplate.opsForZSet().incrementScore(RANKING_KEY, userId, 1);
    }

    @Override
    public List<SysUser> getTopUsers(int limit) {
        Set<ZSetOperations.TypedTuple<Object>> result = redisTemplate.opsForZSet()
                .reverseRangeWithScores(RANKING_KEY, 0, limit - 1);
        return convertToUserRankList(result, 0);
    }

    @Override
    public SysUser getUserRank(String userId) {
        Double score = redisTemplate.opsForZSet().score(RANKING_KEY, userId);
        Long rank = redisTemplate.opsForZSet().reverseRank(RANKING_KEY, userId);

        if (score != null && rank != null) {
            SysUser sysUser = sysUserMapper.selectOne(new LambdaQueryWrapper<SysUser>()
                    .select(SysUser::getAvatar, SysUser::getNickname, SysUser::getId)
                    .eq(SysUser::getId, userId)
            );

            sysUser.setScore(score);
            sysUser.setRank(rank + 1); // Redis排名从0开始，显示排名从1开始

            return sysUser;
        }

        return null;
    }

    @Override
    public Page<SysUser> getRankingPage(UserRankingPageParam userRankingPageParam) {
        // 计算起始和结束索引
        long start = (long) (userRankingPageParam.getCurrent() - 1) * userRankingPageParam.getSize();
        long end = start + userRankingPageParam.getSize() - 1;

        // 获取总用户数
        Long totalUsers = redisTemplate.opsForZSet().zCard(RANKING_KEY);
        if (totalUsers == null) {
            totalUsers = 0L;
        }

        // 获取当前页的用户数据
        Set<ZSetOperations.TypedTuple<Object>> result = redisTemplate.opsForZSet()
                .reverseRangeWithScores(RANKING_KEY, start, end);

        List<SysUser> userRanks = convertToUserRankList(result, start);

        Page<SysUser> sysUserPage = new Page<>();
        sysUserPage.setRecords(userRanks);
        sysUserPage.setTotal(totalUsers);
        sysUserPage.setCurrent(userRankingPageParam.getCurrent());
        sysUserPage.setSize(userRankingPageParam.getSize());
        long totalPages = (totalUsers + userRankingPageParam.getSize() - 1) / userRankingPageParam.getSize();
        sysUserPage.setPages((int) totalPages);
        return sysUserPage;
    }

    @Override
    public List<SysUser> getActiveTop(int limit) {
        List<RankingInfo> topNRanking = rankingUtil.getTopNRanking(RankingEnums.REALTIME_ACTIVE.getValue(), limit);
        List<SysUser> sysUsers = new ArrayList<>();
        for (RankingInfo rankingInfo : topNRanking) {
            SysUser sysUser = sysUserMapper.selectOne(new LambdaQueryWrapper<SysUser>()
                    .select(SysUser::getAvatar, SysUser::getNickname, SysUser::getId)
                    .eq(SysUser::getId, rankingInfo.getEntityId())
            );
            sysUser.setScore(rankingInfo.getScore());
            sysUser.setRank(rankingInfo.getRank());
            sysUsers.add(sysUser);
        }
        return sysUsers;
    }

    @Override
    public Boolean isSolved(String userId, String problemId) {
        String userAcceptedKey = USER_ACCEPTED_PROBLEMS + userId;
        return redisTemplate.opsForSet().isMember(userAcceptedKey, problemId);
    }

    private List<SysUser> convertToUserRankList(Set<ZSetOperations.TypedTuple<Object>> result, long startRank) {
        if (result == null) {
            return Collections.emptyList();
        }

        List<SysUser> rankings = new ArrayList<>();
        long rank = startRank + 1; // Redis排名从0开始，显示排名从1开始

        for (ZSetOperations.TypedTuple<Object> tuple : result) {
            String userId = (String) tuple.getValue();
            Double score = tuple.getScore();

            SysUser sysUser = sysUserMapper.selectOne(new LambdaQueryWrapper<SysUser>()
                    .select(SysUser::getAvatar, SysUser::getNickname, SysUser::getId)
                    .eq(SysUser::getId, userId)
            );

            sysUser.setScore(score);
            sysUser.setRank(rank);
            RankingInfo ranking = rankingUtil.getRanking(RankingEnums.REALTIME_SUBMIT.getValue(), userId);
            if (ranking != null) {
                sysUser.setSubmitCount(ranking.getScore());
            }

            rankings.add(sysUser);

            rank++;
        }

        return rankings;
    }
}
