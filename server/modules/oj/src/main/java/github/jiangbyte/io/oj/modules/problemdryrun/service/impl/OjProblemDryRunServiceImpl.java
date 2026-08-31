package github.jiangbyte.io.oj.modules.problemdryrun.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import github.jiangbyte.io.common.core.exception.BizException;
import github.jiangbyte.io.common.log.audit.AuditSnapshots;
import github.jiangbyte.io.common.mybatis.datasource.ReadDataSource;
import github.jiangbyte.io.oj.config.OjProperties;
import github.jiangbyte.io.oj.modules.judge.mq.OjJudgeMessage;
import github.jiangbyte.io.oj.modules.judge.mq.OjJudgePublisher;
import github.jiangbyte.io.oj.modules.judge.node.entity.OjJudgeNode;
import github.jiangbyte.io.oj.modules.judge.sandbox.SparkSandboxClient;
import github.jiangbyte.io.oj.modules.judge.schedule.CaseLoader;
import github.jiangbyte.io.oj.modules.judge.schedule.NodeScheduler;
import github.jiangbyte.io.oj.modules.judge.schedule.VerdictAggregator;
import github.jiangbyte.io.oj.modules.problemdryrun.entity.OjProblemDryRun;
import github.jiangbyte.io.oj.modules.problemdryrun.enums.OjDryRunLimitMode;
import github.jiangbyte.io.oj.modules.problemdryrun.enums.OjDryRunMode;
import github.jiangbyte.io.oj.modules.problemdryrun.enums.OjDryRunSourceFrom;
import github.jiangbyte.io.oj.modules.problemdryrun.mapper.OjProblemDryRunMapper;
import github.jiangbyte.io.oj.modules.problemdryrun.param.OjProblemApplyLimitsParam;
import github.jiangbyte.io.oj.modules.problemdryrun.param.OjProblemDryRunPageParam;
import github.jiangbyte.io.oj.modules.problemdryrun.param.OjProblemDryRunParam;
import github.jiangbyte.io.oj.modules.problemdryrun.service.OjProblemDryRunService;
import github.jiangbyte.io.oj.modules.problem.entity.OjProblem;
import github.jiangbyte.io.oj.modules.problem.mapper.OjProblemMapper;
import github.jiangbyte.io.oj.modules.problemlanguagelimit.entity.OjProblemLanguageLimit;
import github.jiangbyte.io.oj.modules.problemlanguagelimit.service.OjProblemLanguageLimitService;
import github.jiangbyte.io.oj.modules.problemcase.enums.OjEnableStatus;
import github.jiangbyte.io.oj.modules.problemsolution.entity.OjProblemSolution;
import github.jiangbyte.io.oj.modules.problemsolution.mapper.OjProblemSolutionMapper;
import github.jiangbyte.io.oj.modules.submission.enums.OjVerdict;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * OJ 管理端试跑：入队 + Worker 异步执行（与 Portal 提交共用 RabbitMQ）。
 *
 * Author: Charlie
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class OjProblemDryRunServiceImpl
        extends ServiceImpl<OjProblemDryRunMapper, OjProblemDryRun>
        implements OjProblemDryRunService {

    private static final long MIB = 1024L * 1024L;
    /** 试跑 ALL 模式每批测例数（DB 分页 + 沙箱分批）。 */
    private static final long DRY_RUN_CASE_BATCH = 50L;
    private static final List<String> STATUS_PRIORITY = List.of(
            OjVerdict.CE.name(),
            OjVerdict.SE.name(),
            OjVerdict.RE.name(),
            OjVerdict.TLE.name(),
            OjVerdict.MLE.name(),
            OjVerdict.OLE.name(),
            OjVerdict.WA.name(),
            OjVerdict.AC.name());

    private final OjProblemMapper ojProblemMapper;
    private final OjProblemSolutionMapper ojProblemSolutionMapper;
    private final OjProblemLanguageLimitService ojProblemLanguageLimitService;
    private final CaseLoader caseLoader;
    private final NodeScheduler nodeScheduler;
    private final SparkSandboxClient sparkSandboxClient;
    private final VerdictAggregator verdictAggregator;
    private final OjProperties ojProperties;
    private final OjJudgePublisher ojJudgePublisher;

    /**
     * 管理端发起试跑：校验题/语言 → 解析源码与限额 → 落 PENDING 记录 → 事务提交后入队。
     * 边界：不在此同步调沙箱；实际执行见 {@link #processQueued}。
     */
    @Override
    @Transactional
    public OjProblemDryRun dryRun(OjProblemDryRunParam param) {
        // 1. 题目存在性
        OjProblem problem = ojProblemMapper.selectById(param.getProblemId());
        if (problem == null) {
            throw new BizException(404, "OjProblem not found");
        }
        // 2. 限额模式 + 语言：限额行即允许语言
        String limitMode = normalizeLimitMode(param.getLimitMode());
        if (!StringUtils.hasText(param.getLanguage())) {
            throw new BizException(400, "请选择语言");
        }
        String language = param.getLanguage().trim();
        if (ojProblemLanguageLimitService.findByProblemAndLanguage(problem.getId(), language) == null) {
            throw new BizException(400, "该题目不支持语言: " + language);
        }
        // 3. caseKey 有则 SINGLE，否则 ALL
        boolean single = StringUtils.hasText(param.getCaseKey());
        String mode = single ? OjDryRunMode.SINGLE.name() : OjDryRunMode.ALL.name();
        String caseKey = single ? param.getCaseKey().trim() : null;

        // 4. 解析源码（覆盖或启用参考答案）与限额（题目/放宽）
        SourceResolved source = resolveSource(problem.getId(), language, param.getSource());
        Limits limits = resolveLimits(problem.getId(), language, limitMode);
        boolean stopOnFirstError = param.getStopOnFirstError() != null && param.getStopOnFirstError();

        // 5. 落 PENDING 试跑记录（快照 caseVersion / 限额 / 源码）
        OjProblemDryRun record = new OjProblemDryRun();
        record.setProblemId(problem.getId());
        record.setCaseVersion(problem.getCaseVersion());
        record.setMode(mode);
        record.setCaseKey(caseKey);
        record.setLimitMode(limitMode);
        record.setLanguage(language);
        record.setSource(source.source());
        record.setSourceFrom(source.sourceFrom());
        record.setAppliedTimeMs(limits.cpuTimeMs());
        record.setAppliedMemoryBytes(limits.memoryBytes());
        record.setOverallStatus(OjVerdict.PENDING.name());
        record.setCaseResults(List.of());
        this.save(record);
        AuditSnapshots.created(record);

        // 6. 事务提交后再投递，避免 Worker 读不到未提交行
        String dryRunId = record.getId();
        String requestId = UUID.randomUUID().toString().replace("-", "");
        enqueueAfterCommit(OjJudgeMessage.dryRun(dryRunId, requestId, stopOnFirstError));
        return record;
    }

    /**
     * Worker 执行已入队试跑：选机 → JUDGING → 单测/分批全测 → 裁决写回。
     * 边界：幂等跳过终态；与 Portal 提交共用 MQ，但不写 submission。
     */
    @Override
    public void processQueued(OjJudgeMessage message) {
        // 1. 校验 dryRunId；缺失进 DLQ
        String dryRunId = message.dryRunId();
        if (!StringUtils.hasText(dryRunId)) {
            log.warn("dry-run message missing dryRunId");
            ojJudgePublisher.publishDlq(message);
            return;
        }
        // 2. 记录可能尚未可见：短延迟重试
        OjProblemDryRun record = this.getById(dryRunId);
        if (record == null) {
            log.warn("dry-run not found yet, retry soon: {}", dryRunId);
            ojJudgePublisher.publishRetry(
                    OjJudgeMessage.dryRun(
                            dryRunId,
                            message.requestId(),
                            Boolean.TRUE.equals(message.stopOnFirstError())),
                    1000L);
            return;
        }
        // 3. 已终态直接跳过（幂等）
        if (!OjVerdict.PENDING.matches(record.getOverallStatus())
                && !OjVerdict.JUDGING.matches(record.getOverallStatus())) {
            return;
        }

        // 4. 题目缺失则 SE
        OjProblem problem = ojProblemMapper.selectById(record.getProblemId());
        if (problem == null) {
            finishError(record, "题目不存在");
            return;
        }

        // 5. 选机占坑；无节点则记错并退避重试
        boolean stopOnFirstError = Boolean.TRUE.equals(message.stopOnFirstError());
        String requestId = StringUtils.hasText(message.requestId())
                ? message.requestId()
                : UUID.randomUUID().toString().replace("-", "");
        OjJudgeNode node = nodeScheduler.selectAndAcquire(record.getLanguage(), List.of(), requestId);
        if (node == null) {
            ojJudgePublisher.publishRetry(
                    OjJudgeMessage.dryRun(dryRunId, requestId, stopOnFirstError),
                    3000L);
            this.update(Wrappers.<OjProblemDryRun>lambdaUpdate()
                    .set(OjProblemDryRun::getErrorMessage, "NO_ONLINE_NODE")
                    .eq(OjProblemDryRun::getId, dryRunId)
                    .eq(OjProblemDryRun::getOverallStatus, OjVerdict.PENDING.name()));
            return;
        }

        // 6. 标记 JUDGING 并绑定节点；限额从记录快照恢复
        this.update(Wrappers.<OjProblemDryRun>lambdaUpdate()
                .set(OjProblemDryRun::getOverallStatus, OjVerdict.JUDGING.name())
                .set(OjProblemDryRun::getNodeId, node.getId())
                .eq(OjProblemDryRun::getId, dryRunId)
                .in(OjProblemDryRun::getOverallStatus, List.of(OjVerdict.PENDING.name(), OjVerdict.JUDGING.name())));

        record.setNodeId(node.getId());
        record.setOverallStatus(OjVerdict.JUDGING.name());
        SourceResolved source = new SourceResolved(record.getSource(), record.getSourceFrom());
        Limits limits = new Limits(
                record.getAppliedTimeMs() == null ? 1000 : record.getAppliedTimeMs(),
                (record.getAppliedTimeMs() == null ? 1000 : record.getAppliedTimeMs())
                        * Math.max(1, ojProperties.getJudge().getRealTimeFactor()),
                record.getAppliedMemoryBytes() == null ? 268435456L : record.getAppliedMemoryBytes());

        // 7. 语言限额整单只查一次，供各批沙箱请求复用（栈/输出限额）
        OjProblemLanguageLimit langLimit = ojProblemLanguageLimitService.findByProblemAndLanguage(
                problem.getId(), record.getLanguage());

        try {
            // 8. SINGLE 按 key 加载；ALL 分页分批跑沙箱并合并
            if (OjDryRunMode.SINGLE.matches(record.getMode())) {
                List<CaseLoader.LoadedCase> cases = caseLoader.loadByKey(
                        problem.getId(), record.getCaseVersion(), record.getCaseKey());
                if (cases.isEmpty()) {
                    nodeScheduler.releaseInflight(node.getId());
                    finishError(record, "测例不存在或未启用: " + record.getCaseKey());
                    return;
                }
                applyBatchResult(record, problem, node, cases, source, limits, stopOnFirstError,
                        record.getLanguage(), langLimit);
            } else {
                applyAllBatched(record, problem, node, source, limits, stopOnFirstError,
                        record.getLanguage(), langLimit);
            }
        } catch (BizException ex) {
            nodeScheduler.releaseInflight(node.getId());
            finishError(record, ex.getMessage());
            return;
        } catch (Exception ex) {
            nodeScheduler.recordRunFailure(node.getId(), 0, ex.getMessage());
            record.setOverallStatus(OjVerdict.SE.name());
            record.setErrorMessage(truncate(ex.getMessage(), 512));
            record.setCaseResults(List.of());
        }
        // 9. 写回终态（成功路径已在 apply* 内 markSuccess）
        this.updateById(record);
    }

    private void enqueueAfterCommit(OjJudgeMessage message) {
        Runnable publish = () -> {
            try {
                ojJudgePublisher.publishWork(message);
            } catch (Exception ex) {
                log.warn("publish dry-run work failed dryRunId={}: {}", message.dryRunId(), ex.toString());
                this.update(Wrappers.<OjProblemDryRun>lambdaUpdate()
                        .set(OjProblemDryRun::getOverallStatus, OjVerdict.SE.name())
                        .set(OjProblemDryRun::getErrorMessage,
                                "判题入队失败：" + truncate(ex.getMessage(), 200))
                        .eq(OjProblemDryRun::getId, message.dryRunId())
                        .eq(OjProblemDryRun::getOverallStatus, OjVerdict.PENDING.name()));
            }
        };
        if (TransactionSynchronizationManager.isActualTransactionActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    publish.run();
                }
            });
        } else {
            publish.run();
        }
    }

    private void finishError(OjProblemDryRun record, String message) {
        record.setOverallStatus(OjVerdict.SE.name());
        record.setErrorMessage(truncate(message, 512));
        record.setCaseResults(List.of());
        this.updateById(record);
    }

    /** ALL 模式：DB 分页加载测例，分批调用沙箱并合并裁决。 */
    private void applyAllBatched(
            OjProblemDryRun record,
            OjProblem problem,
            OjJudgeNode node,
            SourceResolved source,
            Limits limits,
            boolean stopOnFirstError,
            String language,
            OjProblemLanguageLimit langLimit) {
        long current = 1L;
        List<Map<String, Object>> mergedCaseResults = new ArrayList<>();
        String overall = OjVerdict.AC.name();
        int maxTime = 0;
        long maxMem = 0L;
        boolean anyCase = false;

        while (true) {
            // 分页拉测例，避免一次加载超大测例包
            List<CaseLoader.LoadedCase> batch = caseLoader.loadPage(
                    problem.getId(), record.getCaseVersion(), current, DRY_RUN_CASE_BATCH);
            if (batch.isEmpty()) {
                if (!anyCase) {
                    throw new BizException("题目无可用测例");
                }
                break;
            }
            anyCase = true;
            BatchOutcome outcome = runOneBatch(
                    node, problem, batch, source, limits, stopOnFirstError, language, langLimit);
            if (outcome.transportFail()) {
                nodeScheduler.recordRunFailure(node.getId(), outcome.httpStatus(), outcome.errorMessage());
                record.setOverallStatus(OjVerdict.SE.name());
                record.setErrorMessage(truncate(outcome.errorMessage(), 512));
                record.setCaseResults(truncateCaseResults(mergedCaseResults));
                return;
            }
            mergedCaseResults.addAll(outcome.verdict().getCaseResults());
            overall = mergeOverallStatus(overall, outcome.verdict().getStatus());
            if (outcome.verdict().getTimeMs() != null) {
                maxTime = Math.max(maxTime, outcome.verdict().getTimeMs());
            }
            if (outcome.verdict().getMemoryBytes() != null) {
                maxMem = Math.max(maxMem, outcome.verdict().getMemoryBytes());
            }
            if (stopOnFirstError && !OjVerdict.AC.matches(outcome.verdict().getStatus())) {
                break;
            }
            if (batch.size() < DRY_RUN_CASE_BATCH) {
                break;
            }
            current++;
        }

        nodeScheduler.markSuccess(node.getId());
        record.setOverallStatus(overall);
        record.setMaxTimeMs(maxTime > 0 ? maxTime : null);
        record.setMaxMemoryBytes(maxMem > 0 ? maxMem : null);
        record.setCaseResults(truncateCaseResults(mergedCaseResults));
        if (!OjVerdict.AC.matches(overall) && !OjVerdict.WA.matches(overall)) {
            record.setErrorMessage(truncate(overall, 512));
        }
        applySuggestions(record, problem);
    }

    private void applyBatchResult(
            OjProblemDryRun record,
            OjProblem problem,
            OjJudgeNode node,
            List<CaseLoader.LoadedCase> cases,
            SourceResolved source,
            Limits limits,
            boolean stopOnFirstError,
            String language,
            OjProblemLanguageLimit langLimit) {
        BatchOutcome outcome = runOneBatch(
                node, problem, cases, source, limits, stopOnFirstError, language, langLimit);
        if (outcome.transportFail()) {
            nodeScheduler.recordRunFailure(node.getId(), outcome.httpStatus(), outcome.errorMessage());
            record.setOverallStatus(OjVerdict.SE.name());
            record.setErrorMessage(truncate(outcome.errorMessage(), 512));
            record.setCaseResults(List.of());
            return;
        }
        nodeScheduler.markSuccess(node.getId());
        VerdictAggregator.AggregateResult verdict = outcome.verdict();
        record.setOverallStatus(verdict.getStatus());
        record.setMaxTimeMs(verdict.getTimeMs());
        record.setMaxMemoryBytes(verdict.getMemoryBytes());
        record.setCaseResults(truncateCaseResults(verdict.getCaseResults()));
        if (StringUtils.hasText(verdict.getJudgeMessage())
                && !OjVerdict.AC.matches(verdict.getStatus())
                && !OjVerdict.WA.matches(verdict.getStatus())) {
            record.setErrorMessage(truncate(verdict.getJudgeMessage(), 512));
        }
        applySuggestions(record, problem);
    }

    private BatchOutcome runOneBatch(
            OjJudgeNode node,
            OjProblem problem,
            List<CaseLoader.LoadedCase> cases,
            SourceResolved source,
            Limits limits,
            boolean stopOnFirstError,
            String language,
            OjProblemLanguageLimit langLimit) {
        List<SparkSandboxClient.CaseInput> inputs = cases.stream()
                .map(c -> new SparkSandboxClient.CaseInput(c.caseKey(), c.stdin()))
                .toList();
        SparkSandboxClient.RunCasesRequest req = new SparkSandboxClient.RunCasesRequest(
                language,
                source.source(),
                inputs,
                stopOnFirstError,
                ojProperties.getJudge().getCaseParallelism(),
                limits.cpuTimeMs(),
                limits.realTimeMs(),
                limits.memoryBytes(),
                langLimit == null ? null : langLimit.getStackLimitBytes(),
                langLimit == null ? null : langLimit.getOutputLimitBytes());
        SparkSandboxClient.RunCasesResult run = sparkSandboxClient.runCases(node, req);
        if (run.transportFail()) {
            return BatchOutcome.transportFail(run.httpStatus(), run.errorMessage());
        }
        VerdictAggregator.AggregateResult verdict = verdictAggregator.aggregate(
                run.body(), cases, stopOnFirstError);
        return BatchOutcome.ok(verdict);
    }

    private String mergeOverallStatus(String left, String right) {
        int leftIdx = STATUS_PRIORITY.indexOf(left);
        int rightIdx = STATUS_PRIORITY.indexOf(right);
        if (leftIdx < 0) {
            leftIdx = STATUS_PRIORITY.size() - 1;
        }
        if (rightIdx < 0) {
            rightIdx = STATUS_PRIORITY.size() - 1;
        }
        return STATUS_PRIORITY.get(Math.min(leftIdx, rightIdx));
    }

    private record BatchOutcome(
            boolean transportFail,
            int httpStatus,
            String errorMessage,
            VerdictAggregator.AggregateResult verdict) {
        static BatchOutcome transportFail(int httpStatus, String errorMessage) {
            return new BatchOutcome(true, httpStatus, errorMessage, null);
        }

        static BatchOutcome ok(VerdictAggregator.AggregateResult verdict) {
            return new BatchOutcome(false, 200, null, verdict);
        }
    }

    @Override
    @ReadDataSource
    public Page<OjProblemDryRun> page(OjProblemDryRunPageParam param) {
        return this.getBaseMapper().selectPage(new Page<>(param.getCurrent(), param.getSize()),
                Wrappers.<OjProblemDryRun>lambdaQuery()
                        .eq(StringUtils.hasText(param.getProblemId()), OjProblemDryRun::getProblemId, param.getProblemId())
                        .eq(StringUtils.hasText(param.getMode()), OjProblemDryRun::getMode, param.getMode())
                        .eq(StringUtils.hasText(param.getLimitMode()), OjProblemDryRun::getLimitMode, param.getLimitMode())
                        .eq(StringUtils.hasText(param.getOverallStatus()), OjProblemDryRun::getOverallStatus, param.getOverallStatus())
                        .eq(StringUtils.hasText(param.getLanguage()), OjProblemDryRun::getLanguage, param.getLanguage())
                        .orderByDesc(OjProblemDryRun::getCreatedAt));
    }

    @Override
    @ReadDataSource
    public OjProblemDryRun detail(String id) {
        OjProblemDryRun entity = this.getById(id);
        if (entity == null) {
            throw new BizException(404, "OjProblemDryRun not found");
        }
        return entity;
    }

    @Override
    @Transactional
    public void applyLimits(OjProblemApplyLimitsParam param) {
        if (param.getTimeLimitMs() == null || param.getTimeLimitMs() < 1) {
            throw new BizException("时限无效");
        }
        if (param.getMemoryLimitBytes() == null || param.getMemoryLimitBytes() < MIB) {
            throw new BizException("内存限额无效（至少 1MiB）");
        }
        if (!StringUtils.hasText(param.getLanguage())) {
            throw new BizException("请指定语言");
        }
        OjProblem problem = ojProblemMapper.selectById(param.getProblemId());
        if (problem == null) {
            throw new BizException(404, "OjProblem not found");
        }
        OjProblemLanguageLimit row = ojProblemLanguageLimitService.findByProblemAndLanguage(
                param.getProblemId(), param.getLanguage().trim());
        if (row == null) {
            throw new BizException(404, "该语言限额不存在，请先在题目中配置");
        }
        AuditSnapshots.before(row);
        row.setTimeLimitMs(param.getTimeLimitMs());
        row.setMemoryLimitBytes(param.getMemoryLimitBytes());
        ojProblemLanguageLimitService.updateById(row);
        AuditSnapshots.after(row);
    }

    private SourceResolved resolveSource(String problemId, String language, String override) {
        if (StringUtils.hasText(override)) {
            return new SourceResolved(override, OjDryRunSourceFrom.OVERRIDE.name());
        }
        OjProblemSolution preferred = ojProblemSolutionMapper.selectOne(
                Wrappers.<OjProblemSolution>lambdaQuery()
                        .eq(OjProblemSolution::getProblemId, problemId)
                        .eq(OjProblemSolution::getLanguage, language)
                        .eq(OjProblemSolution::getStatus, OjEnableStatus.ENABLED.name())
                        .orderByDesc(OjProblemSolution::getIsDefault)
                        .last("LIMIT 1"));
        if (preferred == null || !StringUtils.hasText(preferred.getSource())) {
            throw new BizException("未找到该语言的启用参考答案，请提供 source 或先维护参考答案");
        }
        return new SourceResolved(preferred.getSource(), OjDryRunSourceFrom.STORED.name());
    }

    private Limits resolveLimits(String problemId, String language, String limitMode) {
        OjProperties.Judge judge = ojProperties.getJudge();
        int factor = Math.max(1, judge.getRealTimeFactor());
        if (OjDryRunLimitMode.RELAXED.matches(limitMode)) {
            int cpu = Math.max(1000, judge.getDryRunRelaxedCpuTimeMs());
            long mem = Math.max(MIB, judge.getDryRunRelaxedMemoryBytes());
            return new Limits(cpu, cpu * factor, mem);
        }
        OjProblemLanguageLimit limit = ojProblemLanguageLimitService.findByProblemAndLanguage(problemId, language);
        if (limit == null) {
            throw new BizException(400, "该题目未配置语言限额: " + language);
        }
        int cpu = limit.getTimeLimitMs() == null ? 1000 : Math.max(1, limit.getTimeLimitMs());
        long mem = limit.getMemoryLimitBytes() == null ? 268435456L : Math.max(MIB, limit.getMemoryLimitBytes());
        return new Limits(cpu, cpu * factor, mem);
    }

    private void applySuggestions(OjProblemDryRun record, OjProblem problem) {
        OjProperties.Judge judge = ojProperties.getJudge();
        int maxTime = record.getMaxTimeMs() == null ? 0 : record.getMaxTimeMs();
        long maxMem = record.getMaxMemoryBytes() == null ? 0L : record.getMaxMemoryBytes();
        double timeFactor = judge.getDryRunTimeFactor() <= 0 ? 3.0 : judge.getDryRunTimeFactor();
        double memFactor = judge.getDryRunMemoryFactor() <= 0 ? 2.0 : judge.getDryRunMemoryFactor();

        int suggestedTime = Math.max(1000, (int) Math.ceil(maxTime * timeFactor));
        OjProblemLanguageLimit langLimit = ojProblemLanguageLimitService.findByProblemAndLanguage(
                problem.getId(), record.getLanguage());
        long problemMem = langLimit == null || langLimit.getMemoryLimitBytes() == null
                ? 0L
                : langLimit.getMemoryLimitBytes();
        long suggestedMem = Math.max(problemMem, (long) Math.ceil(maxMem * memFactor));
        suggestedMem = Math.max(MIB, ((suggestedMem + MIB - 1) / MIB) * MIB);

        record.setSuggestedTimeMs(suggestedTime);
        record.setSuggestedMemoryBytes(suggestedMem);
    }

    private List<Map<String, Object>> truncateCaseResults(List<Map<String, Object>> rows) {
        if (rows == null || rows.isEmpty()) {
            return List.of();
        }
        List<Map<String, Object>> out = new ArrayList<>(rows.size());
        for (Map<String, Object> row : rows) {
            if (row == null) {
                continue;
            }
            Object msg = row.get("message");
            if (msg instanceof String s && s.length() > 256) {
                row = new java.util.HashMap<>(row);
                row.put("message", s.substring(0, 256));
            }
            out.add(row);
        }
        return out;
    }

    private static String normalizeLimitMode(String raw) {
        if (!StringUtils.hasText(raw)) {
            throw new BizException("limit_mode 不能为空");
        }
        OjDryRunLimitMode mode = OjDryRunLimitMode.fromCode(raw);
        if (mode == null) {
            throw new BizException("limit_mode 仅支持 PROBLEM/RELAXED");
        }
        return mode.name();
    }

    private static String truncate(String text, int max) {
        if (text == null) {
            return null;
        }
        return text.length() <= max ? text : text.substring(0, max);
    }

    private record SourceResolved(String source, String sourceFrom) {
    }

    private record Limits(int cpuTimeMs, int realTimeMs, long memoryBytes) {
    }
}
