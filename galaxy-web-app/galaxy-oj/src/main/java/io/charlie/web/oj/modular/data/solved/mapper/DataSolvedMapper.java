package io.charlie.web.oj.modular.data.solved.mapper;

import io.charlie.galaxy.cache.MybatisPlusRedisCache;
import io.charlie.web.oj.modular.data.solved.entity.DataSolved;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.CacheNamespace;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * @author Charlie Zhang
 * @version v1.0
 * @date 2025-09-20
 * @description 用户解决表 Mapper 接口
 */
@Mapper
//@CacheNamespace(implementation = MybatisPlusRedisCache.class, eviction = MybatisPlusRedisCache.class)
public interface DataSolvedMapper extends BaseMapper<DataSolved> {
    /**
     * 获取全部题目(在解决表中有记录的题目)的详细通过率统计（包含题目数量）
     * 使用总体通过率计算，更准确反映用户整体解题能力
     *
     * @return 统计信息
     */
    @Select("""
                SELECT 
                    COUNT(*) as totalProblems, 
                    SUM(CASE WHEN total_participants > 0 THEN 1 ELSE 0 END) as problemsWithSubmissions,
                    SUM(total_participants) as totalParticipants,
                    SUM(accepted_participants) as totalAcceptedParticipants,
                    CASE WHEN SUM(total_participants) > 0 THEN 
                        ROUND(SUM(accepted_participants) * 100.0 / SUM(total_participants), 2)
                    ELSE 0 END as averageAcceptanceRate,
                    ROUND(MIN(CASE WHEN total_participants > 0 THEN acceptance_rate ELSE NULL END), 2) as minAcceptanceRate,
                    ROUND(MAX(CASE WHEN total_participants > 0 THEN acceptance_rate ELSE NULL END), 2) as maxAcceptanceRate
                FROM (
                    SELECT 
                        dp.id, 
                        COUNT(DISTINCT ds.user_id) as total_participants, 
                        COUNT(DISTINCT CASE WHEN ds.solved = true THEN ds.user_id ELSE NULL END) as accepted_participants,
                        CASE WHEN COUNT(DISTINCT ds.user_id) > 0 THEN 
                            COUNT(DISTINCT CASE WHEN ds.solved = true THEN ds.user_id ELSE NULL END) * 100.0 / COUNT(DISTINCT ds.user_id)
                        ELSE 0 END as acceptance_rate
                    FROM data_problem dp 
                    LEFT JOIN data_solved ds ON dp.id = ds.problem_id AND ds.is_set = 0 
                    GROUP BY dp.id
                ) as problem_stats
            """)
    Map<String, Object> selectAllProblemAcceptanceStats();

    /**
     * 获取题目通过率统计（支持批量查询）
     *
     * @param problemIds 题目ID列表
     * @return 统计信息列表
     */
    @Select("""
                <script>
                SELECT 
                    problem_id as problemId,
                    COUNT(DISTINCT user_id) as totalParticipants,
                    COUNT(DISTINCT CASE WHEN solved = true THEN user_id ELSE NULL END) as acceptedParticipants,
                    CASE WHEN COUNT(DISTINCT user_id) > 0 THEN 
                        ROUND(COUNT(DISTINCT CASE WHEN solved = true THEN user_id ELSE NULL END) * 100.0 / COUNT(DISTINCT user_id), 2)
                    ELSE 0 END as acceptanceRate
                FROM data_solved
                WHERE problem_id IN
                <foreach collection='problemIds' item='id' open='(' separator=',' close=')'>
                    #{id}
                </foreach>
                AND is_set = 0
                GROUP BY problem_id
                </script>
            """)
    List<Map<String, Object>> selectProblemAcceptanceStats(@Param("problemIds") List<String> problemIds);


    @Select("""
                <script>
                SELECT 
                    problem_id as problemId,
                    COUNT(DISTINCT user_id) as totalParticipants,
                    COUNT(DISTINCT CASE WHEN solved = true THEN user_id ELSE NULL END) as acceptedParticipants,
                    CASE WHEN COUNT(DISTINCT user_id) > 0 THEN 
                        ROUND(COUNT(DISTINCT CASE WHEN solved = true THEN user_id ELSE NULL END) * 100.0 / COUNT(DISTINCT user_id), 2)
                    ELSE 0 END as acceptanceRate
                FROM data_solved
                WHERE problem_id IN
                <foreach collection='problemIds' item='id' open='(' separator=',' close=')'>
                    #{id}
                </foreach>
                AND is_set = 1 AND set_id = #{setId}
                GROUP BY problem_id
                </script>
            """)
    List<Map<String, Object>> selectSetProblemAcceptanceStats(@Param("setId") String setId, @Param("problemIds") List<String> problemIds);


    /**
     * 获取用户解题统计（用于排行榜）
     *
     * @return 用户统计列表
     */
    @Select("""
               SELECT
                ds.user_id,
                (SELECT COUNT(*) FROM data_submit WHERE user_id = ds.user_id AND is_set = 0 AND submit_type = 1) as total_submit_count,
                COUNT(DISTINCT ds.problem_id) as submitted_count,
                COUNT(DISTINCT CASE WHEN ds.solved = 1 THEN ds.problem_id ELSE NULL END) as solved_count,
                CASE
                    WHEN COUNT(DISTINCT ds.problem_id) > 0 THEN
                        ROUND(
                            COUNT(DISTINCT CASE WHEN ds.solved = 1 THEN ds.problem_id ELSE NULL END) * 100.0 /
                            COUNT(DISTINCT ds.problem_id), 2
                        )
                    ELSE 0
                END as acceptance_rate
            FROM data_solved ds
            WHERE ds.is_set = 0
            GROUP BY ds.user_id
            HAVING submitted_count > 0
            ORDER BY solved_count DESC, acceptance_rate DESC
             """)
    List<Map<String, Object>> selectUserSolveStatistics();


    @Select("""
            SELECT
                ds.user_id,
                (SELECT COUNT(*) FROM data_submit WHERE user_id = ds.user_id AND is_set = 0 AND submit_type = 1) as total_submit_count,
                COUNT(DISTINCT ds.problem_id) as submitted_count,
                COUNT(DISTINCT CASE WHEN ds.solved = 1 THEN ds.problem_id ELSE NULL END) as solved_count,
                CASE
                    WHEN COUNT(DISTINCT ds.problem_id) > 0 THEN
                        ROUND(
                            COUNT(DISTINCT CASE WHEN ds.solved = 1 THEN ds.problem_id ELSE NULL END) * 100.0 /
                            COUNT(DISTINCT ds.problem_id), 2
                        )
                    ELSE 0
                END as acceptance_rate
            FROM data_solved ds
            WHERE ds.is_set = 0
            GROUP BY ds.user_id
            HAVING submitted_count > 0
            ORDER BY solved_count DESC, acceptance_rate DESC
            LIMIT #{batchSize} OFFSET #{offset}
            """)
    List<Map<String, Object>> selectUserSolveStatisticsBatch(@Param("offset") int offset, @Param("batchSize") int batchSize);

    /**
     * 根据用户ID获取解题统计
     *
     * @param userId 用户ID
     * @return 用户统计信息
     */
//    @Select("""
//                SELECT
//                    user_id,
//                    COUNT(DISTINCT problem_id) as submitted_count,
//                    COUNT(DISTINCT CASE WHEN solved = 1 THEN problem_id ELSE NULL END) as solved_count,
//                    CASE WHEN COUNT(DISTINCT problem_id) > 0 THEN
//                        ROUND(COUNT(DISTINCT CASE WHEN solved = 1 THEN problem_id ELSE NULL END) * 100.0 / COUNT(DISTINCT problem_id), 2)
//                    ELSE 0 END as acceptance_rate
//                FROM data_solved
//                WHERE is_set = 0 AND user_id = #{userId}
//                GROUP BY user_id
//            """)
    @Select("""
               SELECT
                ds.user_id,
                (SELECT COUNT(*) FROM data_submit WHERE user_id = ds.user_id AND is_set = 0 AND submit_type = 1) as total_submit_count,
                COUNT(DISTINCT ds.problem_id) as submitted_count,
                COUNT(DISTINCT CASE WHEN ds.solved = 1 THEN ds.problem_id ELSE NULL END) as solved_count,
                CASE
                    WHEN COUNT(DISTINCT ds.problem_id) > 0 THEN
                        ROUND(
                            COUNT(DISTINCT CASE WHEN ds.solved = 1 THEN ds.problem_id ELSE NULL END) * 100.0 /
                            COUNT(DISTINCT ds.problem_id), 2
                        )
                    ELSE 0
                END as acceptance_rate
            FROM data_solved ds
            WHERE ds.is_set = 0 AND user_id = #{userId}
            GROUP BY ds.user_id
            HAVING submitted_count > 0
            ORDER BY solved_count DESC, acceptance_rate DESC
             """)
    Map<String, Object> selectUserSolveStatisticsById(@Param("userId") String userId);





    /**
     * 获取题集整体通过率统计（基于已有提交的题目）
     *
     * @param setId 题集ID
     * @return 题集整体统计信息
     */
    @Select("""
        SELECT 
            COUNT(DISTINCT user_id) as totalParticipants,
            COUNT(DISTINCT CASE WHEN solved = 1 THEN user_id ELSE NULL END) as acceptedParticipants,
            CASE WHEN COUNT(DISTINCT user_id) > 0 THEN 
                ROUND(COUNT(DISTINCT CASE WHEN solved = 1 THEN user_id ELSE NULL END) * 100.0 / COUNT(DISTINCT user_id), 2)
            ELSE 0 END as acceptanceRate
        FROM data_solved
        WHERE set_id = #{setId}
        AND is_set = 1
        """)
    Map<String, Object> selectSetAcceptanceStats(@Param("setId") String setId);

    /**
     * 批量获取题集整体通过率统计（基于已有提交的题目）
     *
     * @param setIds 题集ID列表
     * @return 题集整体统计信息列表
     */
//    @Select("""
//        <script>
//        SELECT
//            set_id as setId,
//            COUNT(DISTINCT user_id) as totalParticipants,
//            COUNT(DISTINCT CASE WHEN solved = true THEN user_id ELSE NULL END) as acceptedParticipants,
//            CASE WHEN COUNT(DISTINCT user_id) > 0 THEN
//                ROUND(COUNT(DISTINCT CASE WHEN solved = true THEN user_id ELSE NULL END) * 100.0 / COUNT(DISTINCT user_id), 2)
//            ELSE 0 END as acceptanceRate
//        FROM data_solved
//        WHERE set_id IN
//        <foreach collection='setIds' item='id' open='(' separator=',' close=')'>
//            #{id}
//        </foreach>
//        AND is_set = 1
//        GROUP BY set_id
//        </script>
//        """)
//    List<Map<String, Object>> selectSetAcceptanceStatsBatch(@Param("setIds") List<String> setIds);

    @Select("""
            <script>
            SELECT 
                ds.set_id as setId,
                COUNT(DISTINCT ds.user_id) as totalParticipants,
                COUNT(DISTINCT CASE WHEN ds.solved = 1 THEN ds.user_id ELSE NULL END) as acceptedParticipants,
                CASE WHEN COUNT(DISTINCT ds.user_id) > 0 THEN 
                    ROUND(COUNT(DISTINCT CASE WHEN ds.solved = 1 THEN ds.user_id ELSE NULL END) * 100.0 / COUNT(DISTINCT ds.user_id), 2)
                ELSE 0 END as acceptanceRate,
                COALESCE(sub.submitCount, 0) as submitCount
            FROM data_solved ds
            LEFT JOIN (
                SELECT set_id, COUNT(*) as submitCount
                FROM data_submit
                WHERE set_id IN
                <foreach collection='setIds' item='id' open='(' separator=',' close=')'>
                    #{id}
                </foreach>
                AND is_set = 1
                AND submit_type = 1
                GROUP BY set_id
            ) sub ON ds.set_id = sub.set_id
            WHERE ds.set_id IN
            <foreach collection='setIds' item='id' open='(' separator=',' close=')'>
                #{id}
            </foreach>
            AND ds.is_set = 1
            GROUP BY ds.set_id, sub.submitCount
            </script>
            """)
    List<Map<String, Object>> selectSetAcceptanceStatsBatch(@Param("setIds") List<String> setIds);
}
