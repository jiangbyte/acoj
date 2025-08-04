package io.charlie.app.core.modular.set.ranking.mapper;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import io.charlie.app.core.modular.set.ranking.entity.SetUserRanking;
import org.apache.ibatis.annotations.Mapper;

/**
 * @author Charlie Zhang
 * @version v1.0
 * @date 2025-07-25
 * @description 题目排行榜 Mapper 接口
 */
@Mapper
//@CacheNamespace(implementation = MybatisPlusRedisCache.class, eviction = MybatisPlusRedisCache.class)
public interface SetUserRankingMapper extends BaseMapper<SetUserRanking> {
    IPage<SetUserRanking> selectTotalRankingPage(IPage<SetUserRanking> page, Wrapper<SetUserRanking> ew);

    IPage<SetUserRanking> selectMonthlyRankingPage(IPage<SetUserRanking> page, Wrapper<SetUserRanking> ew);

    IPage<SetUserRanking> selectWeeklyRankingPage(IPage<SetUserRanking> page, Wrapper<SetUserRanking> ew);

    IPage<SetUserRanking> selectDailyRankingPage(IPage<SetUserRanking> page, Wrapper<SetUserRanking> ew);

    SetUserRanking selectTotalRankingByUserId(String setId, String userId);

    SetUserRanking selectMonthlyRankingByUserId(String setId, String userId);

    SetUserRanking selectWeeklyRankingByUserId(String setId, String userId);

    SetUserRanking selectDailyRankingByUserId(String setId, String userId);
}
