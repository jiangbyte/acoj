package io.charlie.app.core.modular.set.ranking.mapper;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import io.charlie.app.core.modular.problem.ranking.entity.ProblemRanking;
import io.charlie.app.core.modular.set.ranking.entity.SetRanking;
import io.charlie.app.core.modular.set.ranking.entity.SetUserRanking;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * @author Charlie Zhang
 * @version v1.0
 * @date 03/08/2025
 * @description TODO
 */
@Mapper
//@CacheNamespace(implementation = MybatisPlusRedisCache.class, eviction = MybatisPlusRedisCache.class)
public interface SetRankingMapper extends BaseMapper<SetRanking> {
    IPage<SetRanking> rankingPage(IPage<SetRanking> page, Wrapper<SetRanking> ew);

    List<SetRanking> selectTopN(Integer n);
}
