package github.jiangbyte.io.oj.modules.stat.mapper;

import github.jiangbyte.io.oj.modules.stat.result.OjDifficultyCountRow;
import github.jiangbyte.io.oj.modules.stat.result.OjUserHomepageResult;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.OffsetDateTime;
import java.util.List;

/**
 * 门户用户主页公开统计查询。
 * <p>
 * Author: Charlie
 */
@Mapper
public interface OjUserHomepageMapper {

    @Select("""
            SELECT p.difficulty AS difficulty, COUNT(*) AS count
            FROM oj_problem p
            WHERE p.status = 'PUBLISHED'
            GROUP BY p.difficulty
            """)
    List<OjDifficultyCountRow> selectPublishedByDifficulty();

    @Select("""
            SELECT p.difficulty AS difficulty, COUNT(*) AS count
            FROM oj_user_problem_stat s
            INNER JOIN oj_problem p ON p.id = s.problem_id AND p.status = 'PUBLISHED'
            WHERE s.account_id = #{accountId}
              AND s.status = 'ACCEPTED'
            GROUP BY p.difficulty
            """)
    List<OjDifficultyCountRow> selectAcceptedByDifficulty(@Param("accountId") String accountId);

    @Select("""
            SELECT COUNT(*)
            FROM oj_user_problem_stat s
            INNER JOIN oj_problem p ON p.id = s.problem_id AND p.status = 'PUBLISHED'
            WHERE s.account_id = #{accountId}
              AND s.status = 'ATTEMPTED'
            """)
    Long countAttempting(@Param("accountId") String accountId);

    @Select("""
            SELECT s.language AS language, COUNT(DISTINCT s.problem_id) AS solved_count
            FROM oj_submission s
            INNER JOIN oj_problem p ON p.id = s.problem_id AND p.status = 'PUBLISHED'
            WHERE s.account_id = #{accountId}
              AND s.status = 'AC'
            GROUP BY s.language
            ORDER BY solved_count DESC, s.language ASC
            """)
    List<OjUserHomepageResult.LanguageStatItem> selectLanguageSolved(@Param("accountId") String accountId);

    @Select("""
            SELECT DATE(DATE_ADD(s.created_at, INTERVAL 8 HOUR)) AS date,
                   COUNT(*) AS count
            FROM oj_submission s
            WHERE s.account_id = #{accountId}
              AND s.created_at >= #{since}
            GROUP BY DATE(DATE_ADD(s.created_at, INTERVAL 8 HOUR))
            ORDER BY date ASC
            """)
    List<OjUserHomepageResult.HeatmapDayItem> selectHeatmapDays(
            @Param("accountId") String accountId,
            @Param("since") OffsetDateTime since);

    @Select("""
            SELECT s.problem_id AS problem_id,
                   p.problem_key AS problem_key,
                   p.title AS title,
                   p.difficulty AS difficulty,
                   DATE_FORMAT(DATE_ADD(s.first_accepted_at, INTERVAL 8 HOUR), '%Y-%m-%d %H:%i:%s') AS accepted_at
            FROM oj_user_problem_stat s
            INNER JOIN oj_problem p ON p.id = s.problem_id AND p.status = 'PUBLISHED'
            WHERE s.account_id = #{accountId}
              AND s.status = 'ACCEPTED'
              AND s.first_accepted_at IS NOT NULL
            ORDER BY s.first_accepted_at DESC
            LIMIT #{limit}
            """)
    List<OjUserHomepageResult.RecentAcceptedItem> selectRecentAccepted(
            @Param("accountId") String accountId,
            @Param("limit") int limit);
}
