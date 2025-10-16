package io.charlie.web.oj.modular.cache.solved.service.bc;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * @author ZhangJiangHu
 * @version v1.0
 * @date 16/10/2025
 * @description 用户解决题目缓存记录服务，第一次是记录，后面是状态的改变
 */
@Service
@RequiredArgsConstructor
public class UserSolvedCacheRecordService {

    private final RedisTemplate<String, Object> redisTemplate;

    // Redis Key 前缀定义
    private static final String USER_SOLVED_PREFIX = "oj:user:solved:"; // 用户已AC题目集合 (Sorted Set)
    private static final String USER_ATTEMPTED_PREFIX = "oj:user:attempted:"; // 用户尝试过但未AC题目集合 (Set)
    private static final String PROBLEM_SOLVED_PREFIX = "oj:problem:solved:"; // 题目被哪些用户解决 (Set)
    private static final String PROBLEM_ATTEMPTED_PREFIX = "oj:problem:attempted:"; // 题目被哪些用户尝试过 (Set)
    private static final String USER_SOLVED_TIMESTAMP_PREFIX = "oj:user:solved:ts:"; // 用户AC题目时间戳 (Hash)

    // 缓存过期时间（7天）
    private static final long CACHE_EXPIRE_DAYS = 7;

    /**
     * 增加用户解决记录（初始化）
     * 用户第一次提交某题目时调用，记录尝试记录
     */
    public void addUserSolvedRecord(String userId, String problemId) {
        String userAttemptedKey = USER_ATTEMPTED_PREFIX + userId;
        String problemAttemptedKey = PROBLEM_ATTEMPTED_PREFIX + problemId;

        // 如果用户已经AC了这道题，不再记录尝试
        if (queryUserSolved(userId, problemId)) {
            return;
        }

        // 添加到用户尝试集合
        redisTemplate.opsForSet().add(userAttemptedKey, problemId);

        // 添加到题目被尝试集合
        redisTemplate.opsForSet().add(problemAttemptedKey, userId);

        // 设置过期时间
        redisTemplate.expire(userAttemptedKey, CACHE_EXPIRE_DAYS, TimeUnit.DAYS);
        redisTemplate.expire(problemAttemptedKey, CACHE_EXPIRE_DAYS, TimeUnit.DAYS);
    }

    /**
     * 处理用户解决记录AC情况，用户通过记录为通过（提交后判断，AC就执行）
     */
    public void handleUserSolvedRecord(String userId, String problemId) {
        String userSolvedKey = USER_SOLVED_PREFIX + userId;
        String userAttemptedKey = USER_ATTEMPTED_PREFIX + userId;
        String problemSolvedKey = PROBLEM_SOLVED_PREFIX + problemId;
        String problemAttemptedKey = PROBLEM_ATTEMPTED_PREFIX + problemId;
        String userSolvedTimestampKey = USER_SOLVED_TIMESTAMP_PREFIX + userId;

        long currentTime = System.currentTimeMillis();

        // 添加到用户已解决集合（Sorted Set，分数为时间戳用于排序）
        redisTemplate.opsForZSet().add(userSolvedKey, problemId, currentTime);

        // 记录AC时间戳
        redisTemplate.opsForHash().put(userSolvedTimestampKey, problemId, String.valueOf(currentTime));

        // 从尝试集合中移除（如果存在）
        redisTemplate.opsForSet().remove(userAttemptedKey, problemId);

        // 添加到题目被解决集合
        redisTemplate.opsForSet().add(problemSolvedKey, userId);

        // 设置过期时间
        redisTemplate.expire(userSolvedKey, CACHE_EXPIRE_DAYS, TimeUnit.DAYS);
        redisTemplate.expire(problemSolvedKey, CACHE_EXPIRE_DAYS, TimeUnit.DAYS);
        redisTemplate.expire(userSolvedTimestampKey, CACHE_EXPIRE_DAYS, TimeUnit.DAYS);
    }

    /**
     * 获取某个用户的所有已通过AC题目列表（按时间排序返回列表）
     */
    public List<String> getUserSolvedCount(String userId) {
        String userSolvedKey = USER_SOLVED_PREFIX + userId;

        // 使用ZSet按分数（时间戳）升序排列获取所有题目
        Set<ZSetOperations.TypedTuple<Object>> tuples = redisTemplate.opsForZSet()
                .rangeWithScores(userSolvedKey, 0, -1);

        if (tuples == null || tuples.isEmpty()) {
            return new ArrayList<>();
        }

        // 按时间戳排序后返回题目ID列表
        return tuples.stream()
                .sorted(Comparator.comparingDouble(ZSetOperations.TypedTuple::getScore))
                .map(tuple -> (String) tuple.getValue())
                .collect(Collectors.toList());
    }

    /**
     * 获取用户有解决记录，但是没有AC的题目列表
     */
    public List<String> getUserUnsolvedCount(String userId) {
        String userAttemptedKey = USER_ATTEMPTED_PREFIX + userId;

        Set<Object> attemptedProblems = redisTemplate.opsForSet().members(userAttemptedKey);

        if (attemptedProblems == null || attemptedProblems.isEmpty()) {
            return new ArrayList<>();
        }

        return attemptedProblems.stream()
                .map(problemId -> (String) problemId)
                .collect(Collectors.toList());
    }

    /**
     * 查询用户这个题目是否已解决
     */
    public boolean queryUserSolved(String userId, String problemId) {
        String userSolvedKey = USER_SOLVED_PREFIX + userId;

        Double score = redisTemplate.opsForZSet().score(userSolvedKey, problemId);
        return score != null;
    }

    /**
     * 反向查询，解决这个题目的用户集合
     */
    public Set<String> queryUserSolvedByProblemId(String problemId) {
        String problemSolvedKey = PROBLEM_SOLVED_PREFIX + problemId;

        Set<Object> users = redisTemplate.opsForSet().members(problemSolvedKey);

        if (users == null) {
            return new HashSet<>();
        }

        return users.stream()
                .map(userId -> (String) userId)
                .collect(Collectors.toSet());
    }

    /**
     * 获得这个用户的解决通过率
     */
    public BigDecimal getUserSolvedRate(String userId) {
        String userSolvedKey = USER_SOLVED_PREFIX + userId;
        String userAttemptedKey = USER_ATTEMPTED_PREFIX + userId;

        // 获取已AC题目数量
        Long solvedCount = redisTemplate.opsForZSet().size(userSolvedKey);
        if (solvedCount == null) solvedCount = 0L;

        // 获取尝试过但未AC题目数量
        Long attemptedCount = redisTemplate.opsForSet().size(userAttemptedKey);
        if (attemptedCount == null) attemptedCount = 0L;

        // 总尝试题目数 = 已AC数 + 未AC数
        long totalAttempted = solvedCount + attemptedCount;

        if (totalAttempted == 0) {
            return BigDecimal.ZERO;
        }

        // 计算通过率并保留4位小数
        double rate = (double) solvedCount / totalAttempted;
        return BigDecimal.valueOf(rate)
                .setScale(4, RoundingMode.HALF_UP);
    }

    /**
     * 获得这个题目的解决通过率
     */
    public BigDecimal getProblemSolvedRate(String problemId) {
        String problemSolvedKey = PROBLEM_SOLVED_PREFIX + problemId;
        String problemAttemptedKey = PROBLEM_ATTEMPTED_PREFIX + problemId;

        // 获取解决此题的用户数量
        Long solvedCount = redisTemplate.opsForSet().size(problemSolvedKey);
        if (solvedCount == null) solvedCount = 0L;

        // 获取尝试过此题的用户数量
        Long attemptedCount = redisTemplate.opsForSet().size(problemAttemptedKey);
        if (attemptedCount == null) attemptedCount = 0L;

        // 总尝试用户数 = 已解决数 + 尝试但未解决数
        long totalAttempted = solvedCount + attemptedCount;

        if (totalAttempted == 0) {
            return BigDecimal.ZERO;
        }

        // 计算通过率并保留4位小数
        double rate = (double) solvedCount / totalAttempted;
        return BigDecimal.valueOf(rate)
                .setScale(4, RoundingMode.HALF_UP);
    }

    /**
     * 获取用户AC题目数量
     */
    public Long getUserSolvedCountNumber(String userId) {
        String userSolvedKey = USER_SOLVED_PREFIX + userId;
        Long count = redisTemplate.opsForZSet().size(userSolvedKey);
        return count != null ? count : 0L;
    }

    /**
     * 获取用户尝试但未AC题目数量
     */
    public Long getUserUnsolvedCountNumber(String userId) {
        String userAttemptedKey = USER_ATTEMPTED_PREFIX + userId;
        Long count = redisTemplate.opsForSet().size(userAttemptedKey);
        return count != null ? count : 0L;
    }

    /**
     * 清除用户的所有记录（用于测试或用户重置）
     */
    public void clearUserRecords(String userId) {
        String userSolvedKey = USER_SOLVED_PREFIX + userId;
        String userAttemptedKey = USER_ATTEMPTED_PREFIX + userId;
        String userSolvedTimestampKey = USER_SOLVED_TIMESTAMP_PREFIX + userId;

        redisTemplate.delete(userSolvedKey);
        redisTemplate.delete(userAttemptedKey);
        redisTemplate.delete(userSolvedTimestampKey);
    }
}