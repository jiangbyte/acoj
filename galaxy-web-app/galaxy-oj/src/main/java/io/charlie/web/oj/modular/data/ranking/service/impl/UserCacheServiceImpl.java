package io.charlie.web.oj.modular.data.ranking.service.impl;

import io.charlie.web.oj.modular.data.ranking.data.PageResult;
import io.charlie.web.oj.modular.data.ranking.data.RankItem;
import io.charlie.web.oj.modular.data.ranking.service.UserCacheService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

@Service
public class UserCacheServiceImpl implements UserCacheService {
    
    private static final String USER_ACTIVITY_PREFIX = "user:activity:";
    private static final String USER_ACCEPTED_PREFIX = "user:accepted:";
    private static final String USER_RANK_KEY = "user:rank";
    private static final String USER_ACTIVITY_RANK_KEY = "user:activity:rank";
    private static final String MONTHLY_KEY_SUFFIX = ":monthly";
    
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;
    
    @Override
    public void addUserActivity(String userId, double increment) {
        String key = USER_ACTIVITY_PREFIX + userId + MONTHLY_KEY_SUFFIX;
        redisTemplate.opsForValue().increment(key, increment);

        // 同时更新活跃度排行榜
        redisTemplate.opsForZSet().incrementScore(USER_ACTIVITY_RANK_KEY, userId, increment);
    }
    
    @Override
    public void addUserAcceptedProblem(String userId, String problemId) {
        String key = USER_ACCEPTED_PREFIX + userId;
        redisTemplate.opsForSet().add(key, problemId);
        
        // 更新排行榜分数（通过题目数量）
        Double score = redisTemplate.opsForSet().size(key) * 1.0;
        redisTemplate.opsForZSet().add(USER_RANK_KEY, userId, score);
    }
    
    @Override
    public Double getUserActivity(String userId) {
        String key = USER_ACTIVITY_PREFIX + userId + MONTHLY_KEY_SUFFIX;
        Object value = redisTemplate.opsForValue().get(key);
        return value != null ? Double.parseDouble(value.toString()) : 0.0;
    }

    @Override
    public PageResult<RankItem> getUserActivityPage(int page, int size) {
        long start = (page - 1) * size;
        long end = start + size - 1;

        // 获取活跃度排行榜分页数据
        Set<ZSetOperations.TypedTuple<Object>> tuples =
                redisTemplate.opsForZSet().reverseRangeWithScores(USER_ACTIVITY_RANK_KEY, start, end);

        // 获取总记录数
        Long total = redisTemplate.opsForZSet().size(USER_ACTIVITY_RANK_KEY);
        Long totalPages = total == 0 ? 0L : (total + size - 1) / size;

        // 转换为RankItem列表
        List<RankItem> items = convertToRankItems(tuples, start + 1);

        return new PageResult<>(items, total, page, size, totalPages);
    }

    @Override
    public List<RankItem> getUserActivityTopN(int n) {
        Set<ZSetOperations.TypedTuple<Object>> tuples =
                redisTemplate.opsForZSet().reverseRangeWithScores(USER_ACTIVITY_RANK_KEY, 0, n - 1);

        return convertToRankItems(tuples);
    }

    @Override
    public Long getUserAcceptedCount(String userId) {
        String key = USER_ACCEPTED_PREFIX + userId;
        return redisTemplate.opsForSet().size(key);
    }
    
    @Override
    public List<RankItem> getUserRankTopN(int n) {
        Set<ZSetOperations.TypedTuple<Object>> tuples =
            redisTemplate.opsForZSet().reverseRangeWithScores(USER_RANK_KEY, 0, n - 1);
        
        return convertToRankItems(tuples);
    }
    
    @Override
    public PageResult<RankItem> getUserRankPage(int page, int size) {
        long start = (page - 1) * size;
        long end = start + size - 1;
        
        Set<ZSetOperations.TypedTuple<Object>> tuples = 
            redisTemplate.opsForZSet().reverseRangeWithScores(USER_RANK_KEY, start, end);
        
        Long total = redisTemplate.opsForZSet().size(USER_RANK_KEY);
        Long totalPages = (total + size - 1) / size;
        
        List<RankItem> items = convertToRankItems(tuples, start + 1);
        
        return new PageResult<>(items, total, page, size, totalPages);
    }
    
    @Override
    public void resetMonthlyActivity() {
        // TODO: 实现按月重置活跃指数的逻辑
        // 需要遍历所有用户的活动键并重置
        // 可以使用Redis的keys命令或scan命令找到所有USER_ACTIVITY_PREFIX + "*" + MONTHLY_KEY_SUFFIX的键
        // 然后删除或重置这些键
    }
    
    @Override
    public void resetUserCache(String userId) {
        String[] keys = {
            USER_ACTIVITY_PREFIX + userId + MONTHLY_KEY_SUFFIX,
            USER_ACCEPTED_PREFIX + userId
        };
        
        redisTemplate.delete(Arrays.asList(keys));
        redisTemplate.opsForZSet().remove(USER_RANK_KEY, userId);
    }
    
    private List<RankItem> convertToRankItems(Set<ZSetOperations.TypedTuple<Object>> tuples) {
        return convertToRankItems(tuples, 1);
    }
    
    private List<RankItem> convertToRankItems(Set<ZSetOperations.TypedTuple<Object>> tuples, long startRank) {
        List<RankItem> items = new ArrayList<>();
        long rank = startRank;
        
        for (ZSetOperations.TypedTuple<Object> tuple : tuples) {
            String id = (String) tuple.getValue();
            Double score = tuple.getScore();
            items.add(new RankItem(rank++, score, id));
        }
        
        return items;
    }
}