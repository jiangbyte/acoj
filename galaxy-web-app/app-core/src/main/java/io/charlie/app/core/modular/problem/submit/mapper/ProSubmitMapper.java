package io.charlie.app.core.modular.problem.submit.mapper;

import io.charlie.app.core.modular.analyse.entity.SubmitTrend;
import io.charlie.app.core.modular.problem.submit.entity.ProSubmit;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import io.charlie.app.core.modular.problem.submit.param.JudgeStatusCountDTO;
import io.charlie.galaxy.cache.MybatisPlusRedisCache;
import io.charlie.galaxy.option.NameOption;
import org.apache.ibatis.annotations.CacheNamespace;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
* @author Charlie Zhang
* @version v1.0
* @date 2025-07-25
* @description 提交表 Mapper 接口
*/
@Mapper
@CacheNamespace(implementation = MybatisPlusRedisCache.class, eviction = MybatisPlusRedisCache.class)
public interface ProSubmitMapper extends BaseMapper<ProSubmit> {
    List<NameOption<Long>> getLanguageDistribution();

    List<NameOption<Double>> getProblemRateDistribution();

    List<SubmitTrend> getWeeklySubmitTrend();

    List<SubmitTrend> getTodayHourlySubmitTrend();

    /**
     * 按状态统计提交数量
     */
    @Select("SELECT status, COUNT(*) as count FROM pro_submit GROUP BY status")
    List<JudgeStatusCountDTO> countByStatus();
}
