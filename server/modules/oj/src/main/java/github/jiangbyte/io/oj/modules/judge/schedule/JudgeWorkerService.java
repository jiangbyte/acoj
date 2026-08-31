package github.jiangbyte.io.oj.modules.judge.schedule;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import github.jiangbyte.io.oj.config.OjProperties;
import github.jiangbyte.io.oj.modules.judge.dispatch.entity.OjJudgeDispatch;
import github.jiangbyte.io.oj.modules.judge.dispatch.mapper.OjJudgeDispatchMapper;
import github.jiangbyte.io.oj.modules.judge.mq.OjJudgeMessage;
import github.jiangbyte.io.oj.modules.judge.mq.OjJudgePublisher;
import github.jiangbyte.io.oj.modules.judge.node.entity.OjJudgeNode;
import github.jiangbyte.io.oj.modules.judge.sandbox.SparkSandboxClient;
import github.jiangbyte.io.oj.modules.problem.entity.OjProblem;
import github.jiangbyte.io.oj.modules.problem.mapper.OjProblemMapper;
import github.jiangbyte.io.oj.modules.stat.entity.OjUserProblemStat;
import github.jiangbyte.io.oj.modules.stat.mapper.OjUserProblemStatMapper;
import github.jiangbyte.io.oj.modules.submission.entity.OjSubmission;
import github.jiangbyte.io.oj.modules.submission.mapper.OjSubmissionMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.net.InetAddress;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * 判题 Worker：领取提交、选机、调沙箱、裁决落库、换机重入队。
 *
 * Author: Charlie
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class JudgeWorkerService {

    private final OjProperties ojProperties;
    private final OjSubmissionMapper ojSubmissionMapper;
    private final OjProblemMapper ojProblemMapper;
    private final OjJudgeDispatchMapper ojJudgeDispatchMapper;
    private final OjUserProblemStatMapper ojUserProblemStatMapper;
    private final NodeScheduler nodeScheduler;
    private final CaseLoader caseLoader;
    private final VerdictAggregator verdictAggregator;
    private final SparkSandboxClient sparkSandboxClient;
    private final OjJudgePublisher ojJudgePublisher;
    private final ObjectMapper objectMapper;

    private final String workerId = resolveWorkerId();

    public void process(OjJudgeMessage message) {
        OjSubmission submission = ojSubmissionMapper.selectById(message.submissionId());
        if (submission == null) {
            log.warn("submission not found, to dlq: {}", message.submissionId());
            ojJudgePublisher.publishDlq(message);
            return;
        }

        String status = submission.getStatus();
        OffsetDateTime now = OffsetDateTime.now(ZoneOffset.UTC);
        boolean reclaimable = "JUDGING".equals(status)
                && submission.getJudgeLeaseUntil() != null
                && submission.getJudgeLeaseUntil().isBefore(now);
        if (!"PENDING".equals(status) && !reclaimable) {
            // 已终态或他 Worker 持有有效租约
            return;
        }

        if (exceededMaxWait(submission, now)) {
            finalizeSystemError(submission, "NODE_UNAVAILABLE", "等待可用节点超时");
            return;
        }

        OjProblem problem = ojProblemMapper.selectById(submission.getProblemId());
        if (problem == null) {
            finalizeSystemError(submission, "PROBLEM_MISSING", "题目不存在");
            return;
        }

        String requestId = StringUtils.hasText(message.requestId())
                ? message.requestId()
                : UUID.randomUUID().toString().replace("-", "");

        OjJudgeNode node = nodeScheduler.selectAndAcquire(
                submission.getLanguage(),
                submission.getTriedNodeIds(),
                requestId);
        if (node == null) {
            handleNoNode(submission, requestId);
            return;
        }

        String judgeToken = UUID.randomUUID().toString().replace("-", "");
        OffsetDateTime leaseUntil = now.plusSeconds(ojProperties.getJudge().getLeaseSeconds());
        List<String> tried = submission.getTriedNodeIds() == null
                ? new ArrayList<>()
                : new ArrayList<>(submission.getTriedNodeIds());
        if (!tried.contains(node.getId())) {
            tried.add(node.getId());
        }
        int nextDispatch = (submission.getDispatchCount() == null ? 0 : submission.getDispatchCount()) + 1;

        boolean claimed = claimSubmission(submission.getId(), submission.getJudgeToken(),
                judgeToken, leaseUntil, node.getId(), tried, nextDispatch, reclaimable);
        if (!claimed) {
            nodeScheduler.releaseInflight(node.getId());
            return;
        }

        OjJudgeDispatch dispatch = startDispatch(submission.getId(), node, nextDispatch, requestId, now);
        try {
            List<CaseLoader.LoadedCase> cases = caseLoader.load(problem.getId(), submission.getCaseVersion());
            SparkSandboxClient.RunCasesResult run = callSandbox(node, problem, submission, cases);
            if (run.transportFail()) {
                handleTransportFail(submission, node, dispatch, requestId, judgeToken, run, nextDispatch);
                return;
            }
            handleExecutionResult(submission, problem, node, dispatch, judgeToken, cases, run);
        } catch (Exception ex) {
            log.error("judge worker error submissionId={}", submission.getId(), ex);
            handleTransportFail(submission, node, dispatch, requestId, judgeToken,
                    SparkSandboxClient.RunCasesResult.transportFail(0, ex.getMessage(), null),
                    nextDispatch);
        }
    }

    private boolean claimSubmission(
            String submissionId,
            String previousToken,
            String judgeToken,
            OffsetDateTime leaseUntil,
            String nodeId,
            List<String> tried,
            int dispatchCount,
            boolean reclaimable) {
        var update = Wrappers.<OjSubmission>lambdaUpdate()
                .set(OjSubmission::getStatus, "JUDGING")
                .set(OjSubmission::getJudgeToken, judgeToken)
                .set(OjSubmission::getJudgeLeaseOwner, workerId)
                .set(OjSubmission::getJudgeLeaseUntil, leaseUntil)
                .set(OjSubmission::getJudgeNodeId, nodeId)
                .set(OjSubmission::getTriedNodeIds, tried)
                .set(OjSubmission::getDispatchCount, dispatchCount)
                .set(OjSubmission::getLastDispatchError, null)
                .eq(OjSubmission::getId, submissionId);
        if (reclaimable) {
            update.eq(OjSubmission::getStatus, "JUDGING");
            if (StringUtils.hasText(previousToken)) {
                update.eq(OjSubmission::getJudgeToken, previousToken);
            }
        } else {
            update.eq(OjSubmission::getStatus, "PENDING");
        }
        return ojSubmissionMapper.update(null, update) > 0;
    }

    private OjJudgeDispatch startDispatch(
            String submissionId,
            OjJudgeNode node,
            int attemptNo,
            String requestId,
            OffsetDateTime startedAt) {
        OjJudgeDispatch dispatch = new OjJudgeDispatch();
        dispatch.setSubmissionId(submissionId);
        dispatch.setNodeId(node.getId());
        dispatch.setNodeEpoch(node.getEpoch());
        dispatch.setAttemptNo(attemptNo);
        dispatch.setWorkerId(workerId);
        dispatch.setRequestId(requestId);
        dispatch.setStartedAt(startedAt);
        dispatch.setExtra(new HashMap<>());
        ojJudgeDispatchMapper.insert(dispatch);
        return dispatch;
    }

    private SparkSandboxClient.RunCasesResult callSandbox(
            OjJudgeNode node,
            OjProblem problem,
            OjSubmission submission,
            List<CaseLoader.LoadedCase> cases) {
        int cpu = problem.getTimeLimitMs() == null ? 1000 : problem.getTimeLimitMs();
        int real = cpu * Math.max(1, ojProperties.getJudge().getRealTimeFactor());
        long mem = problem.getMemoryLimitBytes() == null ? 268435456L : problem.getMemoryLimitBytes();
        List<SparkSandboxClient.CaseInput> inputs = cases.stream()
                .map(c -> new SparkSandboxClient.CaseInput(c.caseKey(), c.stdin()))
                .toList();
        SparkSandboxClient.RunCasesRequest req = new SparkSandboxClient.RunCasesRequest(
                submission.getLanguage(),
                submission.getSourceCode(),
                inputs,
                ojProperties.getJudge().isStopOnFirstError(),
                ojProperties.getJudge().getCaseParallelism(),
                cpu,
                real,
                mem,
                problem.getStackLimitBytes(),
                problem.getOutputLimitBytes());
        return sparkSandboxClient.runCases(node, req);
    }

    private void handleExecutionResult(
            OjSubmission submission,
            OjProblem problem,
            OjJudgeNode node,
            OjJudgeDispatch dispatch,
            String judgeToken,
            List<CaseLoader.LoadedCase> cases,
            SparkSandboxClient.RunCasesResult run) {
        VerdictAggregator.AggregateResult verdict = verdictAggregator.aggregate(
                run.body(),
                cases,
                ojProperties.getJudge().isStopOnFirstError());

        // internal_error 且无有效测点时按换机处理
        if ("SE".equals(verdict.getStatus())
                && run.body() != null
                && "internal_error".equalsIgnoreCase(run.body().path("status").asText())
                && (verdict.getCaseResults() == null || verdict.getCaseResults().isEmpty())) {
            handleTransportFail(submission, node, dispatch, dispatch.getRequestId(), judgeToken,
                    SparkSandboxClient.RunCasesResult.transportFail(run.httpStatus(), "sandbox internal_error", run.body()),
                    submission.getDispatchCount() == null ? 1 : submission.getDispatchCount());
            return;
        }

        OffsetDateTime judgedAt = OffsetDateTime.now(ZoneOffset.UTC);
        Map<String, Object> sandboxRaw = truncateSandboxRaw(run.body());
        boolean cas = ojSubmissionMapper.update(null, Wrappers.<OjSubmission>lambdaUpdate()
                .set(OjSubmission::getStatus, verdict.getStatus())
                .set(OjSubmission::getScore, verdict.getScore())
                .set(OjSubmission::getTimeMs, verdict.getTimeMs())
                .set(OjSubmission::getMemoryBytes, verdict.getMemoryBytes())
                .set(OjSubmission::getCompileOutput, truncate(verdict.getCompileOutput(), 65000))
                .set(OjSubmission::getJudgeMessage, truncate(verdict.getJudgeMessage(), 512))
                .set(OjSubmission::getCaseResults,
                        verdict.getCaseResults() == null ? List.of() : verdict.getCaseResults())
                .set(OjSubmission::getSandboxRaw, sandboxRaw)
                .set(OjSubmission::getJudgedAt, judgedAt)
                .set(OjSubmission::getErrorCode, null)
                .set(OjSubmission::getLastDispatchError, null)
                .eq(OjSubmission::getId, submission.getId())
                .eq(OjSubmission::getStatus, "JUDGING")
                .eq(OjSubmission::getJudgeToken, judgeToken)) > 0;

        finishDispatch(dispatch, "SUCCESS_RESULT", run.httpStatus(), null, null, verdict.getStatus());
        if (cas) {
            nodeScheduler.markSuccess(node.getId());
            bumpProblemCounts(problem.getId(), "AC".equals(verdict.getStatus()));
            upsertUserStat(submission.getAccountId(), problem.getId(), "AC".equals(verdict.getStatus()), judgedAt);
        } else {
            // CAS 失败：结果丢弃，仍释放本轮 inflight（若已被 reaper 处理则 GREATEST 兜底）
            nodeScheduler.releaseInflight(node.getId());
            log.info("CAS rejected for submission {}, discard result", submission.getId());
        }
    }

    private void handleTransportFail(
            OjSubmission submission,
            OjJudgeNode node,
            OjJudgeDispatch dispatch,
            String requestId,
            String judgeToken,
            SparkSandboxClient.RunCasesResult run,
            int dispatchCount) {
        String err = truncate(run.errorMessage(), 512);
        String outcome = run.httpStatus() == 0 && err != null && err.toLowerCase().contains("timeout")
                ? "TIMEOUT"
                : "TRANSPORT_FAIL";
        finishDispatch(dispatch, outcome, run.httpStatus() == 0 ? null : run.httpStatus(),
                outcome, err, null);
        nodeScheduler.recordRunFailure(node.getId(), run.httpStatus(), err);

        OffsetDateTime now = OffsetDateTime.now(ZoneOffset.UTC);
        if (exceededMaxWait(submission, now)) {
            casBackToPendingThenSe(submission.getId(), judgeToken, "NODE_UNAVAILABLE", err);
            return;
        }

        int maxDispatch = ojProperties.getJudge().getMaxDispatchPerSubmission();
        boolean canFailover = dispatchCount < maxDispatch
                && nodeScheduler.hasEligible(submission.getLanguage(), List.of());

        if (canFailover) {
            ojSubmissionMapper.update(null, Wrappers.<OjSubmission>lambdaUpdate()
                    .set(OjSubmission::getStatus, "PENDING")
                    .set(OjSubmission::getJudgeToken, null)
                    .set(OjSubmission::getJudgeLeaseUntil, null)
                    .set(OjSubmission::getJudgeLeaseOwner, null)
                    .set(OjSubmission::getLastDispatchError, err)
                    .set(OjSubmission::getErrorCode, "TRANSPORT_FAIL")
                    .eq(OjSubmission::getId, submission.getId())
                    .eq(OjSubmission::getStatus, "JUDGING")
                    .eq(OjSubmission::getJudgeToken, judgeToken));
            ojJudgePublisher.publishWork(OjJudgeMessage.of(submission.getId(), requestId, OjJudgeMessage.REASON_FAILOVER));
            return;
        }

        long backoff = backoffMs(dispatchCount);
        OffsetDateTime nextRetry = now.plusNanos(backoff * 1_000_000L);
        ojSubmissionMapper.update(null, Wrappers.<OjSubmission>lambdaUpdate()
                .set(OjSubmission::getStatus, "PENDING")
                .set(OjSubmission::getJudgeToken, null)
                .set(OjSubmission::getJudgeLeaseUntil, null)
                .set(OjSubmission::getJudgeLeaseOwner, null)
                .set(OjSubmission::getLastDispatchError, err)
                .set(OjSubmission::getErrorCode, "TRANSPORT_FAIL")
                .set(OjSubmission::getNextRetryAt, nextRetry)
                .eq(OjSubmission::getId, submission.getId())
                .eq(OjSubmission::getStatus, "JUDGING")
                .eq(OjSubmission::getJudgeToken, judgeToken));
        ojJudgePublisher.publishRetry(
                OjJudgeMessage.of(submission.getId(), requestId, OjJudgeMessage.REASON_RETRY_BACKOFF),
                backoff);
    }

    private void handleNoNode(OjSubmission submission, String requestId) {
        OffsetDateTime now = OffsetDateTime.now(ZoneOffset.UTC);
        if (exceededMaxWait(submission, now)) {
            finalizeSystemError(submission, "NODE_UNAVAILABLE", "无可用执行机");
            return;
        }
        int dispatchCount = submission.getDispatchCount() == null ? 0 : submission.getDispatchCount();
        long backoff = backoffMs(Math.max(1, dispatchCount));
        ojSubmissionMapper.update(null, Wrappers.<OjSubmission>lambdaUpdate()
                .set(OjSubmission::getLastDispatchError, "NO_ONLINE_NODE")
                .set(OjSubmission::getErrorCode, "NO_ONLINE_NODE")
                .set(OjSubmission::getNextRetryAt, now.plusNanos(backoff * 1_000_000L))
                .eq(OjSubmission::getId, submission.getId())
                .eq(OjSubmission::getStatus, "PENDING"));
        ojJudgePublisher.publishRetry(
                OjJudgeMessage.of(submission.getId(), requestId, OjJudgeMessage.REASON_RETRY_BACKOFF),
                backoff);
    }

    private void casBackToPendingThenSe(String submissionId, String judgeToken, String errorCode, String message) {
        OffsetDateTime now = OffsetDateTime.now(ZoneOffset.UTC);
        ojSubmissionMapper.update(null, Wrappers.<OjSubmission>lambdaUpdate()
                .set(OjSubmission::getStatus, "SE")
                .set(OjSubmission::getScore, 0)
                .set(OjSubmission::getJudgedAt, now)
                .set(OjSubmission::getErrorCode, errorCode)
                .set(OjSubmission::getLastDispatchError, truncate(message, 512))
                .set(OjSubmission::getJudgeMessage, truncate(message, 512))
                .set(OjSubmission::getCaseResults, List.of())
                .set(OjSubmission::getSandboxRaw, Map.of())
                .eq(OjSubmission::getId, submissionId)
                .eq(OjSubmission::getStatus, "JUDGING")
                .eq(OjSubmission::getJudgeToken, judgeToken));
    }

    private void finalizeSystemError(OjSubmission submission, String errorCode, String message) {
        OffsetDateTime now = OffsetDateTime.now(ZoneOffset.UTC);
        ojSubmissionMapper.update(null, Wrappers.<OjSubmission>lambdaUpdate()
                .set(OjSubmission::getStatus, "SE")
                .set(OjSubmission::getScore, 0)
                .set(OjSubmission::getJudgedAt, now)
                .set(OjSubmission::getErrorCode, errorCode)
                .set(OjSubmission::getLastDispatchError, truncate(message, 512))
                .set(OjSubmission::getJudgeMessage, truncate(message, 512))
                .set(OjSubmission::getCaseResults, List.of())
                .set(OjSubmission::getSandboxRaw, Map.of())
                .eq(OjSubmission::getId, submission.getId())
                .in(OjSubmission::getStatus, List.of("PENDING", "JUDGING")));
    }

    private void finishDispatch(
            OjJudgeDispatch dispatch,
            String outcome,
            Integer httpStatus,
            String errorCode,
            String errorMessage,
            String userVerdict) {
        if (dispatch == null || dispatch.getId() == null) {
            return;
        }
        OffsetDateTime finishedAt = OffsetDateTime.now(ZoneOffset.UTC);
        Integer duration = null;
        if (dispatch.getStartedAt() != null) {
            duration = (int) Math.max(0, finishedAt.toInstant().toEpochMilli()
                    - dispatch.getStartedAt().toInstant().toEpochMilli());
        }
        ojJudgeDispatchMapper.update(null, Wrappers.<OjJudgeDispatch>lambdaUpdate()
                .set(OjJudgeDispatch::getFinishedAt, finishedAt)
                .set(OjJudgeDispatch::getDurationMs, duration)
                .set(OjJudgeDispatch::getOutcome, outcome)
                .set(OjJudgeDispatch::getHttpStatus, httpStatus)
                .set(OjJudgeDispatch::getErrorCode, errorCode)
                .set(OjJudgeDispatch::getErrorMessage, truncate(errorMessage, 512))
                .set(OjJudgeDispatch::getUserVerdict, userVerdict)
                .eq(OjJudgeDispatch::getId, dispatch.getId())
                .isNull(OjJudgeDispatch::getFinishedAt));
    }

    @Transactional
    protected void bumpProblemCounts(String problemId, boolean accepted) {
        if (accepted) {
            ojProblemMapper.update(null, Wrappers.<OjProblem>lambdaUpdate()
                    .setSql("submit_count = IFNULL(submit_count, 0) + 1")
                    .setSql("accept_count = IFNULL(accept_count, 0) + 1")
                    .eq(OjProblem::getId, problemId));
        } else {
            ojProblemMapper.update(null, Wrappers.<OjProblem>lambdaUpdate()
                    .setSql("submit_count = IFNULL(submit_count, 0) + 1")
                    .eq(OjProblem::getId, problemId));
        }
    }

    @Transactional
    protected void upsertUserStat(String accountId, String problemId, boolean accepted, OffsetDateTime at) {
        OjUserProblemStat existing = ojUserProblemStatMapper.selectOne(
                Wrappers.<OjUserProblemStat>lambdaQuery()
                        .eq(OjUserProblemStat::getAccountId, accountId)
                        .eq(OjUserProblemStat::getProblemId, problemId)
                        .last("LIMIT 1"));
        if (existing == null) {
            OjUserProblemStat row = new OjUserProblemStat();
            row.setAccountId(accountId);
            row.setProblemId(problemId);
            row.setStatus(accepted ? "ACCEPTED" : "ATTEMPTED");
            row.setAttemptCount(1);
            row.setAcceptedCount(accepted ? 1 : 0);
            row.setFirstAcceptedAt(accepted ? at : null);
            row.setLastSubmitAt(at);
            row.setExtra(new HashMap<>());
            ojUserProblemStatMapper.insert(row);
            return;
        }
        var update = Wrappers.<OjUserProblemStat>lambdaUpdate()
                .setSql("attempt_count = IFNULL(attempt_count, 0) + 1")
                .set(OjUserProblemStat::getLastSubmitAt, at)
                .eq(OjUserProblemStat::getId, existing.getId());
        if (accepted) {
            update.setSql("accepted_count = IFNULL(accepted_count, 0) + 1")
                    .set(OjUserProblemStat::getStatus, "ACCEPTED");
            if (existing.getFirstAcceptedAt() == null) {
                update.set(OjUserProblemStat::getFirstAcceptedAt, at);
            }
        } else if (!"ACCEPTED".equals(existing.getStatus())) {
            update.set(OjUserProblemStat::getStatus, "ATTEMPTED");
        }
        ojUserProblemStatMapper.update(null, update);
    }

    private boolean exceededMaxWait(OjSubmission submission, OffsetDateTime now) {
        OffsetDateTime queuedAt = submission.getQueuedAt() != null
                ? submission.getQueuedAt()
                : submission.getCreatedAt();
        if (queuedAt == null) {
            return false;
        }
        long waited = now.toInstant().toEpochMilli() - queuedAt.toInstant().toEpochMilli();
        return waited >= ojProperties.getJudge().getMaxWaitMs();
    }

    private static long backoffMs(int dispatchCount) {
        long[] table = {1000L, 2000L, 5000L, 10000L, 20000L, 40000L, 60000L};
        int idx = Math.min(Math.max(dispatchCount - 1, 0), table.length - 1);
        return table[idx];
    }

    private Map<String, Object> truncateSandboxRaw(JsonNode body) {
        if (body == null || body.isNull() || body.isMissingNode()) {
            return new HashMap<>();
        }
        try {
            String json = objectMapper.writeValueAsString(body);
            int max = ojProperties.getJudge().getSandboxRawMaxBytes();
            if (json.length() > max) {
                return Map.of("truncated", true, "preview", json.substring(0, max));
            }
            @SuppressWarnings("unchecked")
            Map<String, Object> map = objectMapper.convertValue(body, Map.class);
            return map == null ? new HashMap<>() : map;
        } catch (Exception ex) {
            return Map.of("error", "serialize_failed");
        }
    }

    private static String truncate(String s, int max) {
        if (s == null) {
            return null;
        }
        return s.length() <= max ? s : s.substring(0, max);
    }

    private static String resolveWorkerId() {
        try {
            return InetAddress.getLocalHost().getHostName() + "-"
                    + UUID.randomUUID().toString().substring(0, 8);
        } catch (Exception ex) {
            return "worker-" + UUID.randomUUID();
        }
    }
}
