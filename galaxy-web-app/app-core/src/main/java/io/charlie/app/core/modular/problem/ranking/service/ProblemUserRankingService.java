package io.charlie.app.core.modular.problem.ranking.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.charlie.app.core.modular.problem.ranking.entity.ProblemUserRanking;
import io.charlie.app.core.modular.problem.ranking.param.ActiveUser;
import io.charlie.app.core.modular.problem.ranking.param.ProblemUserRankingPageParam;
import io.charlie.app.core.modular.problem.ranking.param.UserActivityRank;
import io.charlie.app.core.modular.problem.ranking.service.impl.ProblemUserRankingServiceImpl;

import java.util.List;

/**
 * @author Charlie Zhang
 * @version v1.0
 * @date 28/07/2025
 * @description 提交排行服务
 */
public interface ProblemUserRankingService {
    Page<ProblemUserRanking> totalRankingPage(ProblemUserRankingPageParam problemRankingPageParam);

    Page<ProblemUserRanking> monthlyRankingPage(ProblemUserRankingPageParam problemRankingPageParam);

    Page<ProblemUserRanking> weeklyRankingPage(ProblemUserRankingPageParam problemRankingPageParam);

    Page<ProblemUserRanking> dailyRankingPage(ProblemUserRankingPageParam problemRankingPageParam);

    ProblemUserRanking totalRankingByUserId(String userId);

    ProblemUserRanking monthlyRankingByUserId(String userId);

    ProblemUserRanking weeklyRankingByUserId(String userId);

    ProblemUserRanking dailyRankingByUserId(String userId);

    List<ProblemUserRanking> topN(Integer n);

    List<String> getActiveUsersTop10();

    List<ActiveUser> getActiveUsersTopN(int n);

    UserActivityRank getUserActivityRank(String userId);
}
