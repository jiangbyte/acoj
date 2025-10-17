package io.charlie.web.oj.modular.data.ranking.service.impl;

import io.charlie.web.oj.modular.data.ranking.data.PageResult;
import io.charlie.web.oj.modular.data.ranking.data.RankItem;
import io.charlie.web.oj.modular.data.ranking.service.ProblemCacheService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Service;

import java.text.DecimalFormat;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
public class ProblemCacheServiceImpl implements ProblemCacheService {

    private static final String PROBLEM_SUBMIT_PREFIX = "problem:submit:";
    private static final String PROBLEM_ACCEPT_PREFIX = "problem:accept:";
    private static final String PROBLEM_PARTICIPANT_PREFIX = "problem:participant:";
    private static final String PROBLEM_ATTEMPT_PREFIX = "problem:attempt:";
    private static final String PROBLEM_RANK_KEY = "problem:rank";
    private static final String PROBLEM_PARTICIPANT_RANK_KEY = "problem:participant:rank";

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
        Boolean isNew = redisTemplate.opsForSet().add(key, userId) == 1;

        // 如果是新用户参与，更新参与人数排行榜
        if (Boolean.TRUE.equals(isNew)) {
            Double participantCount = redisTemplate.opsForSet().size(key) * 1.0;
            redisTemplate.opsForZSet().add(PROBLEM_PARTICIPANT_RANK_KEY, problemId, participantCount);
        }
    }

    @Override
    public void addAttemptUser(String problemId, String userId) {
        String acceptKey = PROBLEM_ACCEPT_PREFIX + problemId;
        String attemptKey = PROBLEM_ATTEMPT_PREFIX + problemId;

        // 检查用户是否已经通过
        Boolean isAccepted = redisTemplate.opsForSet().isMember(acceptKey, userId);
        if (Boolean.FALSE.equals(isAccepted)) {
            redisTemplate.opsForSet().add(attemptKey, userId);
        }
    }

    @Override
    public Long getSubmitCount(String problemId) {
        String key = PROBLEM_SUBMIT_PREFIX + problemId;
        return redisTemplate.opsForSet().size(key);
    }

    @Override
    public Map<String, Long> batchGetSubmitCount(List<String> problemIds) {
        if (problemIds == null || problemIds.isEmpty()) {
            return new HashMap<>();
        }

        // 使用管道批量操作
        List<Object> results = redisTemplate.executePipelined(new RedisCallback<Object>() {
            @Override
            public Object doInRedis(RedisConnection connection) throws DataAccessException {
                for (String problemId : problemIds) {
                    String key = PROBLEM_SUBMIT_PREFIX + problemId;
                    connection.setCommands().sCard(key.getBytes());
                }
                return null;
            }
        });

        Map<String, Long> result = new HashMap<>(problemIds.size());
        for (int i = 0; i < problemIds.size(); i++) {
            result.put(problemIds.get(i), (Long) results.get(i));
        }
        return result;
    }

    @Override
    public Long getAcceptCount(String problemId) {
        String key = PROBLEM_ACCEPT_PREFIX + problemId;
        return redisTemplate.opsForSet().size(key);
    }

    @Override
    public Map<String, Long> batchGetAcceptCount(List<String> problemIds) {
        if (problemIds == null || problemIds.isEmpty()) {
            return new HashMap<>();
        }

        List<Object> results = redisTemplate.executePipelined(new RedisCallback<Object>() {
            @Override
            public Object doInRedis(RedisConnection connection) throws DataAccessException {
                for (String problemId : problemIds) {
                    String key = PROBLEM_ACCEPT_PREFIX + problemId;
                    connection.setCommands().sCard(key.getBytes());
                }
                return null;
            }
        });

        Map<String, Long> result = new HashMap<>(problemIds.size());
        for (int i = 0; i < problemIds.size(); i++) {
            result.put(problemIds.get(i), (Long) results.get(i));
        }
        return result;
    }

    @Override
    public Long getParticipantCount(String problemId) {
        String key = PROBLEM_PARTICIPANT_PREFIX + problemId;
        return redisTemplate.opsForSet().size(key);
    }

    @Override
    public Map<String, Long> batchGetParticipantCount(List<String> problemIds) {
        if (problemIds == null || problemIds.isEmpty()) {
            return new HashMap<>();
        }

        List<Object> results = redisTemplate.executePipelined(new RedisCallback<Object>() {
            @Override
            public Object doInRedis(RedisConnection connection) throws DataAccessException {
                for (String problemId : problemIds) {
                    String key = PROBLEM_PARTICIPANT_PREFIX + problemId;
                    connection.setCommands().sCard(key.getBytes());
                }
                return null;
            }
        });

        Map<String, Long> result = new HashMap<>(problemIds.size());
        for (int i = 0; i < problemIds.size(); i++) {
            result.put(problemIds.get(i), (Long) results.get(i));
        }
        return result;
    }

    @Override
    public Long getAttemptCount(String problemId) {
        String key = PROBLEM_ATTEMPT_PREFIX + problemId;
        return redisTemplate.opsForSet().size(key);
    }

    @Override
    public Double getAcceptRate(String problemId) {
        Long acceptCount = getAcceptCount(problemId); // 通过人数
        long count = getParticipantCount(problemId);
        if (count == 0) {
            return 0.0;
        }
        double rate = (acceptCount.doubleValue() / count) * 100;
        double result = Math.round(rate * 100.0) / 100.0;

        // 确保总是返回小数格式
        DecimalFormat df = new DecimalFormat("0.00");
        return Double.parseDouble(df.format(result));
    }

    @Override
    public Double getAverageAcceptRate() {
//        // 获取所有题目ID（从排行榜中获取）
//        Set<Object> problemIds = redisTemplate.opsForZSet().range(PROBLEM_RANK_KEY, 0, -1);
//
//        if (problemIds == null || problemIds.isEmpty()) {
//            return 0.0;
//        }
//
//        double totalRate = 0.0;
//        int count = 0;
//
//        for (Object problemIdObj : problemIds) {
//            String problemId = (String) problemIdObj;
//            Double acceptRate = getAcceptRate(problemId);
//            if (acceptRate != null) {
//                totalRate += acceptRate;
//                count++;
//            }
//        }
//
//        return count > 0 ? Math.round((totalRate / count) * 100.0) / 100.0 : 0.0;

        // 优化：直接从排行榜获取所有题目ID，然后批量计算
        Set<Object> problemIds = redisTemplate.opsForZSet().range(PROBLEM_RANK_KEY, 0, -1);

        if (problemIds == null || problemIds.isEmpty()) {
            return 0.0;
        }

        List<String> problemIdList = problemIds.stream()
                .map(Object::toString)
                .collect(Collectors.toList());

        // 使用批量方法获取通过率
        Map<String, Double> acceptRates = batchGetAcceptRate(problemIdList);

        double totalRate = 0.0;
        int count = 0;

        for (Double rate : acceptRates.values()) {
            if (rate != null) {
                totalRate += rate;
                count++;
            }
        }

        return count > 0 ? Math.round((totalRate / count) * 100.0) / 100.0 : 0.0;
    }

    @Override
    public Map<String, Double> batchGetAcceptRate(List<String> problemIds) {
        if (problemIds == null || problemIds.isEmpty()) {
            return new HashMap<>();
        }

        // 批量获取通过人数和参与人数
        Map<String, Long> acceptCounts = batchGetAcceptCount(problemIds);
        Map<String, Long> participantCounts = batchGetParticipantCount(problemIds);

        Map<String, Double> result = new HashMap<>(problemIds.size());
        DecimalFormat df = new DecimalFormat("0.00");

        for (String problemId : problemIds) {
            Long acceptCount = acceptCounts.get(problemId);
            Long participantCount = participantCounts.get(problemId);

            if (participantCount == null || participantCount == 0) {
                result.put(problemId, 0.0);
            } else {
                double rate = (acceptCount.doubleValue() / participantCount) * 100;
                double formattedRate = Math.round(rate * 100.0) / 100.0;
                result.put(problemId, Double.parseDouble(df.format(formattedRate)));
            }
        }

        return result;
    }

    @Override
    public List<RankItem> getProblemRankTopN(int n) {
        Set<ZSetOperations.TypedTuple<Object>> tuples =
                redisTemplate.opsForZSet().reverseRangeWithScores(PROBLEM_RANK_KEY, 0, n - 1);

        return convertToRankItems(tuples);
    }

    @Override
    public List<RankItem> getProblemParticipantRankTopN(int n) {
        Set<ZSetOperations.TypedTuple<Object>> tuples =
                redisTemplate.opsForZSet().reverseRangeWithScores(PROBLEM_PARTICIPANT_RANK_KEY, 0, n - 1);

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
    public PageResult<RankItem> getProblemParticipantRankPage(int page, int size) {
        return null;
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