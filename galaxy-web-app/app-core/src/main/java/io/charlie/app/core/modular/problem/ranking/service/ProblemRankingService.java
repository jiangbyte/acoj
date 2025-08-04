package io.charlie.app.core.modular.problem.ranking.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.charlie.app.core.modular.problem.ranking.entity.ProblemRanking;
import io.charlie.app.core.modular.problem.ranking.entity.ProblemUserRanking;
import io.charlie.app.core.modular.problem.ranking.param.ProblemRankingPageParam;
import io.charlie.app.core.modular.problem.ranking.param.ProblemUserRankingPageParam;

/**
 * @author Charlie Zhang
 * @version v1.0
 * @date 03/08/2025
 * @description TODO
 */
public interface ProblemRankingService {
    Page<ProblemRanking> rankingPage(ProblemRankingPageParam rankingPageParam);
}
