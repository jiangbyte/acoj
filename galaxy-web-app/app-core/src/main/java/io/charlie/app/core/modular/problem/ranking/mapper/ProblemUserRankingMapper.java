package io.charlie.app.core.modular.problem.ranking.mapper;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import io.charlie.app.core.modular.problem.ranking.entity.ProblemUserRanking;
import io.charlie.app.core.modular.problem.ranking.param.ProblemUserRankingPageParam;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
* @author Charlie Zhang
* @version v1.0
* @date 2025-07-25
* @description 题目排行榜 Mapper 接口
*/
@Mapper
//@CacheNamespace(implementation = MybatisPlusRedisCache.class, eviction = MybatisPlusRedisCache.class)
public interface ProblemUserRankingMapper extends BaseMapper<ProblemUserRanking> {
    IPage<ProblemUserRanking> selectTotalRankingPage(IPage<ProblemUserRanking> page, Wrapper<ProblemUserRanking> ew);

    IPage<ProblemUserRanking> selectTotalRankingPageWithParam(IPage<ProblemUserRanking> page, Wrapper<ProblemUserRanking> ew, ProblemUserRankingPageParam param);

    IPage<ProblemUserRanking> selectMonthlyRankingPage(IPage<ProblemUserRanking> page, Wrapper<ProblemUserRanking> ew);
    IPage<ProblemUserRanking> selectWeeklyRankingPage(IPage<ProblemUserRanking> page, Wrapper<ProblemUserRanking> ew);
    IPage<ProblemUserRanking> selectDailyRankingPage(IPage<ProblemUserRanking> page, Wrapper<ProblemUserRanking> ew);

    ProblemUserRanking selectTotalRankingByUserId(String userId);
    ProblemUserRanking selectMonthlyRankingByUserId(String userId);
    ProblemUserRanking selectWeeklyRankingByUserId(String userId);
    ProblemUserRanking selectDailyRankingByUserId(String userId);

    List<ProblemUserRanking> selectTopN(Integer n);
}
