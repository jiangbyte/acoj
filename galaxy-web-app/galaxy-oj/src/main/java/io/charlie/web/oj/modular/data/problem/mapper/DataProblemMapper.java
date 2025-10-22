package io.charlie.web.oj.modular.data.problem.mapper;

import io.charlie.galaxy.cache.MybatisPlusRedisCache;
import io.charlie.web.oj.modular.data.problem.entity.DataProblem;
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
 * @description 题目表 Mapper 接口
 */
@Mapper
//@CacheNamespace(implementation = MybatisPlusRedisCache.class, eviction = MybatisPlusRedisCache.class)
public interface DataProblemMapper extends BaseMapper<DataProblem> {
    /**
     * 按提交人数排行的题目 Top N 查询（包含排名）
     * @param topN 前N名
     * @return 题目列表（包含排名）
     */
//    @Select("""
//                SELECT
//                    dp.*,
//                    ROW_NUMBER() OVER (ORDER BY COALESCE(ds.submit_count, 0) DESC) as `rank`
//                FROM data_problem dp
//                LEFT JOIN (
//                    SELECT problem_id, COUNT(DISTINCT user_id) as submit_count
//                    FROM data_solved
//                    WHERE is_set = 0
//                    GROUP BY problem_id
//                ) ds ON dp.id = ds.problem_id
//                ORDER BY COALESCE(ds.submit_count, 0) DESC
//                LIMIT #{topN}
//            """)
    @Select("""
        SELECT 
            dp.*,
            ROW_NUMBER() OVER (ORDER BY ds.submit_count DESC) as `rank`
        FROM data_problem dp
        INNER JOIN (
            SELECT problem_id, COUNT(DISTINCT user_id) as submit_count
            FROM data_solved
            WHERE is_set = 0
            GROUP BY problem_id
            HAVING COUNT(DISTINCT user_id) > 0
        ) ds ON dp.id = ds.problem_id
        ORDER BY ds.submit_count DESC
        LIMIT #{topN}
    """)
    List<DataProblem> selectTopNBySubmitCount(@Param("topN") Integer topN);
}
