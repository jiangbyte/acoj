package github.jiangbyte.io.oj.modules.judge.job;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import github.jiangbyte.io.common.job.JobHandler;
import github.jiangbyte.io.oj.modules.judge.node.entity.OjJudgeNode;
import github.jiangbyte.io.oj.modules.judge.node.mapper.OjJudgeNodeMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 在途对账：以 JUDGING 聚合为权威，条件修复 oj_judge_node.inflight_count。
 * <p>sys_job + Lock4j 单例；接受与 Worker 占坑的短暂 ±1 竞态，下周期收敛。
 *
 * Author: Charlie
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class OjJudgeInflightReconcileJob implements JobHandler {

    private final OjJudgeNodeMapper ojJudgeNodeMapper;

    @Override
    public String execute(String params) {
        List<OjNodeInflightCountRow> rows = ojJudgeNodeMapper.countJudgingByNode();
        Map<String, Long> judgingByNode = new HashMap<>();
        if (rows != null) {
            for (OjNodeInflightCountRow row : rows) {
                if (row == null || !StringUtils.hasText(row.getNodeId())) {
                    continue;
                }
                judgingByNode.put(row.getNodeId().trim(), row.getCnt() == null ? 0L : row.getCnt());
            }
        }

        List<OjJudgeNode> nodes = ojJudgeNodeMapper.selectList(
                Wrappers.<OjJudgeNode>lambdaQuery().select(OjJudgeNode::getId, OjJudgeNode::getInflightCount));
        int fixed = 0;
        int checked = 0;
        if (nodes != null) {
            for (OjJudgeNode node : nodes) {
                if (node == null || !StringUtils.hasText(node.getId())) {
                    continue;
                }
                checked++;
                int expected = judgingByNode.getOrDefault(node.getId(), 0L).intValue();
                int current = node.getInflightCount() == null ? 0 : node.getInflightCount();
                if (current == expected) {
                    continue;
                }
                int affected = ojJudgeNodeMapper.update(null, Wrappers.<OjJudgeNode>lambdaUpdate()
                        .set(OjJudgeNode::getInflightCount, expected)
                        .eq(OjJudgeNode::getId, node.getId())
                        .eq(OjJudgeNode::getInflightCount, current));
                if (affected > 0) {
                    fixed++;
                    log.warn("inflight reconcile drift nodeId={} was={} now={}",
                            node.getId(), current, expected);
                }
            }
        }
        return "checked=" + checked + ",fixed=" + fixed;
    }
}
