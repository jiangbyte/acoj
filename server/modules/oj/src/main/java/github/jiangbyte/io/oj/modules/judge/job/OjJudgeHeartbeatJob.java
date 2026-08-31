package github.jiangbyte.io.oj.modules.judge.job;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import github.jiangbyte.io.common.job.JobHandler;
import github.jiangbyte.io.oj.config.OjProperties;
import github.jiangbyte.io.oj.modules.judge.node.entity.OjJudgeNode;
import github.jiangbyte.io.oj.modules.judge.node.mapper.OjJudgeNodeMapper;
import github.jiangbyte.io.oj.modules.judge.schedule.NodeScheduler;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 被动心跳超时：超时未上报则 OFFLINE；并推进熔断半开。
 * <p>由 sys_job + Lock4j 单例调度（默认 FIXED 15s）。
 *
 * Author: Charlie
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class OjJudgeHeartbeatJob implements JobHandler {

    private final OjJudgeNodeMapper ojJudgeNodeMapper;
    private final NodeScheduler nodeScheduler;
    private final OjProperties ojProperties;

    @Override
    public String execute(String params) {
        nodeScheduler.transitionOpenToHalfOpen();

        long staleMs = Math.max(15_000L, ojProperties.getJudge().getHeartbeatStaleMs());
        OffsetDateTime threshold = OffsetDateTime.now(ZoneOffset.UTC).minus(Duration.ofMillis(staleMs));

        int affected = ojJudgeNodeMapper.update(null, Wrappers.<OjJudgeNode>lambdaUpdate()
                .set(OjJudgeNode::getRuntimeStatus, "OFFLINE")
                .setSql("epoch = IFNULL(epoch, 0) + 1")
                .eq(OjJudgeNode::getRuntimeStatus, "ONLINE")
                .and(w -> w.isNull(OjJudgeNode::getLastHeartbeatAt)
                        .or()
                        .lt(OjJudgeNode::getLastHeartbeatAt, threshold)));
        if (affected > 0) {
            log.info("marked {} judge node(s) OFFLINE due to stale heartbeat (threshold={})",
                    affected, threshold);
        }
        return "offline=" + affected;
    }
}
