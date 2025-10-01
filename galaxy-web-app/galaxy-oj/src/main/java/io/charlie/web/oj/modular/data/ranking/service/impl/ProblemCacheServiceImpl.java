package io.charlie.web.oj.modular.data.ranking.service.impl;

import io.charlie.web.oj.modular.data.ranking.data.PageResult;
import io.charlie.web.oj.modular.data.ranking.data.RankItem;
import io.charlie.web.oj.modular.data.ranking.service.ProblemCacheService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

@Service
public class ProblemCacheServiceImpl implements ProblemCacheService {
    
    private static final String PROBLEM_SUBMIT_PREFIX = "problem:submit:";
    private static final String PROBLEM_ACCEPT_PREFIX = "problem:accept:";
    private static final String PROBLEM_PARTICIPANT_PREFIX = "problem:participant:";
    private static final String PROBLEM_ATTEMPT_PREFIX = "problem:attempt:";
    private static final String PROBLEM_RANK_KEY = "problem:rank";
    
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;
    
    @Override
    public void addSubmitUser(String problemId, String userId) {
        String key = PROBLEM_SUBMIT_PREFIX + problemId;
        redisTemplate.opsForSet().add(key, userId);
        
        // 同时添加到参与人数
        addParticipantUser(problemId, userId);
    }
    
    @Override
    public void addAcceptUser(String problemId, String userId) {
        String acceptKey = PROBLEM_ACCEPT_PREFIX + problemId;
        String attemptKey = PROBLEM_ATTEMPT_PREFIX + problemId;
        
        // 添加到通过集合
        redisTemplate.opsForSet().add(acceptKey, userId);
        
        // 从尝试集合中移除（如果用户已通过）
        redisTemplate.opsForSet().remove(attemptKey, userId);
        
        // 更新排行榜分数（通过人数）
        Double score = redisTemplate.opsForSet().size(acceptKey) * 1.0;
        redisTemplate.opsForZSet().add(PROBLEM_RANK_KEY, problemId, score);
    }
    
    @Override
    public void addParticipantUser(String problemId, String userId) {
        String key = PROBLEM_PARTICIPANT_PREFIX + problemId;
        redisTemplate.opsForSet().add(key, userId);
    }
    
    @Override
    public void addAttemptUser(String problemId, String userId) {
        String acceptKey = PROBLEM_ACCEPT_PREFIX + problemId;
        String attemptKey = PROBLEM_ATTEMPT_PREFIX + problemId;
        
        // 检查用户是否已经通过
        Boolean isAccepted = redisTemplate.opsForSet().isMember(acceptKey, userId);
        if (!isAccepted) {
            redisTemplate.opsForSet().add(attemptKey, userId);
        }
    }
    
    @Override
    public Long getSubmitCount(String problemId) {
        String key = PROBLEM_SUBMIT_PREFIX + problemId;
        return redisTemplate.opsForSet().size(key);
    }
    
    @Override
    public Long getAcceptCount(String problemId) {
        String key = PROBLEM_ACCEPT_PREFIX + problemId;
        return redisTemplate.opsForSet().size(key);
    }
    
    @Override
    public Long getParticipantCount(String problemId) {
        String key = PROBLEM_PARTICIPANT_PREFIX + problemId;
        return redisTemplate.opsForSet().size(key);
    }
    
    @Override
    public Long getAttemptCount(String problemId) {
        String key = PROBLEM_ATTEMPT_PREFIX + problemId;
        return redisTemplate.opsForSet().size(key);
    }
    
    @Override
    public Double getAcceptRate(String problemId) {
        Long attemptCount = getAttemptCount(problemId);
        Long acceptCount = getAcceptCount(problemId);
        
        if (attemptCount == 0 && acceptCount == 0) {
            return 0.0;
        }
        
        return attemptCount.doubleValue() / (attemptCount + acceptCount);
    }
    
    @Override
    public List<RankItem> getProblemRankTopN(int n) {
        Set<ZSetOperations.TypedTuple<Object>> tuples =
            redisTemplate.opsForZSet().reverseRangeWithScores(PROBLEM_RANK_KEY, 0, n - 1);
        
        return convertToRankItems(tuples);
    }
    
    @Override
    public PageResult<RankItem> getProblemRankPage(int page, int size) {
        long start = (page - 1) * size;
        long end = start + size - 1;
        
        Set<ZSetOperations.TypedTuple<Object>> tuples = 
            redisTemplate.opsForZSet().reverseRangeWithScores(PROBLEM_RANK_KEY, start, end);
        
        Long total = redisTemplate.opsForZSet().size(PROBLEM_RANK_KEY);
        Long totalPages = (total + size - 1) / size;
        
        List<RankItem> items = convertToRankItems(tuples, start + 1);
        
        return new PageResult<>(items, total, page, size, totalPages);
    }
    
    @Override
    public void resetProblemCache(String problemId) {
        String[] keys = {
            PROBLEM_SUBMIT_PREFIX + problemId,
            PROBLEM_ACCEPT_PREFIX + problemId,
            PROBLEM_PARTICIPANT_PREFIX + problemId,
            PROBLEM_ATTEMPT_PREFIX + problemId
        };
        
        redisTemplate.delete(Arrays.asList(keys));
        redisTemplate.opsForZSet().remove(PROBLEM_RANK_KEY, problemId);
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