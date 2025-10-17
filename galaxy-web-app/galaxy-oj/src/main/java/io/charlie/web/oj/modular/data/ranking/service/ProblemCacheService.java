package io.charlie.web.oj.modular.data.ranking.service;

import io.charlie.web.oj.modular.data.ranking.data.RankItem;
import io.charlie.web.oj.modular.data.ranking.data.PageResult;

import java.util.List;
import java.util.Map;

public interface ProblemCacheService {

    // 增加提交人数
    void addSubmitUser(String problemId, String userId);

    // 增加通过人数
    void addAcceptUser(String problemId, String userId);

    // 增加参与人数
    void addParticipantUser(String problemId, String userId);

    // 增加尝试人数（如果用户已通过，则不记录）
    void addAttemptUser(String problemId, String userId);

//    boolean isAccepted(String problemId, String userId);

    // 获取提交人数
    Long getSubmitCount(String problemId);
    Map<String, Long> batchGetSubmitCount(List<String> problemIds);
    // 获取通过人数
    Long getAcceptCount(String problemId);

    Map<String, Long> batchGetAcceptCount(List<String> problemIds);

    // 获取参与人数
    Long getParticipantCount(String problemId);

    Map<String, Long> batchGetParticipantCount(List<String> problemIds);

    // 获取尝试人数
    Long getAttemptCount(String problemId);

    // 获取通过率
    Double getAcceptRate(String problemId);

    Double getAverageAcceptRate();

    Map<String, Double> batchGetAcceptRate(List<String> problemId);

    // 获取题目排行榜TopN（按通过人数排序）
    List<RankItem> getProblemRankTopN(int n);

    List<RankItem> getProblemParticipantRankTopN(int n);

    // 获取题目分页排行榜
    PageResult<RankItem> getProblemRankPage(int page, int size);

    PageResult<RankItem> getProblemParticipantRankPage(int page, int size);

    // 重置题目缓存
    void resetProblemCache(String problemId);
}