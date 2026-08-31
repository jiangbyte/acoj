package github.jiangbyte.io.oj.modules.judge.node.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import github.jiangbyte.io.oj.modules.judge.job.OjNodeInflightCountRow;
import github.jiangbyte.io.oj.modules.judge.node.entity.OjJudgeNode;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * OJ 执行机 Mapper。
 *
 * Author: Charlie
 */
@Mapper
public interface OjJudgeNodeMapper extends BaseMapper<OjJudgeNode> {

    /**
     * 按 judge_node_id 聚合 JUDGING 数量（inflight 对账权威源）。
     */
    @Select("""
            SELECT judge_node_id AS node_id, COUNT(*) AS cnt
            FROM oj_submission
            WHERE status = 'JUDGING'
              AND judge_node_id IS NOT NULL
              AND judge_node_id <> ''
            GROUP BY judge_node_id
            """)
    List<OjNodeInflightCountRow> countJudgingByNode();
}
