package io.charlie.web.oj.modular.data.ranking.service;

import io.charlie.web.oj.modular.data.ranking.data.PageResult;
import io.charlie.web.oj.modular.data.ranking.data.ProblemSetProblemStats;
import io.charlie.web.oj.modular.data.ranking.data.RankItem;

import java.util.List;
import java.util.Map;

public interface ProblemSetCacheService {

    // 增加题集参与人数
    void addProblemSetParticipant(String problemSetId, String userId);

    // 增加题集内题目的提交人数
    void addProblemSetProblemSubmit(String problemSetId, String problemId, String userId);

    // 增加题集内题目的通过人数
    void addProblemSetProblemAccept(String problemSetId, String problemId, String userId);

    // 增加题集内题目的参与人数
    void addProblemSetProblemParticipant(String problemSetId, String problemId, String userId);

    // 增加题集内题目的尝试人数
    void addProblemSetProblemAttempt(String problemSetId, String problemId, String userId);

    // 获取题集参与人数
    Long getProblemSetParticipantCount(String problemSetId);

    // 获取题集平均通过率
    Double getProblemSetAverageAcceptRate(String problemSetId);

    // 获取题集内题目的统计信息
    ProblemSetProblemStats getProblemSetProblemStats(String problemSetId, String problemId);

    // 获取题集排行榜TopN（按参与人数排序）
    List<RankItem> getProblemSetRankTopN(int n);

    Double getProblemAcceptRate(String problemSetId, String problemId);

    // 获取题集分页排行榜
    PageResult<RankItem> getProblemSetRankPage(int page, int size);

    Long getProblemAcceptCount(String problemSetId, String problemId);

    // 重置题集缓存
    void resetProblemSetCache(String problemSetId);

    /**
     * 获取题集内所有题目的总提交次数
     * @param problemSetId 题集ID
     * @return 总提交次数
     */
    Long getProblemSetTotalSubmitCount(String problemSetId);

}