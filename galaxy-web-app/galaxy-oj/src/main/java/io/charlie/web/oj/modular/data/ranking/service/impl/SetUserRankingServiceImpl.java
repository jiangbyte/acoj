package io.charlie.web.oj.modular.data.ranking.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.charlie.galaxy.utils.ranking.RankingInfo;
import io.charlie.galaxy.utils.ranking.RankingUtil;
import io.charlie.web.oj.modular.data.ranking.UserRankingPageParam;
import io.charlie.web.oj.modular.data.ranking.enums.RankingEnums;
import io.charlie.web.oj.modular.data.ranking.service.SetUserRankingService;
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
 * @date 24/09/2025
 * @description TODO
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SetUserRankingServiceImpl implements SetUserRankingService {
    private static final String RANKING_KEY = "oj:ta:set:ranking";
    private static final String USER_ACCEPTED_PROBLEMS = "oj:ta:set:user:accepted:";

    private final RedisTemplate<String, Object> redisTemplate;
    private final RankingUtil rankingUtil;
    private final SysUserMapper sysUserMapper;

    // 生成包含setId的排名键
    private String getRankingKey(String setId) {
        return RANKING_KEY + setId;
    }

    // 生成包含setId的用户AC题目键
    private String getUserAcceptedKey(String setId, String userId) {
        return USER_ACCEPTED_PROBLEMS + setId + ":" + userId;
    }

    @Override
    public void processSubmission(DataSubmit dataSubmit) {
        if (!dataSubmit.getStatus().equals(JudgeStatus.ACCEPTED.getValue())) {
            log.info("提交不是AC，不处理");
            return; // 只处理AC的提交
        }

        String userId = dataSubmit.getUserId();
        String problemId = dataSubmit.getProblemId();
        String setId = dataSubmit.getSetId(); // 新增setId字段

        // 检查用户是否已经AC过此题
        String userAcceptedKey = getUserAcceptedKey(setId, userId);
        Boolean hasAccepted = redisTemplate.opsForSet().isMember(userAcceptedKey, problemId);
        if (Boolean.TRUE.equals(hasAccepted)) {
            log.info("用户已经AC过此题，不再处理");
            return; // 已经AC过此题，不再计分
        }

        // 记录用户AC的题目
        redisTemplate.opsForSet().add(userAcceptedKey, problemId);

        // 增加用户分数
        String rankingKey = getRankingKey(setId);
        redisTemplate.opsForZSet().incrementScore(rankingKey, userId, 1);
    }

    @Override
    public List<SysUser> getTopUsers(String setId, int limit) {
        String rankingKey = getRankingKey(setId);
        Set<ZSetOperations.TypedTuple<Object>> result = redisTemplate.opsForZSet()
                .reverseRangeWithScores(rankingKey, 0, limit - 1);
        return convertToUserRankList(result, 0);
    }

    @Override
    public SysUser getUserRank(String setId, String userId) {
        String rankingKey = getRankingKey(setId);
        Double score = redisTemplate.opsForZSet().score(rankingKey, userId);
        Long rank = redisTemplate.opsForZSet().reverseRank(rankingKey, userId);

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
    public Page<SysUser> getRankingPage(String setId, UserRankingPageParam userRankingPageParam) {
        String rankingKey = getRankingKey(setId);

        // 计算起始和结束索引
        long start = (long) (userRankingPageParam.getCurrent() - 1) * userRankingPageParam.getSize();
        long end = start + userRankingPageParam.getSize() - 1;

        // 获取总用户数
        Long totalUsers = redisTemplate.opsForZSet().zCard(rankingKey);
        if (totalUsers == null) {
            totalUsers = 0L;
        }

        // 获取当前页的用户数据
        Set<ZSetOperations.TypedTuple<Object>> result = redisTemplate.opsForZSet()
                .reverseRangeWithScores(rankingKey, start, end);

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
        List<RankingInfo> topNRanking = rankingUtil.getTopNRanking(RankingEnums.REALTIME_SET_ACTIVE.getValue(), limit);
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
    public Boolean isSolved(String setId, String userId, String problemId) {
        String userAcceptedKey = getUserAcceptedKey(setId, userId);
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
            RankingInfo ranking = rankingUtil.getRanking(RankingEnums.REALTIME_SET_SUBMIT.getValue(), userId);
            sysUser.setSubmitCount(ranking.getScore());

            rankings.add(sysUser);

            rank++;
        }

        return rankings;
    }
}
