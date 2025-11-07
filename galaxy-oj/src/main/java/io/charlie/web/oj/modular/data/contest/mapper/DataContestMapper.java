package io.charlie.web.oj.modular.data.contest.mapper;

import com.baomidou.dynamic.datasource.annotation.DS;
import io.charlie.galaxy.cache.MybatisPlusRedisCache;
import io.charlie.web.oj.modular.data.contest.entity.DataContest;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import io.charlie.web.oj.modular.data.set.entity.DataSet;
import org.apache.ibatis.annotations.CacheNamespace;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
* @author Charlie Zhang
* @version v1.0
* @date 2025-11-06
* @description 竞赛表 Mapper 接口
*/
@Mapper
//@CacheNamespace(implementation = MybatisPlusRedisCache.class, eviction = MybatisPlusRedisCache.class)
public interface DataContestMapper extends BaseMapper<DataContest> {
    @DS("slave")
    @Select("""
     SELECT
         dp.*,
         ROW_NUMBER() OVER (ORDER BY ds.submit_count DESC) as `rank`
     FROM data_contest dp
              INNER JOIN (
         SELECT module_id, COUNT(DISTINCT user_id) as submit_count
         FROM data_solved
         WHERE module_type = 'CONTEST'
         GROUP BY module_id
         HAVING COUNT(DISTINCT user_id) > 0
     ) ds ON dp.id = ds.module_id
     ORDER BY ds.submit_count DESC
        LIMIT #{topN}
    """)
    List<DataContest> selectTopNBySubmitCount(@Param("topN") Integer topN);
}
