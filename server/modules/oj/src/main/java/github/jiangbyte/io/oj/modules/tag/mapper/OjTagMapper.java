package github.jiangbyte.io.oj.modules.tag.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import github.jiangbyte.io.oj.modules.tag.entity.OjTag;
import github.jiangbyte.io.oj.modules.tag.result.OjTagOptionItem;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * OJ 题目标签 Mapper。
 *
 * Author: Charlie
 */
@Mapper
public interface OjTagMapper extends BaseMapper<OjTag> {

    /**
     * 启用标签 + 已发布题目数（单次聚合，避免按标签 N+1）。
     */
    @Select("""
            SELECT t.id AS id,
                   t.name AS name,
                   COUNT(DISTINCT CASE WHEN p.status = 'PUBLISHED' THEN pt.problem_id END) AS problem_count
            FROM oj_tag t
            LEFT JOIN oj_problem_tag pt ON pt.tag_id = t.id
            LEFT JOIN oj_problem p ON p.id = pt.problem_id
            WHERE t.status = 'ENABLED'
            GROUP BY t.id, t.name
            ORDER BY problem_count DESC, t.name ASC
            """)
    List<OjTagOptionItem> selectEnabledWithPublishedCounts();
}
