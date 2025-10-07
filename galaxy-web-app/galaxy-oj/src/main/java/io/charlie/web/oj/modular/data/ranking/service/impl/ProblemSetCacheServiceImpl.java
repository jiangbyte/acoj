package io.charlie.web.oj.modular.data.ranking.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import io.charlie.web.oj.modular.data.ranking.data.PageResult;
import io.charlie.web.oj.modular.data.ranking.data.ProblemSetProblemStats;
import io.charlie.web.oj.modular.data.ranking.data.RankItem;
import io.charlie.web.oj.modular.data.ranking.service.ProblemSetCacheService;
import io.charlie.web.oj.modular.data.relation.set.service.DataSetProblemService;
import io.charlie.web.oj.modular.data.set.service.DataSetService;
import io.charlie.web.oj.modular.data.submit.entity.DataSubmit;
import io.charlie.web.oj.modular.data.submit.mapper.DataSubmitMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Service;

import java.text.DecimalFormat;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProblemSetCacheServiceImpl implements ProblemSetCacheService {
    private final DataSubmitMapper dataSubmitMapper;

    private static final String PROBLEM_SET_PARTICIPANT_PREFIX = "problemset:participant:";
    private static final String PROBLEM_SET_RANK_KEY = "problemset:rank";

    // 题集内题目的前缀
    private static final String PROBLEM_SET_PROBLEM_SUBMIT_PREFIX = "problemset:problem:submit:";
    private static final String PROBLEM_SET_PROBLEM_SUBMIT_COUNT_PREFIX = "problemset:problem:submit:count";
    private static final String PROBLEM_SET_PROBLEM_ACCEPT_PREFIX = "problemset:problem:accept:";
    private static final String PROBLEM_SET_PROBLEM_PARTICIPANT_PREFIX = "problemset:problem:participant:";
    private static final String PROBLEM_SET_PROBLEM_ATTEMPT_PREFIX = "problemset:problem:attempt:";

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Autowired
    private DataSetProblemService dataSetProblemService;

    @Override
    public void addProblemSetParticipant(String problemSetId, String userId) {
        String key = PROBLEM_SET_PARTICIPANT_PREFIX + problemSetId;
        redisTemplate.opsForSet().add(key, userId);

        // 更新题集排行榜分数（参与人数）
        Double score = redisTemplate.opsForSet().size(key) * 1.0;
        redisTemplate.opsForZSet().add(PROBLEM_SET_RANK_KEY, problemSetId, score);
    }

    @Override
    public void addProblemSetProblemSubmit(String problemSetId, String problemId, String userId) {
        String key = PROBLEM_SET_PROBLEM_SUBMIT_PREFIX + problemSetId + ":" + problemId;
        redisTemplate.opsForSet().add(key, userId);

        // 同时添加到参与人数
        addProblemSetProblemParticipant(problemSetId, problemId, userId);
    }

    @Override
    public void addProblemSetProblemAccept(String problemSetId, String problemId, String userId) {
        String acceptKey = PROBLEM_SET_PROBLEM_ACCEPT_PREFIX + problemSetId + ":" + problemId;
        String attemptKey = PROBLEM_SET_PROBLEM_ATTEMPT_PREFIX + problemSetId + ":" + problemId;

        redisTemplate.opsForSet().add(acceptKey, userId);
        redisTemplate.opsForSet().remove(attemptKey, userId);
    }

    @Override
    public void addProblemSetProblemParticipant(String problemSetId, String problemId, String userId) {
        String key = PROBLEM_SET_PROBLEM_PARTICIPANT_PREFIX + problemSetId + ":" + problemId;
        redisTemplate.opsForSet().add(key, userId);
    }

    @Override
    public void addProblemSetProblemAttempt(String problemSetId, String problemId, String userId) {
        String acceptKey = PROBLEM_SET_PROBLEM_ACCEPT_PREFIX + problemSetId + ":" + problemId;
        String attemptKey = PROBLEM_SET_PROBLEM_ATTEMPT_PREFIX + problemSetId + ":" + problemId;

        Boolean isAccepted = redisTemplate.opsForSet().isMember(acceptKey, userId);
        if (Boolean.FALSE.equals(isAccepted)) {
            redisTemplate.opsForSet().add(attemptKey, userId);
        }
    }

    @Override
    public Long getProblemSetParticipantCount(String problemSetId) {
        String key = PROBLEM_SET_PARTICIPANT_PREFIX + problemSetId;
        return redisTemplate.opsForSet().size(key);
    }

    @Override
    public Double getProblemSetAverageAcceptRate(String problemSetId) {
        List<String> problemIds = dataSetProblemService.getProblemIdsBySetId(problemSetId); // 临时方法

        if (problemIds.isEmpty()) {
            return 0.0;
        }

        double totalRate = 0.0;
        int count = 0;

        for (String problemId : problemIds) {
            Double acceptRate = getProblemAcceptRate(problemSetId, problemId);
            if (acceptRate != null) {
                totalRate += acceptRate;
                count++;
            }
        }

        return count > 0 ? Math.round((totalRate / count) * 100.0) / 100.0 : 0.0;
    }

    @Override
    public ProblemSetProblemStats getProblemSetProblemStats(String problemSetId, String problemId) {

        ProblemSetProblemStats problemSetProblemStats = new ProblemSetProblemStats();
        problemSetProblemStats.setSubmitCount(getProblemSubmitCount(problemSetId, problemId));
        problemSetProblemStats.setAcceptCount(getProblemAcceptCount(problemSetId, problemId));
        problemSetProblemStats.setParticipantCount(getProblemParticipantCount(problemSetId, problemId));
        problemSetProblemStats.setAttemptCount(getProblemAttemptCount(problemSetId, problemId));
        problemSetProblemStats.setAcceptRate(getProblemAcceptRate(problemSetId, problemId));

        return problemSetProblemStats;
    }

    @Override
    public List<RankItem> getProblemSetRankTopN(int n) {
        Set<ZSetOperations.TypedTuple<Object>> tuples =
                redisTemplate.opsForZSet().reverseRangeWithScores(PROBLEM_SET_RANK_KEY, 0, n - 1);

        return convertToRankItems(tuples);
    }

    @Override
    public PageResult<RankItem> getProblemSetRankPage(int page, int size) {
        long start = (page - 1) * size;
        long end = start + size - 1;

        Set<ZSetOperations.TypedTuple<Object>> tuples =
                redisTemplate.opsForZSet().reverseRangeWithScores(PROBLEM_SET_RANK_KEY, start, end);

        Long total = redisTemplate.opsForZSet().size(PROBLEM_SET_RANK_KEY);
        Long totalPages = (total + size - 1) / size;

        List<RankItem> items = convertToRankItems(tuples, start + 1);

        return new PageResult<>(items, total, page, size, totalPages);
    }

    @Override
    public void resetProblemSetCache(String problemSetId) {
        // 删除题集参与人数
        String participantKey = PROBLEM_SET_PARTICIPANT_PREFIX + problemSetId;
        redisTemplate.delete(participantKey);

        // TODO: 删除题集内所有题目的缓存
        // List<String> problemIds = dataSetService.getProblems(problemSetId);
        // for (String problemId : problemIds) {
        //     resetProblemSetProblemCache(problemSetId, problemId);
        // }

        redisTemplate.opsForZSet().remove(PROBLEM_SET_RANK_KEY, problemSetId);
    }

    @Override
    public Long getProblemSetTotalSubmitCount(String problemSetId) {
        List<String> problemIds = dataSetProblemService.getProblemIdsBySetId(problemSetId);
//
//        if (problemIds.isEmpty()) {
//            return 0L;
//        }
//
//        long totalSubmitCount = 0L;
//
//        for (String problemId : problemIds) {
//            Long submitCount = getProblemSubmitCount(problemSetId, problemId);
//            if (submitCount != null) {
//                totalSubmitCount += submitCount;
//            }
//        }
//
//        return totalSubmitCount;
        if (problemIds.isEmpty()) {
            return 0L;
        }

        return dataSubmitMapper.selectCount(new LambdaQueryWrapper<DataSubmit>()
                .eq(DataSubmit::getSetId, problemSetId)
                .in(DataSubmit::getProblemId, problemIds)
                .eq(DataSubmit::getIsSet, Boolean.TRUE)
                .eq(DataSubmit::getSubmitType, Boolean.TRUE)
        );
    }

    // 私有辅助方法
    private Long getProblemSubmitCount(String problemSetId, String problemId) {
        String key = PROBLEM_SET_PROBLEM_SUBMIT_PREFIX + problemSetId + ":" + problemId;
        return redisTemplate.opsForSet().size(key);
    }

    public Long getProblemAcceptCount(String problemSetId, String problemId) {
        String key = PROBLEM_SET_PROBLEM_ACCEPT_PREFIX + problemSetId + ":" + problemId;
        return redisTemplate.opsForSet().size(key);
    }

    private Long getProblemParticipantCount(String problemSetId, String problemId) {
        String key = PROBLEM_SET_PROBLEM_PARTICIPANT_PREFIX + problemSetId + ":" + problemId;
        return redisTemplate.opsForSet().size(key);
    }

    private Long getProblemAttemptCount(String problemSetId, String problemId) {
        String key = PROBLEM_SET_PROBLEM_ATTEMPT_PREFIX + problemSetId + ":" + problemId;
        return redisTemplate.opsForSet().size(key);
    }

    @Override
    public Double getProblemAcceptRate(String problemSetId, String problemId) {
        Long acceptCount = getProblemAcceptCount(problemSetId, problemId); // 通过人数
        long count = getProblemParticipantCount(problemSetId, problemId);

        if (count == 0) {
            return 0.0;
        }
        double rate = (acceptCount.doubleValue() / count) * 100;
        double result = Math.round(rate * 100.0) / 100.0;

        // 确保总是返回小数格式
        DecimalFormat df = new DecimalFormat("0.00");
        return Double.parseDouble(df.format(result));
    }

    private void resetProblemSetProblemCache(String problemSetId, String problemId) {
        String[] keys = {
                PROBLEM_SET_PROBLEM_SUBMIT_PREFIX + problemSetId + ":" + problemId,
                PROBLEM_SET_PROBLEM_ACCEPT_PREFIX + problemSetId + ":" + problemId,
                PROBLEM_SET_PROBLEM_PARTICIPANT_PREFIX + problemSetId + ":" + problemId,
                PROBLEM_SET_PROBLEM_ATTEMPT_PREFIX + problemSetId + ":" + problemId
        };

        redisTemplate.delete(Arrays.asList(keys));
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