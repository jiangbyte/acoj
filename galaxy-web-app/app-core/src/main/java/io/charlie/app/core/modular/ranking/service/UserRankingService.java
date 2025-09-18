package io.charlie.app.core.modular.ranking.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.charlie.app.core.modular.problem.ranking.entity.ProblemUserRanking;
import io.charlie.app.core.modular.ranking.data.UserRankingInfo;
import io.charlie.app.core.modular.ranking.param.UserRankingPageParam;
import io.charlie.galaxy.result.Result;

import java.util.List;

/**
 * @author ZhangJiangHu
 * @version v1.0
 * @date 18/09/2025
 * @description TODO
 */
public interface UserRankingService {
    void problemUserRankingUpdate(String id);

    Result.PageData<UserRankingInfo> problemUserRankingPage(UserRankingPageParam param);

    List<UserRankingInfo> problemUserRankingTopN(int n);

    UserRankingInfo problemUserRankingById(String id);
}
