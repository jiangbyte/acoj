package io.charlie.app.core.modular.problem.ranking.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.charlie.app.core.modular.problem.ranking.entity.ProblemUserRanking;
import io.charlie.app.core.modular.problem.ranking.param.ProblemUserRankingPageParam;

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
}
