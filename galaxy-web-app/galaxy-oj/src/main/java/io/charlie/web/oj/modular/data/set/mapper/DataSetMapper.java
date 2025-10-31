package io.charlie.web.oj.modular.data.set.mapper;

import io.charlie.galaxy.cache.MybatisPlusRedisCache;
import io.charlie.web.oj.modular.data.problem.entity.DataProblem;
import io.charlie.web.oj.modular.data.set.entity.DataSet;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.CacheNamespace;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
* @author Charlie Zhang
* @version v1.0
* @date 2025-09-20
* @description 题集 Mapper 接口
*/
@Mapper
@CacheNamespace(implementation = MybatisPlusRedisCache.class, eviction = MybatisPlusRedisCache.class)
public interface DataSetMapper extends BaseMapper<DataSet> {
    @Select("""
     SELECT
         dp.*,
         ROW_NUMBER() OVER (ORDER BY ds.submit_count DESC) as `rank`
     FROM data_set dp
              INNER JOIN (
         SELECT set_id, COUNT(DISTINCT user_id) as submit_count
         FROM data_solved
         WHERE is_set = 1
         GROUP BY set_id
         HAVING COUNT(DISTINCT user_id) > 0
     ) ds ON dp.id = ds.set_id
      WHERE dp.is_visible = 1 
     ORDER BY ds.submit_count DESC
        LIMIT #{topN}
    """)
    List<DataSet> selectTopNBySubmitCount(@Param("topN") Integer topN);
}
