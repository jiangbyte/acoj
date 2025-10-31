package io.charlie.web.oj.modular.data.problem.mapper;

import io.charlie.galaxy.cache.MybatisPlusRedisCache;
import io.charlie.web.oj.modular.data.problem.entity.DataProblem;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import io.charlie.web.oj.modular.data.problem.param.DifficultyDistribution;
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
@CacheNamespace(implementation = MybatisPlusRedisCache.class, eviction = MybatisPlusRedisCache.class)
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
        WHERE dp.is_public = 1 AND dp.is_visible = 1 
        ORDER BY ds.submit_count DESC
        LIMIT #{topN}
    """)
    List<DataProblem> selectTopNBySubmitCount(@Param("topN") Integer topN);


    @Select("SELECT " +
            "difficulty, " +
            "COUNT(*) as count, " +
            "CASE difficulty " +
            "    WHEN 1 THEN '简单' " +
            "    WHEN 2 THEN '中等' " +
            "    WHEN 3 THEN '困难' " +
            "END as difficulty_name, " +
            "ROUND(COUNT(*) * 100.0 / (SELECT COUNT(*) FROM data_problem WHERE is_visible = true AND is_public = true), 2) as percentage " +
            "FROM data_problem " +
            "WHERE is_visible = true AND is_public = true AND difficulty IN (1, 2, 3) " +
            "GROUP BY difficulty")
    List<DifficultyDistribution> selectDifficultyDistribution();
}
