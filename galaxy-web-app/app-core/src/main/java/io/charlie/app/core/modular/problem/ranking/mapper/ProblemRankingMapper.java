package io.charlie.app.core.modular.problem.ranking.mapper;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import io.charlie.app.core.modular.problem.ranking.entity.ProblemRanking;
import io.charlie.app.core.modular.problem.ranking.entity.ProblemUserRanking;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * @author Charlie Zhang
 * @version v1.0
 * @date 03/08/2025
 * @description 题目排行榜Mapper
 */
@Mapper
//@CacheNamespace(implementation = MybatisPlusRedisCache.class, eviction = MybatisPlusRedisCache.class)
public interface ProblemRankingMapper extends BaseMapper<ProblemRanking> {
    IPage<ProblemRanking> rankingPage(IPage<ProblemRanking> page, Wrapper<ProblemRanking> ew);

    List<ProblemRanking> selectTopN(Integer n);
}
