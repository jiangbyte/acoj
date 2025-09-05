package io.charlie.app.core.modular.set.progress.mapper;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import io.charlie.app.core.modular.problem.ranking.entity.ProblemUserRanking;
import io.charlie.app.core.modular.set.progress.entity.ProSetProgress;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import io.charlie.app.core.modular.set.progress.entity.ProSetProgressData;
import io.charlie.app.core.modular.set.progress.param.ProSetProgressDataPageParam;
import org.apache.ibatis.annotations.Mapper;

/**
 * @author Charlie Zhang
 * @version v1.0
 * @date 2025-07-25
 * @description 题集进度表 Mapper 接口
 */
@Mapper
//@CacheNamespace(implementation = MybatisPlusRedisCache.class, eviction = MybatisPlusRedisCache.class)
public interface ProSetProgressMapper extends BaseMapper<ProSetProgress> {
    IPage<ProSetProgressData> selectProgressDataPage(IPage<ProSetProgressData> page, Wrapper<ProSetProgressData> ew, ProSetProgressDataPageParam param);
}
