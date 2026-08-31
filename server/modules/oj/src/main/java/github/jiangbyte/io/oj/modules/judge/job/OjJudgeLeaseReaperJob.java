package github.jiangbyte.io.oj.modules.judge.job;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import github.jiangbyte.io.oj.modules.judge.dispatch.entity.OjJudgeDispatch;
import github.jiangbyte.io.oj.modules.judge.dispatch.mapper.OjJudgeDispatchMapper;
import github.jiangbyte.io.oj.modules.judge.mq.OjJudgeMessage;
import github.jiangbyte.io.oj.modules.judge.mq.OjJudgePublisher;
import github.jiangbyte.io.oj.modules.judge.schedule.NodeScheduler;
import github.jiangbyte.io.oj.modules.submission.entity.OjSubmission;
import github.jiangbyte.io.oj.modules.submission.mapper.OjSubmissionMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.UUID;

/**
 * 租约回收：过期 JUDGING 改回 PENDING 并重新入队。
 *
 * Author: Charlie
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class OjJudgeLeaseReaperJob {

    private final OjSubmissionMapper ojSubmissionMapper;
    private final OjJudgeDispatchMapper ojJudgeDispatchMapper;
    private final NodeScheduler nodeScheduler;
    private final OjJudgePublisher ojJudgePublisher;

    @Scheduled(fixedDelayString = "${hei.oj.judge.zombie-requeue-scan-ms:15000}")
    public void reap() {
        OffsetDateTime now = OffsetDateTime.now(ZoneOffset.UTC);
        List<OjSubmission> expired = ojSubmissionMapper.selectList(
                Wrappers.<OjSubmission>lambdaQuery()
                        .eq(OjSubmission::getStatus, "JUDGING")
                        .lt(OjSubmission::getJudgeLeaseUntil, now)
                        .last("LIMIT 100"));
        for (OjSubmission submission : expired) {
            try {
                reclaimOne(submission, now);
            } catch (Exception ex) {
                log.warn("lease reap failed submissionId={}: {}", submission.getId(), ex.toString());
            }
        }
    }

    private void reclaimOne(OjSubmission submission, OffsetDateTime now) {
        String token = submission.getJudgeToken();
        boolean ok = ojSubmissionMapper.update(null, Wrappers.<OjSubmission>lambdaUpdate()
                .set(OjSubmission::getStatus, "PENDING")
                .set(OjSubmission::getJudgeToken, null)
                .set(OjSubmission::getJudgeLeaseUntil, null)
                .set(OjSubmission::getJudgeLeaseOwner, null)
                .set(OjSubmission::getErrorCode, "LEASE_EXPIRED")
                .set(OjSubmission::getLastDispatchError, "LEASE_EXPIRED")
                .eq(OjSubmission::getId, submission.getId())
                .eq(OjSubmission::getStatus, "JUDGING")
                .eq(StringUtils.hasText(token), OjSubmission::getJudgeToken, token)) > 0;
        if (!ok) {
            return;
        }

        // 同一 token 只减一次：未结束的 dispatch 记 CANCELLED_LEASE
        OjJudgeDispatch open = ojJudgeDispatchMapper.selectOne(
                Wrappers.<OjJudgeDispatch>lambdaQuery()
                        .eq(OjJudgeDispatch::getSubmissionId, submission.getId())
                        .eq(StringUtils.hasText(submission.getJudgeNodeId()),
                                OjJudgeDispatch::getNodeId, submission.getJudgeNodeId())
                        .isNull(OjJudgeDispatch::getFinishedAt)
                        .orderByDesc(OjJudgeDispatch::getStartedAt)
                        .last("LIMIT 1"));
        if (open != null) {
            Integer duration = null;
            if (open.getStartedAt() != null) {
                duration = (int) Math.max(0, now.toInstant().toEpochMilli()
                        - open.getStartedAt().toInstant().toEpochMilli());
            }
            int finished = ojJudgeDispatchMapper.update(null, Wrappers.<OjJudgeDispatch>lambdaUpdate()
                    .set(OjJudgeDispatch::getFinishedAt, now)
                    .set(OjJudgeDispatch::getDurationMs, duration)
                    .set(OjJudgeDispatch::getOutcome, "CANCELLED_LEASE")
                    .set(OjJudgeDispatch::getErrorCode, "LEASE_EXPIRED")
                    .eq(OjJudgeDispatch::getId, open.getId())
                    .isNull(OjJudgeDispatch::getFinishedAt));
            if (finished > 0 && StringUtils.hasText(open.getNodeId())) {
                nodeScheduler.releaseInflight(open.getNodeId());
            }
        } else if (StringUtils.hasText(submission.getJudgeNodeId())) {
            nodeScheduler.releaseInflight(submission.getJudgeNodeId());
        }

        String requestId = UUID.randomUUID().toString().replace("-", "");
        ojJudgePublisher.publishWork(
                OjJudgeMessage.of(submission.getId(), requestId, OjJudgeMessage.REASON_LEASE_REAP));
    }
}
