package io.charlie.app.core.modular.set.problems.mapper;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import io.charlie.app.core.modular.problem.problem.entity.ProProblem;
import io.charlie.app.core.modular.problem.ranking.entity.ProblemUserRanking;
import io.charlie.app.core.modular.set.problems.entity.ProSetProblem;
import io.charlie.app.core.modular.set.submit.entity.ProSetSubmit;
import io.charlie.galaxy.cache.MybatisPlusRedisCache;
import org.apache.ibatis.annotations.CacheNamespace;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * @author ZhangJiangHu
 * @version v1.0
 * @date 28/08/2025
 * @description TODO
 */
@Mapper
@CacheNamespace(implementation = MybatisPlusRedisCache.class, eviction = MybatisPlusRedisCache.class)
public interface ProSetProblemMapper extends BaseMapper<ProSetProblem> {
    IPage<ProProblem> selectSetProblemPage(IPage<ProProblem> page, Wrapper<ProProblem> ew, String setId);

    List<ProProblem> selectSetProblemList(Wrapper<ProProblem> ew, String setId);
}
