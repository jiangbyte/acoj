package github.jiangbyte.io.oj.modules.problemdryrun.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import github.jiangbyte.io.common.core.exception.BizException;
import github.jiangbyte.io.common.log.audit.AuditSnapshots;
import github.jiangbyte.io.common.mybatis.datasource.ReadDataSource;
import github.jiangbyte.io.oj.config.OjProperties;
import github.jiangbyte.io.oj.modules.judge.node.entity.OjJudgeNode;
import github.jiangbyte.io.oj.modules.judge.sandbox.SparkSandboxClient;
import github.jiangbyte.io.oj.modules.judge.schedule.CaseLoader;
import github.jiangbyte.io.oj.modules.judge.schedule.NodeScheduler;
import github.jiangbyte.io.oj.modules.judge.schedule.VerdictAggregator;
import github.jiangbyte.io.oj.modules.problemdryrun.entity.OjProblemDryRun;
import github.jiangbyte.io.oj.modules.problemdryrun.mapper.OjProblemDryRunMapper;
import github.jiangbyte.io.oj.modules.problemdryrun.param.OjProblemApplyLimitsParam;
import github.jiangbyte.io.oj.modules.problemdryrun.param.OjProblemDryRunPageParam;
import github.jiangbyte.io.oj.modules.problemdryrun.param.OjProblemDryRunParam;
import github.jiangbyte.io.oj.modules.problemdryrun.service.OjProblemDryRunService;
import github.jiangbyte.io.oj.modules.problem.entity.OjProblem;
import github.jiangbyte.io.oj.modules.problem.mapper.OjProblemMapper;
import github.jiangbyte.io.oj.modules.problemsolution.entity.OjProblemSolution;
import github.jiangbyte.io.oj.modules.problemsolution.mapper.OjProblemSolutionMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * OJ 管理端试跑服务：选机 → run_cases → 裁决 → 落历史。
 *
 * Author: Charlie
 */
@Service
@RequiredArgsConstructor
public class OjProblemDryRunServiceImpl
        extends ServiceImpl<OjProblemDryRunMapper, OjProblemDryRun>
        implements OjProblemDryRunService {

    private static final long MIB = 1024L * 1024L;
    /** 试跑 ALL 模式每批测例数（DB 分页 + 沙箱分批）。 */
    private static final long DRY_RUN_CASE_BATCH = 50L;
    private static final List<String> STATUS_PRIORITY = List.of(
            "CE", "SE", "RE", "TLE", "MLE", "OLE", "WA", "AC");

    private final OjProblemMapper ojProblemMapper;
    private final OjProblemSolutionMapper ojProblemSolutionMapper;
    private final CaseLoader caseLoader;
    private final NodeScheduler nodeScheduler;
    private final SparkSandboxClient sparkSandboxClient;
    private final VerdictAggregator verdictAggregator;
    private final OjProperties ojProperties;

    @Override
    public OjProblemDryRun dryRun(OjProblemDryRunParam param) {
        OjProblem problem = ojProblemMapper.selectById(param.getProblemId());
        if (problem == null) {
            throw new BizException(404, "OjProblem not found");
        }
        String limitMode = normalizeLimitMode(param.getLimitMode());
        if (!StringUtils.hasText(param.getLanguage())) {
            throw new BizException(400, "请选择语言");
        }
        String language = param.getLanguage().trim();
        List<String> allowed = problem.getAllowedLanguages();
        if (allowed != null && !allowed.isEmpty()
                && allowed.stream().noneMatch(l -> language.equalsIgnoreCase(l))) {
            throw new BizException(400, "该题目不支持语言: " + language);
        }
        boolean single = StringUtils.hasText(param.getCaseKey());
        String mode = single ? "SINGLE" : "ALL";
        String caseKey = single ? param.getCaseKey().trim() : null;

        SourceResolved source = resolveSource(problem.getId(), language, param.getSource());
        Limits limits = resolveLimits(problem, limitMode);
        boolean stopOnFirstError = param.getStopOnFirstError() != null
                ? param.getStopOnFirstError()
                : false;

        String requestId = UUID.randomUUID().toString().replace("-", "");
        OjJudgeNode node = nodeScheduler.selectAndAcquire(language, List.of(), requestId);
        if (node == null) {
            throw new BizException("当前无可用执行机");
        }

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
        record.setNodeId(node.getId());
        record.setCaseResults(List.of());

        try {
            if (single) {
                List<CaseLoader.LoadedCase> cases = caseLoader.loadByKey(
                        problem.getId(), problem.getCaseVersion(), caseKey);
                if (cases.isEmpty()) {
                    throw new BizException("测例不存在或未启用: " + caseKey);
                }
                applyBatchResult(record, problem, node, cases, source, limits, stopOnFirstError, language);
            } else {
                applyAllBatched(record, problem, node, source, limits, stopOnFirstError, language);
            }
        } catch (BizException ex) {
            nodeScheduler.releaseInflight(node.getId());
            throw ex;
        } catch (Exception ex) {
            nodeScheduler.recordRunFailure(node.getId(), 0, ex.getMessage());
            record.setOverallStatus("SE");
            record.setErrorMessage(truncate(ex.getMessage(), 512));
            record.setCaseResults(List.of());
        }

        this.save(record);
        AuditSnapshots.created(record);
        return record;
    }

    /** ALL 模式：DB 分页加载测例，分批调用沙箱并合并裁决。 */
    private void applyAllBatched(
            OjProblemDryRun record,
            OjProblem problem,
            OjJudgeNode node,
            SourceResolved source,
            Limits limits,
            boolean stopOnFirstError,
            String language) {
        long current = 1L;
        List<Map<String, Object>> mergedCaseResults = new ArrayList<>();
        String overall = "AC";
        int maxTime = 0;
        long maxMem = 0L;
        boolean anyCase = false;

        while (true) {
            List<CaseLoader.LoadedCase> batch = caseLoader.loadPage(
                    problem.getId(), problem.getCaseVersion(), current, DRY_RUN_CASE_BATCH);
            if (batch.isEmpty()) {
                if (!anyCase) {
                    throw new BizException("题目无可用测例");
                }
                break;
            }
            anyCase = true;
            BatchOutcome outcome = runOneBatch(node, problem, batch, source, limits, stopOnFirstError, language);
            if (outcome.transportFail()) {
                nodeScheduler.recordRunFailure(node.getId(), outcome.httpStatus(), outcome.errorMessage());
                record.setOverallStatus("SE");
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
            if (stopOnFirstError && !"AC".equals(outcome.verdict().getStatus())) {
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
        if (!"AC".equals(overall) && !"WA".equals(overall)) {
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
            String language) {
        BatchOutcome outcome = runOneBatch(node, problem, cases, source, limits, stopOnFirstError, language);
        if (outcome.transportFail()) {
            nodeScheduler.recordRunFailure(node.getId(), outcome.httpStatus(), outcome.errorMessage());
            record.setOverallStatus("SE");
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
                && !"AC".equals(verdict.getStatus())
                && !"WA".equals(verdict.getStatus())) {
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
            String language) {
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
                problem.getStackLimitBytes(),
                problem.getOutputLimitBytes());
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
        OjProblem problem = ojProblemMapper.selectById(param.getProblemId());
        if (problem == null) {
            throw new BizException(404, "OjProblem not found");
        }
        AuditSnapshots.before(problem);
        problem.setTimeLimitMs(param.getTimeLimitMs());
        problem.setMemoryLimitBytes(param.getMemoryLimitBytes());
        ojProblemMapper.updateById(problem);
        AuditSnapshots.after(problem);
    }

    private SourceResolved resolveSource(String problemId, String language, String override) {
        if (StringUtils.hasText(override)) {
            return new SourceResolved(override, "OVERRIDE");
        }
        OjProblemSolution preferred = ojProblemSolutionMapper.selectOne(
                Wrappers.<OjProblemSolution>lambdaQuery()
                        .eq(OjProblemSolution::getProblemId, problemId)
                        .eq(OjProblemSolution::getLanguage, language)
                        .eq(OjProblemSolution::getStatus, "ENABLED")
                        .orderByDesc(OjProblemSolution::getIsDefault)
                        .last("LIMIT 1"));
        if (preferred == null || !StringUtils.hasText(preferred.getSource())) {
            throw new BizException("未找到该语言的启用参考答案，请提供 source 或先维护参考答案");
        }
        return new SourceResolved(preferred.getSource(), "STORED");
    }

    private Limits resolveLimits(OjProblem problem, String limitMode) {
        OjProperties.Judge judge = ojProperties.getJudge();
        int factor = Math.max(1, judge.getRealTimeFactor());
        if ("RELAXED".equals(limitMode)) {
            int cpu = Math.max(1000, judge.getDryRunRelaxedCpuTimeMs());
            long mem = Math.max(MIB, judge.getDryRunRelaxedMemoryBytes());
            return new Limits(cpu, cpu * factor, mem);
        }
        int cpu = problem.getTimeLimitMs() == null ? 1000 : Math.max(1, problem.getTimeLimitMs());
        long mem = problem.getMemoryLimitBytes() == null ? 268435456L : Math.max(MIB, problem.getMemoryLimitBytes());
        return new Limits(cpu, cpu * factor, mem);
    }

    private void applySuggestions(OjProblemDryRun record, OjProblem problem) {
        OjProperties.Judge judge = ojProperties.getJudge();
        int maxTime = record.getMaxTimeMs() == null ? 0 : record.getMaxTimeMs();
        long maxMem = record.getMaxMemoryBytes() == null ? 0L : record.getMaxMemoryBytes();
        double timeFactor = judge.getDryRunTimeFactor() <= 0 ? 3.0 : judge.getDryRunTimeFactor();
        double memFactor = judge.getDryRunMemoryFactor() <= 0 ? 2.0 : judge.getDryRunMemoryFactor();

        int suggestedTime = Math.max(1000, (int) Math.ceil(maxTime * timeFactor));
        long problemMem = problem.getMemoryLimitBytes() == null ? 0L : problem.getMemoryLimitBytes();
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
        String mode = raw.trim().toUpperCase();
        if (!"PROBLEM".equals(mode) && !"RELAXED".equals(mode)) {
            throw new BizException("limit_mode 仅支持 PROBLEM/RELAXED");
        }
        return mode;
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
