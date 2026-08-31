package github.jiangbyte.io.oj.modules.judge.job;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import github.jiangbyte.io.common.job.JobHandler;
import github.jiangbyte.io.oj.modules.judge.dispatch.entity.OjJudgeDispatch;
import github.jiangbyte.io.oj.modules.judge.dispatch.mapper.OjJudgeDispatchMapper;
import github.jiangbyte.io.oj.modules.judge.enums.OjDispatchOutcome;
import github.jiangbyte.io.oj.modules.judge.mq.OjJudgeMessage;
import github.jiangbyte.io.oj.modules.judge.mq.OjJudgePublisher;
import github.jiangbyte.io.oj.modules.judge.schedule.NodeScheduler;
import github.jiangbyte.io.oj.modules.submission.entity.OjSubmission;
import github.jiangbyte.io.oj.modules.submission.enums.OjVerdict;
import github.jiangbyte.io.oj.modules.submission.mapper.OjSubmissionMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * 租约回收：过期 JUDGING 改回 PENDING 并重新入队。
 * <p>由 sys_job + Lock4j 单例调度；关 dispatch CAS 成功才 release inflight。
 *
 * Author: Charlie
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class OjJudgeLeaseReaperJob implements JobHandler {

    private static final int SCAN_LIMIT = 100;
    private static final int IN_CHUNK = 500;

    private final OjSubmissionMapper ojSubmissionMapper;
    private final OjJudgeDispatchMapper ojJudgeDispatchMapper;
    private final NodeScheduler nodeScheduler;
    private final OjJudgePublisher ojJudgePublisher;

    @Override
    public String execute(String params) {
        OffsetDateTime now = OffsetDateTime.now(ZoneOffset.UTC);

        List<OjSubmission> expired = ojSubmissionMapper.selectList(
                Wrappers.<OjSubmission>lambdaQuery()
                        .eq(OjSubmission::getStatus, OjVerdict.JUDGING.name())
                        .lt(OjSubmission::getJudgeLeaseUntil, now)
                        .last("LIMIT " + SCAN_LIMIT));
        if (expired.isEmpty()) {
            return "reclaimed=0,enqueued=0";
        }

        List<OjSubmission> reclaimed = new ArrayList<>();
        for (OjSubmission submission : expired) {
            try {
                if (casReclaimSubmission(submission)) {
                    reclaimed.add(submission);
                }
            } catch (Exception ex) {
                log.warn("lease reap CAS failed submissionId={}: {}", submission.getId(), ex.toString());
            }
        }
        if (reclaimed.isEmpty()) {
            return "reclaimed=0,enqueued=0";
        }

        Map<String, OjJudgeDispatch> openBySubmission = loadLatestOpenDispatches(reclaimed);

        Set<String> nodesToRelease = new HashSet<>();
        for (OjSubmission submission : reclaimed) {
            OjJudgeDispatch open = openBySubmission.get(submission.getId());
            if (open != null) {
                if (finishDispatchCancelled(open, now) && StringUtils.hasText(open.getNodeId())) {
                    nodesToRelease.add(open.getNodeId());
                }
            } else if (StringUtils.hasText(submission.getJudgeNodeId())) {
                // 无 open dispatch：本轮占坑未落审计时仍释放，避免泄漏
                nodesToRelease.add(submission.getJudgeNodeId());
            }
        }
        for (String nodeId : nodesToRelease) {
            nodeScheduler.releaseInflight(nodeId);
        }

        int enqueued = 0;
        for (OjSubmission submission : reclaimed) {
            try {
                String requestId = UUID.randomUUID().toString().replace("-", "");
                ojJudgePublisher.publishWork(
                        OjJudgeMessage.of(submission.getId(), requestId, OjJudgeMessage.REASON_LEASE_REAP));
                enqueued++;
            } catch (Exception ex) {
                log.warn("lease reap enqueue failed submissionId={}: {}", submission.getId(), ex.toString());
            }
        }
        return "reclaimed=" + reclaimed.size() + ",enqueued=" + enqueued;
    }

    private boolean casReclaimSubmission(OjSubmission submission) {
        String token = submission.getJudgeToken();
        return ojSubmissionMapper.update(null, Wrappers.<OjSubmission>lambdaUpdate()
                .set(OjSubmission::getStatus, OjVerdict.PENDING.name())
                .set(OjSubmission::getJudgeToken, null)
                .set(OjSubmission::getJudgeLeaseUntil, null)
                .set(OjSubmission::getJudgeLeaseOwner, null)
                .set(OjSubmission::getErrorCode, "LEASE_EXPIRED")
                .set(OjSubmission::getLastDispatchError, "LEASE_EXPIRED")
                .eq(OjSubmission::getId, submission.getId())
                .eq(OjSubmission::getStatus, OjVerdict.JUDGING.name())
                .eq(StringUtils.hasText(token), OjSubmission::getJudgeToken, token)) > 0;
    }

    private Map<String, OjJudgeDispatch> loadLatestOpenDispatches(List<OjSubmission> reclaimed) {
        List<String> submissionIds = reclaimed.stream()
                .map(OjSubmission::getId)
                .filter(StringUtils::hasText)
                .distinct()
                .toList();
        Map<String, OjJudgeDispatch> latest = new HashMap<>();
        for (int i = 0; i < submissionIds.size(); i += IN_CHUNK) {
            List<String> batch = submissionIds.subList(i, Math.min(i + IN_CHUNK, submissionIds.size()));
            List<OjJudgeDispatch> rows = ojJudgeDispatchMapper.selectList(
                    Wrappers.<OjJudgeDispatch>lambdaQuery()
                            .in(OjJudgeDispatch::getSubmissionId, batch)
                            .isNull(OjJudgeDispatch::getFinishedAt)
                            .orderByDesc(OjJudgeDispatch::getStartedAt));
            for (OjJudgeDispatch row : rows) {
                if (row == null || !StringUtils.hasText(row.getSubmissionId())) {
                    continue;
                }
                latest.putIfAbsent(row.getSubmissionId(), row);
            }
        }
        return latest;
    }

    private boolean finishDispatchCancelled(OjJudgeDispatch open, OffsetDateTime now) {
        Integer duration = null;
        if (open.getStartedAt() != null) {
            duration = (int) Math.max(0, now.toInstant().toEpochMilli()
                    - open.getStartedAt().toInstant().toEpochMilli());
        }
        return ojJudgeDispatchMapper.update(null, Wrappers.<OjJudgeDispatch>lambdaUpdate()
                .set(OjJudgeDispatch::getFinishedAt, now)
                .set(OjJudgeDispatch::getDurationMs, duration)
                .set(OjJudgeDispatch::getOutcome, OjDispatchOutcome.CANCELLED_LEASE.name())
                .set(OjJudgeDispatch::getErrorCode, "LEASE_EXPIRED")
                .eq(OjJudgeDispatch::getId, open.getId())
                .isNull(OjJudgeDispatch::getFinishedAt)) > 0;
    }
}
