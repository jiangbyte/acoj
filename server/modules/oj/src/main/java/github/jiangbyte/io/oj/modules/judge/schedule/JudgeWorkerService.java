package github.jiangbyte.io.oj.modules.judge.schedule;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import github.jiangbyte.io.oj.config.OjProperties;
import github.jiangbyte.io.oj.modules.judge.dispatch.entity.OjJudgeDispatch;
import github.jiangbyte.io.oj.modules.judge.dispatch.mapper.OjJudgeDispatchMapper;
import github.jiangbyte.io.oj.modules.judge.enums.OjDispatchOutcome;
import github.jiangbyte.io.oj.modules.judge.enums.SandboxCaseStatus;
import github.jiangbyte.io.oj.modules.judge.mq.OjJudgeMessage;
import github.jiangbyte.io.oj.modules.judge.mq.OjJudgePublisher;
import github.jiangbyte.io.oj.modules.judge.node.entity.OjJudgeNode;
import github.jiangbyte.io.oj.modules.judge.sandbox.SparkSandboxClient;
import github.jiangbyte.io.oj.modules.problem.entity.OjProblem;
import github.jiangbyte.io.oj.modules.problem.mapper.OjProblemMapper;
import github.jiangbyte.io.oj.modules.problemlanguagelimit.entity.OjProblemLanguageLimit;
import github.jiangbyte.io.oj.modules.problemlanguagelimit.service.OjProblemLanguageLimitService;
import github.jiangbyte.io.oj.modules.stat.entity.OjUserProblemStat;
import github.jiangbyte.io.oj.modules.stat.enums.OjUserProblemStatStatus;
import github.jiangbyte.io.oj.modules.stat.mapper.OjUserProblemStatMapper;
import github.jiangbyte.io.oj.modules.submission.entity.OjSubmission;
import github.jiangbyte.io.oj.modules.submission.enums.OjVerdict;
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
    private final OjProblemLanguageLimitService ojProblemLanguageLimitService;
    private final OjJudgeDispatchMapper ojJudgeDispatchMapper;
    private final OjUserProblemStatMapper ojUserProblemStatMapper;
    private final NodeScheduler nodeScheduler;
    private final CaseLoader caseLoader;
    private final VerdictAggregator verdictAggregator;
    private final SparkSandboxClient sparkSandboxClient;
    private final OjJudgePublisher ojJudgePublisher;
    private final ObjectMapper objectMapper;

    private final String workerId = resolveWorkerId();

    /**
     * UpdateWrapper.set(JSON 字段) 默认不走实体 TypeHandler，会把 List/Map 以 binary 写入导致 MySQL JSON 报错。
     * 必须显式指定 typeHandler。
     */
    private static final String JSON_TYPE_HANDLER =
            "typeHandler=github.jiangbyte.io.common.mybatis.handler.JacksonJsonTypeHandler";

    /**
     * 消费一条判题消息：校验可领 → 选机占坑 → CAS 领取 → 调沙箱 → 裁决落库或换机重入队。
     * 边界：幂等（非 PENDING/不可回收则直接返回）；不负责消息 ACK（由 Consumer 处理）。
     */
    public void process(OjJudgeMessage message) {
        // 1. 加载提交；消息可能早于事务可见，短延迟重试避免误进 DLQ
        OjSubmission submission = ojSubmissionMapper.selectById(message.submissionId());
        if (submission == null) {
            log.warn("submission not found yet, retry soon: {}", message.submissionId());
            ojJudgePublisher.publishRetry(
                    OjJudgeMessage.of(
                            message.submissionId(),
                            message.requestId(),
                            OjJudgeMessage.REASON_RETRY_BACKOFF),
                    1000L);
            return;
        }

        // 2. 仅处理 PENDING，或租约已过期的 JUDGING（可被本 Worker 回收）
        String status = submission.getStatus();
        OffsetDateTime now = OffsetDateTime.now(ZoneOffset.UTC);
        boolean reclaimable = OjVerdict.JUDGING.matches(status)
                && submission.getJudgeLeaseUntil() != null
                && submission.getJudgeLeaseUntil().isBefore(now);
        if (!OjVerdict.PENDING.matches(status) && !reclaimable) {
            return;
        }

        // 2b. next_retry_at 未到：ack 跳过，依赖已有 delay 消息或补偿 Job（禁止二次灌队列）
        if (OjVerdict.PENDING.matches(status)
                && submission.getNextRetryAt() != null
                && submission.getNextRetryAt().isAfter(now)) {
            return;
        }

        // 3. 排队过久直接 SE，避免无限重试占队列
        if (exceededMaxWait(submission, now)) {
            finalizeSystemError(submission, "NODE_UNAVAILABLE", "等待可用节点超时");
            return;
        }

        // 4. 题目缺失无法判题，落 SE
        OjProblem problem = ojProblemMapper.selectById(submission.getProblemId());
        if (problem == null) {
            finalizeSystemError(submission, "PROBLEM_MISSING", "题目不存在");
            return;
        }

        String requestId = StringUtils.hasText(message.requestId())
                ? message.requestId()
                : UUID.randomUUID().toString().replace("-", "");

        // 5. 按语言选机并 CAS 占坑；无节点则退避重试
        OjJudgeNode node = nodeScheduler.selectAndAcquire(
                submission.getLanguage(),
                submission.getTriedNodeIds(),
                requestId);
        if (node == null) {
            handleNoNode(submission, requestId);
            return;
        }

        // 6. 准备新租约 token、tried 列表与 dispatch 次数
        String judgeToken = UUID.randomUUID().toString().replace("-", "");
        OffsetDateTime leaseUntil = now.plusSeconds(ojProperties.getJudge().getLeaseSeconds());
        List<String> tried = submission.getTriedNodeIds() == null
                ? new ArrayList<>()
                : new ArrayList<>(submission.getTriedNodeIds());
        if (!tried.contains(node.getId())) {
            tried.add(node.getId());
        }
        int nextDispatch = (submission.getDispatchCount() == null ? 0 : submission.getDispatchCount()) + 1;

        // 7. CAS 领取提交为 JUDGING；失败则释放已占坑
        boolean claimed;
        try {
            claimed = claimSubmission(submission.getId(), submission.getJudgeToken(),
                    judgeToken, leaseUntil, node.getId(), tried, nextDispatch, reclaimable);
        } catch (RuntimeException ex) {
            nodeScheduler.releaseInflight(node.getId());
            throw ex;
        }
        if (!claimed) {
            nodeScheduler.releaseInflight(node.getId());
            return;
        }

        // 8. 写 dispatch 开始记录；失败则回滚提交到 PENDING
        OjJudgeDispatch dispatch;
        try {
            dispatch = startDispatch(submission.getId(), node, nextDispatch, requestId, now);
        } catch (RuntimeException ex) {
            nodeScheduler.releaseInflight(node.getId());
            unlockAfterClaimFailure(submission.getId(), judgeToken, ex.getMessage());
            throw ex;
        }
        try {
            // 9. 加载测例 → 调沙箱 → 传输失败换机或正常裁决落库
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

    /**
     * CAS 领取：PENDING→JUDGING，或过期 JUDGING 用旧 token 回收。
     */
    private boolean claimSubmission(
            String submissionId,
            String previousToken,
            String judgeToken,
            OffsetDateTime leaseUntil,
            String nodeId,
            List<String> tried,
            int dispatchCount,
            boolean reclaimable) {
        // 1. 写入租约与调度元数据，JSON 字段显式 typeHandler
        var update = Wrappers.<OjSubmission>lambdaUpdate()
                .set(OjSubmission::getStatus, OjVerdict.JUDGING.name())
                .set(OjSubmission::getJudgeToken, judgeToken)
                .set(OjSubmission::getJudgeLeaseOwner, workerId)
                .set(OjSubmission::getJudgeLeaseUntil, leaseUntil)
                .set(OjSubmission::getJudgeNodeId, nodeId)
                .set(OjSubmission::getTriedNodeIds, tried, JSON_TYPE_HANDLER)
                .set(OjSubmission::getDispatchCount, dispatchCount)
                .set(OjSubmission::getLastDispatchError, null)
                .eq(OjSubmission::getId, submissionId);
        // 2. 回收路径校验旧 token；正常路径要求仍为 PENDING
        if (reclaimable) {
            update.eq(OjSubmission::getStatus, OjVerdict.JUDGING.name());
            if (StringUtils.hasText(previousToken)) {
                update.eq(OjSubmission::getJudgeToken, previousToken);
            }
        } else {
            update.eq(OjSubmission::getStatus, OjVerdict.PENDING.name());
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
        // outcome 列 NOT NULL 无默认值；开始时记 STARTED，结束时再覆盖
        dispatch.setOutcome(OjDispatchOutcome.STARTED.name());
        dispatch.setExtra(new HashMap<>());
        ojJudgeDispatchMapper.insert(dispatch);
        return dispatch;
    }

    /**
     * claim 成功但后续无法继续时，把提交退回 PENDING。
     */
    private void unlockAfterClaimFailure(String submissionId, String judgeToken, String message) {
        ojSubmissionMapper.update(null, Wrappers.<OjSubmission>lambdaUpdate()
                .set(OjSubmission::getStatus, OjVerdict.PENDING.name())
                .set(OjSubmission::getJudgeToken, null)
                .set(OjSubmission::getJudgeLeaseUntil, null)
                .set(OjSubmission::getJudgeLeaseOwner, null)
                .set(OjSubmission::getErrorCode, "DISPATCH_START_FAILED")
                .set(OjSubmission::getLastDispatchError, truncate(message, 512))
                .eq(OjSubmission::getId, submissionId)
                .eq(OjSubmission::getStatus, OjVerdict.JUDGING.name())
                .eq(OjSubmission::getJudgeToken, judgeToken));
    }

    /** 按语言限额组装沙箱请求并执行 run_cases。 */
    private SparkSandboxClient.RunCasesResult callSandbox(
            OjJudgeNode node,
            OjProblem problem,
            OjSubmission submission,
            List<CaseLoader.LoadedCase> cases) {
        // 1. 限额行即允许语言；缺失说明配置被删，属异常
        OjProblemLanguageLimit limit = ojProblemLanguageLimitService.findByProblemAndLanguage(
                problem.getId(), submission.getLanguage());
        if (limit == null) {
            throw new IllegalStateException("missing language limit: " + submission.getLanguage());
        }
        // 2. 实限 = CPU×系数；默认内存 256MiB
        int cpu = limit.getTimeLimitMs() == null ? 1000 : limit.getTimeLimitMs();
        int real = cpu * Math.max(1, ojProperties.getJudge().getRealTimeFactor());
        long mem = limit.getMemoryLimitBytes() == null ? 268435456L : limit.getMemoryLimitBytes();
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
                limit.getStackLimitBytes(),
                limit.getOutputLimitBytes());
        return sparkSandboxClient.runCases(node, req);
    }

    /** 业务裁决落库：CAS 写终态；成功则 bump 统计，失败则丢弃结果并释放 inflight。 */
    private void handleExecutionResult(
            OjSubmission submission,
            OjProblem problem,
            OjJudgeNode node,
            OjJudgeDispatch dispatch,
            String judgeToken,
            List<CaseLoader.LoadedCase> cases,
            SparkSandboxClient.RunCasesResult run) {
        // 1. 聚合沙箱响应为整单 verdict
        VerdictAggregator.AggregateResult verdict = verdictAggregator.aggregate(
                run.body(),
                cases,
                ojProperties.getJudge().isStopOnFirstError());

        // 2. internal_error 且无测点：按传输失败换机，而非直接落 SE
        if (OjVerdict.SE.matches(verdict.getStatus())
                && run.body() != null
                && SandboxCaseStatus.INTERNAL_ERROR.matches(run.body().path("status").asText())
                && (verdict.getCaseResults() == null || verdict.getCaseResults().isEmpty())) {
            handleTransportFail(submission, node, dispatch, dispatch.getRequestId(), judgeToken,
                    SparkSandboxClient.RunCasesResult.transportFail(run.httpStatus(), "sandbox internal_error", run.body()),
                    submission.getDispatchCount() == null ? 1 : submission.getDispatchCount());
            return;
        }

        // 3. CAS 写终态（须仍持有本轮 judgeToken）
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
                        verdict.getCaseResults() == null ? List.of() : verdict.getCaseResults(),
                        JSON_TYPE_HANDLER)
                .set(OjSubmission::getSandboxRaw, sandboxRaw, JSON_TYPE_HANDLER)
                .set(OjSubmission::getJudgedAt, judgedAt)
                .set(OjSubmission::getErrorCode, null)
                .set(OjSubmission::getLastDispatchError, null)
                .eq(OjSubmission::getId, submission.getId())
                .eq(OjSubmission::getStatus, OjVerdict.JUDGING.name())
                .eq(OjSubmission::getJudgeToken, judgeToken)) > 0;

        // 4. 关闭 dispatch（CAS finished_at）；仅本进程成功关闭时才动 inflight
        boolean dispatchClosed = finishDispatch(dispatch, OjDispatchOutcome.SUCCESS_RESULT.name(),
                run.httpStatus(), null, null, verdict.getStatus());
        if (cas) {
            if (dispatchClosed) {
                nodeScheduler.markSuccess(node.getId());
            }
            bumpProblemCounts(problem.getId(), OjVerdict.AC.matches(verdict.getStatus()));
            upsertUserStat(submission.getAccountId(), problem.getId(), OjVerdict.AC.matches(verdict.getStatus()), judgedAt);
        } else if (dispatchClosed) {
            // CAS 失败但本进程关了 dispatch：需自行释放；若已被 Reaper 关闭则跳过防双减
            nodeScheduler.releaseInflight(node.getId());
            log.info("CAS rejected for submission {}, discard result", submission.getId());
        } else {
            log.info("CAS rejected for submission {}, dispatch already closed, skip inflight release",
                    submission.getId());
        }
    }

    /** 传输/超时失败：关 dispatch、记节点失败，再换机或退避重试，超时则 SE。 */
    private void handleTransportFail(
            OjSubmission submission,
            OjJudgeNode node,
            OjJudgeDispatch dispatch,
            String requestId,
            String judgeToken,
            SparkSandboxClient.RunCasesResult run,
            int dispatchCount) {
        // 1. 区分 TIMEOUT / TRANSPORT_FAIL，CAS 关闭本轮 dispatch；成功才扣节点
        String err = truncate(run.errorMessage(), 512);
        String outcome = run.httpStatus() == 0 && err != null && err.toLowerCase().contains("timeout")
                ? OjDispatchOutcome.TIMEOUT.name()
                : OjDispatchOutcome.TRANSPORT_FAIL.name();
        boolean dispatchClosed = finishDispatch(dispatch, outcome, run.httpStatus() == 0 ? null : run.httpStatus(),
                outcome, err, null);
        if (dispatchClosed) {
            nodeScheduler.recordRunFailure(node.getId(), run.httpStatus(), err);
        }

        // 2. 非基础设施失败（如 4xx）：不换机，直接 SE
        if (!SparkSandboxClient.isNodeInfrastructureFailure(run.httpStatus())) {
            casBackToPendingThenSe(submission.getId(), judgeToken, "SANDBOX_REJECT", err);
            return;
        }

        // 3. 总等待超时：直接 SE
        OffsetDateTime now = OffsetDateTime.now(ZoneOffset.UTC);
        if (exceededMaxWait(submission, now)) {
            casBackToPendingThenSe(submission.getId(), judgeToken, "NODE_UNAVAILABLE", err);
            return;
        }

        // 4. 未达换机上限且仍有 Eligible：退 PENDING 立即 failover
        int maxDispatch = ojProperties.getJudge().getMaxDispatchPerSubmission();
        boolean canFailover = dispatchCount < maxDispatch
                && nodeScheduler.hasEligible(submission.getLanguage(), List.of());

        if (canFailover) {
            ojSubmissionMapper.update(null, Wrappers.<OjSubmission>lambdaUpdate()
                    .set(OjSubmission::getStatus, OjVerdict.PENDING.name())
                    .set(OjSubmission::getJudgeToken, null)
                    .set(OjSubmission::getJudgeLeaseUntil, null)
                    .set(OjSubmission::getJudgeLeaseOwner, null)
                    .set(OjSubmission::getLastDispatchError, err)
                    .set(OjSubmission::getErrorCode, "TRANSPORT_FAIL")
                    .eq(OjSubmission::getId, submission.getId())
                    .eq(OjSubmission::getStatus, OjVerdict.JUDGING.name())
                    .eq(OjSubmission::getJudgeToken, judgeToken));
            ojJudgePublisher.publishWork(OjJudgeMessage.of(submission.getId(), requestId, OjJudgeMessage.REASON_FAILOVER));
            return;
        }

        // 5. 否则指数退避后重试
        long backoff = backoffMs(dispatchCount);
        OffsetDateTime nextRetry = now.plusNanos(backoff * 1_000_000L);
        ojSubmissionMapper.update(null, Wrappers.<OjSubmission>lambdaUpdate()
                .set(OjSubmission::getStatus, OjVerdict.PENDING.name())
                .set(OjSubmission::getJudgeToken, null)
                .set(OjSubmission::getJudgeLeaseUntil, null)
                .set(OjSubmission::getJudgeLeaseOwner, null)
                .set(OjSubmission::getLastDispatchError, err)
                .set(OjSubmission::getErrorCode, "TRANSPORT_FAIL")
                .set(OjSubmission::getNextRetryAt, nextRetry)
                .eq(OjSubmission::getId, submission.getId())
                .eq(OjSubmission::getStatus, OjVerdict.JUDGING.name())
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
                .eq(OjSubmission::getStatus, OjVerdict.PENDING.name()));
        ojJudgePublisher.publishRetry(
                OjJudgeMessage.of(submission.getId(), requestId, OjJudgeMessage.REASON_RETRY_BACKOFF),
                backoff);
    }

    private void casBackToPendingThenSe(String submissionId, String judgeToken, String errorCode, String message) {
        OffsetDateTime now = OffsetDateTime.now(ZoneOffset.UTC);
        ojSubmissionMapper.update(null, Wrappers.<OjSubmission>lambdaUpdate()
                .set(OjSubmission::getStatus, OjVerdict.SE.name())
                .set(OjSubmission::getScore, 0)
                .set(OjSubmission::getJudgedAt, now)
                .set(OjSubmission::getErrorCode, errorCode)
                .set(OjSubmission::getLastDispatchError, truncate(message, 512))
                .set(OjSubmission::getJudgeMessage, truncate(message, 512))
                .set(OjSubmission::getCaseResults, List.of(), JSON_TYPE_HANDLER)
                .set(OjSubmission::getSandboxRaw, Map.of(), JSON_TYPE_HANDLER)
                .eq(OjSubmission::getId, submissionId)
                .eq(OjSubmission::getStatus, OjVerdict.JUDGING.name())
                .eq(OjSubmission::getJudgeToken, judgeToken));
    }

    private void finalizeSystemError(OjSubmission submission, String errorCode, String message) {
        OffsetDateTime now = OffsetDateTime.now(ZoneOffset.UTC);
        ojSubmissionMapper.update(null, Wrappers.<OjSubmission>lambdaUpdate()
                .set(OjSubmission::getStatus, OjVerdict.SE.name())
                .set(OjSubmission::getScore, 0)
                .set(OjSubmission::getJudgedAt, now)
                .set(OjSubmission::getErrorCode, errorCode)
                .set(OjSubmission::getLastDispatchError, truncate(message, 512))
                .set(OjSubmission::getJudgeMessage, truncate(message, 512))
                .set(OjSubmission::getCaseResults, List.of(), JSON_TYPE_HANDLER)
                .set(OjSubmission::getSandboxRaw, Map.of(), JSON_TYPE_HANDLER)
                .eq(OjSubmission::getId, submission.getId())
                .in(OjSubmission::getStatus, List.of(OjVerdict.PENDING.name(), OjVerdict.JUDGING.name())));
    }

    /**
     * CAS 关闭 open dispatch；返回是否由本进程成功关闭（用于决定是否释放 inflight）。
     */
    private boolean finishDispatch(
            OjJudgeDispatch dispatch,
            String outcome,
            Integer httpStatus,
            String errorCode,
            String errorMessage,
            String userVerdict) {
        if (dispatch == null || dispatch.getId() == null) {
            return false;
        }
        OffsetDateTime finishedAt = OffsetDateTime.now(ZoneOffset.UTC);
        Integer duration = null;
        if (dispatch.getStartedAt() != null) {
            duration = (int) Math.max(0, finishedAt.toInstant().toEpochMilli()
                    - dispatch.getStartedAt().toInstant().toEpochMilli());
        }
        return ojJudgeDispatchMapper.update(null, Wrappers.<OjJudgeDispatch>lambdaUpdate()
                .set(OjJudgeDispatch::getFinishedAt, finishedAt)
                .set(OjJudgeDispatch::getDurationMs, duration)
                .set(OjJudgeDispatch::getOutcome, outcome)
                .set(OjJudgeDispatch::getHttpStatus, httpStatus)
                .set(OjJudgeDispatch::getErrorCode, errorCode)
                .set(OjJudgeDispatch::getErrorMessage, truncate(errorMessage, 512))
                .set(OjJudgeDispatch::getUserVerdict, userVerdict)
                .eq(OjJudgeDispatch::getId, dispatch.getId())
                .isNull(OjJudgeDispatch::getFinishedAt)) > 0;
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
            row.setStatus(accepted
                    ? OjUserProblemStatStatus.ACCEPTED.name()
                    : OjUserProblemStatStatus.ATTEMPTED.name());
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
                    .set(OjUserProblemStat::getStatus, OjUserProblemStatStatus.ACCEPTED.name());
            if (existing.getFirstAcceptedAt() == null) {
                update.set(OjUserProblemStat::getFirstAcceptedAt, at);
            }
        } else if (!OjUserProblemStatStatus.ACCEPTED.matches(existing.getStatus())) {
            update.set(OjUserProblemStat::getStatus, OjUserProblemStatStatus.ATTEMPTED.name());
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
