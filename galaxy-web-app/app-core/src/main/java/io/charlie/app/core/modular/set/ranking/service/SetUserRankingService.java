package io.charlie.app.core.modular.set.ranking.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.charlie.app.core.modular.set.ranking.entity.SetUserRanking;
import io.charlie.app.core.modular.set.ranking.param.SetUserRankingPageParam;

/**
 * @author Charlie Zhang
 * @version v1.0
 * @date 28/07/2025
 * @description 题集提交排行榜服务
 */
public interface SetUserRankingService {
    Page<SetUserRanking> totalRankingPage(SetUserRankingPageParam setRankingPageParam);

    Page<SetUserRanking> monthlyRankingPage(SetUserRankingPageParam setRankingPageParam);

    Page<SetUserRanking> weeklyRankingPage(SetUserRankingPageParam setRankingPageParam);

    Page<SetUserRanking> dailyRankingPage(SetUserRankingPageParam setRankingPageParam);

    SetUserRanking totalRankingByUserId(String setId, String userId);

    SetUserRanking monthlyRankingByUserId(String setId, String userId);

    SetUserRanking weeklyRankingByUserId(String setId, String userId);

    SetUserRanking dailyRankingByUserId(String setId, String userId);
}
