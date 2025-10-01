package io.charlie.web.oj.modular.data.ranking.service;

import io.charlie.web.oj.modular.data.ranking.data.PageResult;
import io.charlie.web.oj.modular.data.ranking.data.RankItem;

import java.util.List;

public interface UserCacheService {
    
    // 增加用户活跃指数
    void addUserActivity(String userId, double increment);
    
    // 增加用户通过的题目
    void addUserAcceptedProblem(String userId, String problemId);
    
    // 获取用户活跃指数
    Double getUserActivity(String userId);

    // 获取活跃分页排行榜
    PageResult<RankItem> getUserActivityPage(int page, int size);

    List<RankItem> getUserActivityTopN(int n);

    // 获取用户通过的题目数量
    Long getUserAcceptedCount(String userId);
    
    // 获取用户排行榜TopN（按通过题目数量排序）
    List<RankItem> getUserRankTopN(int n);
    
    // 获取用户分页排行榜
    PageResult<RankItem> getUserRankPage(int page, int size);
    
    // 重置用户月活跃指数
    void resetMonthlyActivity();
    
    // 重置用户缓存
    void resetUserCache(String userId);
}