package io.charlie.app.core.modular.set.ranking.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.charlie.app.core.modular.problem.ranking.entity.ProblemRanking;
import io.charlie.app.core.modular.problem.ranking.param.ProblemRankingPageParam;
import io.charlie.app.core.modular.set.ranking.entity.SetRanking;
import io.charlie.app.core.modular.set.ranking.param.SetRankingPageParam;

import java.util.List;

/**
 * @author Charlie Zhang
 * @version v1.0
 * @date 03/08/2025
 * @description 题集排行服务
 */
public interface SetRankingService {
    Page<SetRanking> rankingPage(SetRankingPageParam setRankingPageParam);

    List<SetRanking> topN(Integer n);
}
