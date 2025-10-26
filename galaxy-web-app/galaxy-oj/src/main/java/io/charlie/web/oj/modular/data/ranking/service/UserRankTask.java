package io.charlie.web.oj.modular.data.ranking.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.charlie.web.oj.modular.data.ranking.param.RankingPageParam;
import io.charlie.web.oj.modular.data.solved.mapper.DataSolvedMapper;
import io.charlie.web.oj.modular.sys.user.entity.SysUser;
import io.charlie.web.oj.modular.sys.user.service.SysUserService;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SessionCallback;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@Slf4j
public class UserRankTask {

    @Autowired
    private DataSolvedMapper dataSolvedMapper;

    @Autowired
    private SysUserService sysUserService;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    private static final String USER_RANK_KEY = "aoj:user:rank";
    private static final String USER_RANK_SORTED_SET_KEY = "aoj:user:rank:sorted";
    private static final String USER_STATS_KEY_PREFIX = "aoj:user:stats:";

    @PostConstruct
    public void init() {
        // 应用启动后立即执行
        CompletableFuture.runAsync(this::doSyncUserRank);
    }

    @Scheduled(cron = "0 0/10 * * * ?") // 每10分钟同步一次
    public void syncUserRankToRedis() {
        doSyncUserRank();
    }

    private void doSyncUserRank() {
        try {
            log.info("开始同步用户排行榜数据到Redis");

            // 清空旧的排行榜数据
            redisTemplate.delete(USER_RANK_SORTED_SET_KEY);

            int batchSize = 1000;
            int offset = 0;
            int totalProcessed = 0;

            while (true) {
                // 分批查询用户统计信息
                List<Map<String, Object>> userStats = dataSolvedMapper.selectUserSolveStatisticsBatch(offset, batchSize);

                if (userStats.isEmpty()) {
                    log.info("已处理完成，共处理 {} 个用户", totalProcessed);
                    break;
                }

                log.info("正在处理第 {} 批数据，共 {} 个用户", (offset / batchSize) + 1, userStats.size());

                // 使用Pipeline批量更新Redis
                redisTemplate.executePipelined(new SessionCallback<Object>() {
                    @Override
                    public Object execute(RedisOperations operations) {
                        for (Map<String, Object> stat : userStats) {
                            String userId = (String) stat.get("user_id");
                            Long solvedCount = ((Number) stat.get("solved_count")).longValue();
                            Long submittedCount = ((Number) stat.get("submitted_count")).longValue();
                            Long totalSubmitCount = ((Number) stat.get("total_submit_count")).longValue();
                            BigDecimal acceptanceRate = (BigDecimal) stat.get("acceptance_rate");

                            // 存储用户详细统计信息
                            Map<String, Object> userDetail = new HashMap<>();
                            userDetail.put("userId", userId);
                            userDetail.put("solvedCount", solvedCount);
                            userDetail.put("submittedCount", submittedCount);
                            userDetail.put("totalSubmitCount", totalSubmitCount);
                            userDetail.put("acceptanceRate", acceptanceRate.toString());

                            operations.opsForHash().putAll(USER_STATS_KEY_PREFIX + userId, userDetail);

                            // 使用复合分数进行排序
                            double compositeScore = calculateCompositeScore(solvedCount, acceptanceRate);
                            operations.opsForZSet().add(USER_RANK_SORTED_SET_KEY, userId, compositeScore);
                        }
                        return null;
                    }
                });

                totalProcessed += userStats.size();
                offset += batchSize;

                // 短暂休眠，避免对数据库造成太大压力
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }

            log.info("用户排行榜数据同步完成，共同步 {} 个用户", totalProcessed);
        } catch (Exception e) {
            log.error("同步用户排行榜数据失败", e);
        }
    }

//    private void doSyncUserRank() {
//        try {
//            log.info("开始同步用户排行榜数据到Redis");
//
//            // 从数据库获取用户统计信息
//            List<Map<String, Object>> userStats = dataSolvedMapper.selectUserSolveStatistics();
//
//            // 清空旧的排行榜数据
//            redisTemplate.delete(USER_RANK_SORTED_SET_KEY);
//
//            // 批量更新Redis
//            for (Map<String, Object> stat : userStats) {
//                String userId = (String) stat.get("user_id");
//                Long solvedCount = ((Number) stat.get("solved_count")).longValue();
//                Long submittedCount = ((Number) stat.get("submitted_count")).longValue();
//                Long totalSubmitCount = ((Number) stat.get("total_submit_count")).longValue();
//                BigDecimal acceptanceRate = (BigDecimal) stat.get("acceptance_rate");
//
//                // 存储用户详细统计信息
//                Map<String, Object> userDetail = new HashMap<>();
//                userDetail.put("userId", userId);
//                userDetail.put("solvedCount", solvedCount);
//                userDetail.put("submittedCount", submittedCount);
//                userDetail.put("totalSubmitCount", totalSubmitCount);
//                userDetail.put("acceptanceRate", acceptanceRate.toString());
//
//                redisTemplate.opsForHash().putAll(USER_STATS_KEY_PREFIX + userId, userDetail);
//
//                // 使用复合分数进行排序
//                double compositeScore = calculateCompositeScore(solvedCount, acceptanceRate);
//
//                redisTemplate.opsForZSet().add(USER_RANK_SORTED_SET_KEY, userId, compositeScore);
//            }
//
//            log.info("用户排行榜数据同步完成，共同步 {} 个用户", userStats.size());
//        } catch (Exception e) {
//            log.error("同步用户排行榜数据失败", e);
//        }
//    }

    /**
     * 简化版复合分数
     */
    private double calculateCompositeScore(Long solvedCount, BigDecimal acceptanceRate) {
        // 只需要两个维度：解题数量 + 通过率
        return solvedCount * 1_000_000.0 + acceptanceRate.doubleValue() * 100.0;
    }

    /**
     * 获取排行榜分页数据
     */
    public Page<SysUser> getRankingPage(RankingPageParam param) {
        // 设置默认参数
        int current = param.getCurrent() != null ? param.getCurrent() : 1;
        int size = param.getSize() != null ? param.getSize() : 20;

        // 从Redis获取排行榜用户ID范围
        long start = (long) (current - 1) * size;
        long end = start + size - 1;

        // 获取指定范围的用户ID（按分数从高到低）
        Set<Object> userIds = redisTemplate.opsForZSet().reverseRange(USER_RANK_SORTED_SET_KEY, start, end);

        if (CollectionUtils.isEmpty(userIds)) {
            return new Page<>(current, size, 0);
        }

        // 获取总用户数
        Long totalUsers = redisTemplate.opsForZSet().size(USER_RANK_SORTED_SET_KEY);
        if (totalUsers == null) {
            totalUsers = 0L;
        }

        // 批量获取用户详细信息并填充排行榜数据
        List<SysUser> userList = buildRankingUsersBatch(new ArrayList<>(userIds), start);

        // 构建分页结果
        Page<SysUser> page = new Page<>(current, size, totalUsers);
        page.setRecords(userList);

        return page;
    }

    /**
     * 批量构建排行榜用户列表
     */
    private List<SysUser> buildRankingUsersBatch(List<Object> userIds, long startRank) {
        if (CollectionUtils.isEmpty(userIds)) {
            return Collections.emptyList();
        }

        // 批量获取用户信息
        List<String> userIdList = userIds.stream()
                .map(userId -> (String) userId)
                .collect(Collectors.toList());

        List<SysUser> users = sysUserService.listByIds(userIdList);
        if (CollectionUtils.isEmpty(users)) {
            return Collections.emptyList();
        }

        // 转换为Map便于查找
        Map<String, SysUser> userMap = users.stream()
                .collect(Collectors.toMap(SysUser::getId, Function.identity()));

        List<SysUser> result = new ArrayList<>();
        for (int i = 0; i < userIds.size(); i++) {
            String userId = (String) userIds.get(i);
            SysUser user = userMap.get(userId);
            if (user == null) {
                continue;
            }

            // 逐个获取用户统计信息（因为multiGet返回的是List<Object>，不方便处理）
            Map<Object, Object> stats = redisTemplate.opsForHash().entries(USER_STATS_KEY_PREFIX + userId);

            // 填充排行榜相关字段
            fillRankingData(user, stats, startRank + i + 1);
            result.add(user);
        }

        return result;
    }

    /**
     * 批量构建排行榜用户列表 - 使用Pipeline优化Redis查询
     */
    private List<SysUser> buildRankingUsersBatchWithPipeline(List<Object> userIds, long startRank) {
        if (CollectionUtils.isEmpty(userIds)) {
            return Collections.emptyList();
        }

        // 批量获取用户信息
        List<String> userIdList = userIds.stream()
                .map(userId -> (String) userId)
                .collect(Collectors.toList());

        List<SysUser> users = sysUserService.listByIds(userIdList);
        if (CollectionUtils.isEmpty(users)) {
            return Collections.emptyList();
        }

        // 转换为Map便于查找
        Map<String, SysUser> userMap = users.stream()
                .collect(Collectors.toMap(SysUser::getId, Function.identity()));

        // 使用Pipeline批量获取Redis统计信息
        List<Object> statsResults = redisTemplate.executePipelined(new org.springframework.data.redis.core.SessionCallback<Object>() {
            @Override
            public Object execute(org.springframework.data.redis.core.RedisOperations operations) {
                for (String userId : userIdList) {
                    operations.opsForHash().entries(USER_STATS_KEY_PREFIX + userId);
                }
                return null;
            }
        });

        List<SysUser> result = new ArrayList<>();
        for (int i = 0; i < userIds.size(); i++) {
            String userId = (String) userIds.get(i);
            SysUser user = userMap.get(userId);
            if (user == null) {
                continue;
            }

            // 获取对应的统计信息
            Map<Object, Object> stats = Collections.emptyMap();
            if (i < statsResults.size() && statsResults.get(i) instanceof Map) {
                stats = (Map<Object, Object>) statsResults.get(i);
            }

            // 填充排行榜相关字段
            fillRankingData(user, stats, startRank + i + 1);
            result.add(user);
        }

        return result;
    }

    /**
     * 填充排行榜数据到用户实体
     */
    private void fillRankingData(SysUser user, Map<Object, Object> stats, long rank) {
        // 设置排名
        user.setRank(rank);

        // 设置分数
        Double score = redisTemplate.opsForZSet().score(USER_RANK_SORTED_SET_KEY, user.getId());
        if (score != null) {
//            user.setScore(score);
        }

        // 设置统计信息
        if (stats != null && !stats.isEmpty()) {
            user.setSolvedCount(getLongValue(stats, "solvedCount"));
            user.setSubmittedCount(getLongValue(stats, "submittedCount"));
            user.setTotalSubmitCount(getLongValue(stats, "totalSubmitCount"));
//            user.setAcceptanceRate((BigDecimal) stats.get("acceptanceRate"));
            user.setAcceptanceRate(getBigDecimalValue(stats, "acceptanceRate"));
        }
    }

    /**
     * 获取BigDecimal类型值
     */
    private BigDecimal getBigDecimalValue(Map<Object, Object> map, String key) {
        Object value = map.get(key);
        if (value instanceof BigDecimal) {
            return (BigDecimal) value;
        } else if (value instanceof String) {
            try {
                return new BigDecimal((String) value);
            } catch (NumberFormatException e) {
                log.warn("无法解析BigDecimal值: {}, key: {}", value, key);
                return BigDecimal.ZERO;
            }
        } else if (value instanceof Number) {
            return BigDecimal.valueOf(((Number) value).doubleValue());
        }
        return BigDecimal.ZERO;
    }

    /**
     * 获取Long类型值
     */
    private Long getLongValue(Map<Object, Object> map, String key) {
        Object value = map.get(key);
        if (value instanceof Number) {
            return ((Number) value).longValue();
        } else if (value instanceof String) {
            try {
                return Long.parseLong((String) value);
            } catch (NumberFormatException e) {
                return 0L;
            }
        }
        return 0L;
    }

    /**
     * 获取用户个人排名信息
     */
    public SysUser getUserPersonalRank(String userId) {
        // 获取用户排名（从0开始，所以要+1）
        Long rank = redisTemplate.opsForZSet().reverseRank(USER_RANK_SORTED_SET_KEY, userId);
        if (rank == null) {
            return null; // 用户未上榜
        }

        // 批量获取用户信息（即使单个用户也使用批量接口，保持一致性）
        List<SysUser> users = sysUserService.listByIds(Collections.singletonList(userId));
        if (CollectionUtils.isEmpty(users)) {
            return null;
        }

        SysUser user = users.get(0);

        // 获取用户统计信息
        Map<Object, Object> stats = redisTemplate.opsForHash().entries(USER_STATS_KEY_PREFIX + userId);

        // 填充排行榜数据
        fillRankingData(user, stats, rank + 1);

        return user;
    }

    /**
     * 获取用户排名周围的用户（前后各n个）
     */
    public List<SysUser> getNearbyUsers(String userId, int range) {
        // 获取用户排名
        Long userRank = redisTemplate.opsForZSet().reverseRank(USER_RANK_SORTED_SET_KEY, userId);
        if (userRank == null) {
            return Collections.emptyList();
        }

        long start = Math.max(0, userRank - range);
        long end = userRank + range;

        // 获取前后范围的用户ID
        Set<Object> nearbyUserIds = redisTemplate.opsForZSet().reverseRange(USER_RANK_SORTED_SET_KEY, start, end);
        if (CollectionUtils.isEmpty(nearbyUserIds)) {
            return Collections.emptyList();
        }

        // 批量构建用户列表（使用Pipeline优化版本）
        return buildRankingUsersBatchWithPipeline(new ArrayList<>(nearbyUserIds), start);
    }

    /**
     * 搜索排行榜用户
     */
    public Page<SysUser> searchRankingUsers(RankingPageParam param) {
        if (param.getKeyword() == null || param.getKeyword().trim().isEmpty()) {
            return getRankingPage(param);
        }

        // 先获取所有用户ID
        Set<Object> allUserIds = redisTemplate.opsForZSet().reverseRange(USER_RANK_SORTED_SET_KEY, 0, -1);
        if (CollectionUtils.isEmpty(allUserIds)) {
            return new Page<>(param.getCurrent(), param.getSize(), 0);
        }

        // 批量获取所有用户信息用于搜索过滤
        List<String> allUserIdList = allUserIds.stream()
                .map(userId -> (String) userId)
                .collect(Collectors.toList());

        List<SysUser> allUsers = sysUserService.listByIds(allUserIdList);
        Map<String, SysUser> allUserMap = allUsers.stream()
                .collect(Collectors.toMap(SysUser::getId, Function.identity()));

        // 根据关键词过滤用户
        List<String> filteredUserIds = allUserIdList.stream()
                .filter(userId -> {
                    SysUser user = allUserMap.get(userId);
                    return user != null && matchesKeyword(user, param.getKeyword());
                })
                .collect(Collectors.toList());

        // 手动分页
        int current = param.getCurrent() != null ? param.getCurrent() : 1;
        int size = param.getSize() != null ? param.getSize() : 20;
        int start = (current - 1) * size;
        int end = Math.min(start + size, filteredUserIds.size());

        if (start >= filteredUserIds.size()) {
            return new Page<>(current, size, filteredUserIds.size());
        }

        List<String> pageUserIds = filteredUserIds.subList(start, end);
        List<Object> pageUserIdsObj = new ArrayList<>(pageUserIds);

        // 使用Pipeline优化版本构建用户列表
        List<SysUser> userList = buildRankingUsersBatchWithPipeline(pageUserIdsObj, start);

        Page<SysUser> page = new Page<>(current, size, filteredUserIds.size());
        page.setRecords(userList);

        return page;
    }

    /**
     * 检查用户是否匹配关键词
     */
    private boolean matchesKeyword(SysUser user, String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return true;
        }

        String lowerKeyword = keyword.toLowerCase();
        return (user.getUsername() != null && user.getUsername().toLowerCase().contains(lowerKeyword)) ||
                (user.getNickname() != null && user.getNickname().toLowerCase().contains(lowerKeyword)) ||
                (user.getEmail() != null && user.getEmail().toLowerCase().contains(lowerKeyword)) ||
                (user.getStudentNumber() != null && user.getStudentNumber().toLowerCase().contains(lowerKeyword));
    }

    /**
     * 获取排行榜总人数
     */
    public Long getTotalRankingUsers() {
        Long total = redisTemplate.opsForZSet().size(USER_RANK_SORTED_SET_KEY);
        return total != null ? total : 0L;
    }

    /**
     * 刷新单个用户的排行榜数据
     */
    public void refreshUserRank(String userId) {
        // 从数据库重新查询该用户的统计数据
        Map<String, Object> userStat = dataSolvedMapper.selectUserSolveStatisticsById(userId);
        if (userStat == null || userStat.isEmpty()) {
            // 如果用户没有统计数据，从排行榜中移除
            redisTemplate.opsForZSet().remove(USER_RANK_SORTED_SET_KEY, userId);
            redisTemplate.delete(USER_STATS_KEY_PREFIX + userId);
            return;
        }

        // 更新Redis中的用户数据
        Long solvedCount = ((Number) userStat.get("solved_count")).longValue();
        Long submittedCount = ((Number) userStat.get("submitted_count")).longValue();
        Long totalSubmitCount = ((Number) userStat.get("total_submit_count")).longValue();
        BigDecimal acceptanceRate = (BigDecimal) userStat.get("acceptance_rate");

        // 存储用户详细统计信息
        Map<String, Object> userDetail = new HashMap<>();
        userDetail.put("userId", userId);
        userDetail.put("solvedCount", solvedCount);
        userDetail.put("submittedCount", submittedCount);
        userDetail.put("totalSubmitCount", totalSubmitCount);
        userDetail.put("acceptanceRate", acceptanceRate.toString());

        redisTemplate.opsForHash().putAll(USER_STATS_KEY_PREFIX + userId, userDetail);

        // 更新排行榜分数
        double compositeScore = calculateCompositeScore(solvedCount, acceptanceRate);
        redisTemplate.opsForZSet().add(USER_RANK_SORTED_SET_KEY, userId, compositeScore);
    }


    /**
     * 获取TopN排行榜用户
     */
    public List<SysUser> getTopNRankingUsers(int topN) {
        // 限制最大数量，避免性能问题
        int limit = Math.min(topN, 1000);

        // 从Redis获取前N个用户ID
        Set<Object> userIds = redisTemplate.opsForZSet().reverseRange(USER_RANK_SORTED_SET_KEY, 0, limit - 1);

        if (CollectionUtils.isEmpty(userIds)) {
            return Collections.emptyList();
        }

        // 批量构建用户列表
        return buildRankingUsersBatchWithPipeline(new ArrayList<>(userIds), 0);
    }

    /**
     * 获取TopN排行榜用户（带缓存版本）
     */
    public List<SysUser> getTopNRankingUsersWithCache(int topN) {
        String cacheKey = USER_RANK_KEY + ":top:" + topN;

        // 尝试从缓存获取
        List<SysUser> cachedResult = (List<SysUser>) redisTemplate.opsForValue().get(cacheKey);
        if (cachedResult != null) {
            return cachedResult;
        }

        // 查询数据
        List<SysUser> result = getTopNRankingUsers(topN);

        // 缓存结果，过期时间5分钟
        if (!CollectionUtils.isEmpty(result)) {
            redisTemplate.opsForValue().set(cacheKey, result, Duration.ofMinutes(5));
        }

        return result;
    }
}