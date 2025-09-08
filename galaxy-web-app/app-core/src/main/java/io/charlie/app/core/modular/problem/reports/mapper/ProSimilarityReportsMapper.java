package io.charlie.app.core.modular.problem.reports.mapper;

import io.charlie.galaxy.cache.MybatisPlusRedisCache;
import io.charlie.app.core.modular.problem.reports.entity.ProSimilarityReports;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.CacheNamespace;
import org.apache.ibatis.annotations.Mapper;

/**
* @author Charlie Zhang
* @version v1.0
* @date 2025-09-08
* @description 题目报告库表 Mapper 接口
*/
@Mapper
//@CacheNamespace(implementation = MybatisPlusRedisCache.class, eviction = MybatisPlusRedisCache.class)
public interface ProSimilarityReportsMapper extends BaseMapper<ProSimilarityReports> {

}
