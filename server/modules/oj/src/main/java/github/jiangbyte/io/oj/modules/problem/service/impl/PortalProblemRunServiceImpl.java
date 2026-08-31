package github.jiangbyte.io.oj.modules.problem.service.impl;

import github.jiangbyte.io.common.core.exception.BizException;
import github.jiangbyte.io.oj.config.OjProperties;
import github.jiangbyte.io.oj.modules.judge.enums.SandboxCaseStatus;
import github.jiangbyte.io.oj.modules.judge.node.entity.OjJudgeNode;
import github.jiangbyte.io.oj.modules.judge.sandbox.SparkSandboxClient;
import github.jiangbyte.io.oj.modules.judge.schedule.CaseLoader;
import github.jiangbyte.io.oj.modules.judge.schedule.NodeScheduler;
import github.jiangbyte.io.oj.modules.judge.schedule.VerdictAggregator;
import github.jiangbyte.io.oj.modules.problem.entity.OjProblem;
import github.jiangbyte.io.oj.modules.problem.param.OjProblemPortalRunParam;
import github.jiangbyte.io.oj.modules.problem.result.OjProblemPortalRunResult;
import github.jiangbyte.io.oj.modules.problem.service.OjProblemService;
import github.jiangbyte.io.oj.modules.problem.service.PortalProblemRunService;
import github.jiangbyte.io.oj.modules.problemlanguagelimit.entity.OjProblemLanguageLimit;
import github.jiangbyte.io.oj.modules.problemlanguagelimit.service.OjProblemLanguageLimitService;
import github.jiangbyte.io.oj.modules.submission.enums.OjVerdict;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import tools.jackson.databind.JsonNode;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * 门户样例试跑：选机 → run_cases → 裁决；不落库。
 * <p>
 * Author: Charlie
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PortalProblemRunServiceImpl implements PortalProblemRunService {

    private static final int MAX_CASES = 5;
    private static final int MAX_INPUT_CHARS = 64 * 1024;

    private final OjProblemService ojProblemService;
    private final OjProblemLanguageLimitService ojProblemLanguageLimitService;
    private final OjProperties ojProperties;
    private final NodeScheduler nodeScheduler;
    private final SparkSandboxClient sparkSandboxClient;
    private final VerdictAggregator verdictAggregator;

    /**
     * 门户样例试跑：校验语言 → 解析测例 → 选机 → run_cases → 裁决；不落库。
     * 边界：最多 5 组；传输失败记节点失败；异常须释放 inflight。
     */
    @Override
    public OjProblemPortalRunResult run(OjProblemPortalRunParam param) {
        // 1. 仅已发布题可跑（portalDetail 门禁）
        OjProblem problem = ojProblemService.portalDetail(param.getProblemId());
        String language = param.getLanguage() == null ? "" : param.getLanguage().trim();
        if (!StringUtils.hasText(language)) {
            throw new BizException(400, "请选择语言");
        }
        // 2. 允许语言与限额同一行真相：有 language_limit 才可跑
        OjProblemLanguageLimit limit = ojProblemLanguageLimitService.findByProblemAndLanguage(
                problem.getId(), language);
        if (limit == null) {
            throw new BizException(400, "该题目不支持语言: " + language);
        }
        if (!StringUtils.hasText(param.getSourceCode())) {
            throw new BizException(400, "请输入代码");
        }

        // 3. 自定义测例优先，否则用题面 samples；至少一组
        List<CaseLoader.LoadedCase> cases = resolveCases(problem, param.getCases());
        if (cases.isEmpty()) {
            throw new BizException(400, "请至少提供一组测试用例");
        }

        // 4. 选机占坑；无节点 503
        String requestId = UUID.randomUUID().toString().replace("-", "");
        OjJudgeNode node = nodeScheduler.selectAndAcquire(language, List.of(), requestId);
        if (node == null) {
            throw new BizException(503, "暂无可用判题节点，请稍后重试");
        }

        try {
            // 5. 按限额组装请求；遇错即停，减少样例试跑耗时
            int cpu = limit.getTimeLimitMs() == null ? 1000 : limit.getTimeLimitMs();
            int real = cpu * Math.max(1, ojProperties.getJudge().getRealTimeFactor());
            long mem = limit.getMemoryLimitBytes() == null ? 268435456L : limit.getMemoryLimitBytes();
            List<SparkSandboxClient.CaseInput> inputs = cases.stream()
                    .map(c -> new SparkSandboxClient.CaseInput(c.caseKey(), c.stdin()))
                    .toList();
            SparkSandboxClient.RunCasesRequest req = new SparkSandboxClient.RunCasesRequest(
                    language,
                    param.getSourceCode(),
                    inputs,
                    true,
                    ojProperties.getJudge().getCaseParallelism(),
                    cpu,
                    real,
                    mem,
                    limit.getStackLimitBytes(),
                    limit.getOutputLimitBytes());
            SparkSandboxClient.RunCasesResult run = sparkSandboxClient.runCases(node, req);
            // 6. 传输失败记节点并 502；成功则裁决并 markSuccess
            if (run.transportFail()) {
                nodeScheduler.recordRunFailure(node.getId(), run.httpStatus(), run.errorMessage());
                throw new BizException(502, "判题服务暂时不可用，请稍后重试");
            }

            VerdictAggregator.AggregateResult verdict = verdictAggregator.aggregate(run.body(), cases, true);
            nodeScheduler.markSuccess(node.getId());
            return toResult(verdict, cases, run.body());
        } catch (BizException ex) {
            throw ex;
        } catch (Exception ex) {
            // 7. 业务外异常须释放 inflight，避免泄漏
            log.error("portal sample run failed problemId={}", problem.getId(), ex);
            nodeScheduler.releaseInflight(node.getId());
            throw new BizException(500, "试跑失败，请稍后重试");
        }
    }

    private List<CaseLoader.LoadedCase> resolveCases(
            OjProblem problem,
            List<OjProblemPortalRunParam.CaseItem> rawCases) {
        List<CaseLoader.LoadedCase> result = new ArrayList<>();
        if (rawCases != null && !rawCases.isEmpty()) {
            if (rawCases.size() > MAX_CASES) {
                throw new BizException(400, "测试用例最多 " + MAX_CASES + " 组");
            }
            int i = 1;
            for (OjProblemPortalRunParam.CaseItem item : rawCases) {
                String stdin = item.getInput() == null ? "" : item.getInput();
                if (stdin.length() > MAX_INPUT_CHARS) {
                    throw new BizException(400, "单组输入过长");
                }
                String expected = item.getOutput();
                // null expected = 只跑不比对；空串仍按空期望比对
                result.add(new CaseLoader.LoadedCase(
                        "case-" + i,
                        stdin,
                        expected,
                        false,
                        i));
                i++;
            }
            return result;
        }

        List<Map<String, Object>> samples = problem.getSamples();
        if (samples == null || samples.isEmpty()) {
            return result;
        }
        int i = 1;
        for (Map<String, Object> sample : samples) {
            if (i > MAX_CASES) {
                break;
            }
            String stdin = sample == null || sample.get("input") == null
                    ? ""
                    : String.valueOf(sample.get("input"));
            if (stdin.length() > MAX_INPUT_CHARS) {
                throw new BizException(400, "样例输入过长");
            }
            Object out = sample == null ? null : sample.get("output");
            String expected = out == null ? null : String.valueOf(out);
            result.add(new CaseLoader.LoadedCase("sample-" + i, stdin, expected, true, i));
            i++;
        }
        return result;
    }

    private OjProblemPortalRunResult toResult(
            VerdictAggregator.AggregateResult verdict,
            List<CaseLoader.LoadedCase> cases,
            JsonNode sandboxBody) {
        OjProblemPortalRunResult result = new OjProblemPortalRunResult();
        result.setStatus(verdict.getStatus());
        result.setCompileOutput(verdict.getCompileOutput());
        result.setJudgeMessage(verdict.getJudgeMessage());
        result.setTimeMs(verdict.getTimeMs());
        result.setMemoryBytes(verdict.getMemoryBytes());

        Map<String, JsonNode> sandboxByKey = indexSandboxCases(sandboxBody);
        List<OjProblemPortalRunResult.CaseResult> rows = new ArrayList<>();
        List<Map<String, Object>> aggregated = verdict.getCaseResults();
        if (aggregated != null) {
            for (int i = 0; i < aggregated.size(); i++) {
                Map<String, Object> row = aggregated.get(i);
                CaseLoader.LoadedCase loaded = i < cases.size() ? cases.get(i) : null;
                String caseKey = row.get("case_key") == null ? "" : String.valueOf(row.get("case_key"));
                if (loaded != null && StringUtils.hasText(loaded.caseKey())) {
                    caseKey = loaded.caseKey();
                }
                OjProblemPortalRunResult.CaseResult item = new OjProblemPortalRunResult.CaseResult();
                item.setCaseKey(caseKey);
                String status = row.get("status") == null ? null : String.valueOf(row.get("status"));
                String expected = loaded == null ? null : loaded.expectedStdout();
                JsonNode sandboxCase = sandboxByKey.get(caseKey);
                String stdout = sandboxCase == null ? null : text(sandboxCase, "stdout");
                // 无期望输出：成功跑通即视为通过展示，不做 WA
                if (expected == null && OjVerdict.WA.matches(status)) {
                    String sandboxStatus = sandboxCase == null ? null : text(sandboxCase, "status");
                    if (SandboxCaseStatus.SUCCEEDED.matches(sandboxStatus)) {
                        status = OjVerdict.AC.name();
                    }
                }
                item.setStatus(status);
                Object time = row.get("time_ms");
                item.setTimeMs(time instanceof Number n ? n.intValue() : null);
                Object mem = row.get("memory_bytes");
                item.setMemoryBytes(mem instanceof Number n ? n.longValue() : null);
                item.setMessage(row.get("message") == null ? null : String.valueOf(row.get("message")));
                item.setStdin(loaded == null ? null : loaded.stdin());
                item.setExpected(expected);
                item.setStdout(stdout);
                rows.add(item);
            }
        }
        result.setCaseResults(rows);

        // 无期望时可能修正了单例，重算整单
        if (!rows.isEmpty()
                && !OjVerdict.CE.matches(result.getStatus())
                && !OjVerdict.SE.matches(result.getStatus())) {
            String overall = OjVerdict.AC.name();
            for (OjProblemPortalRunResult.CaseResult row : rows) {
                if (!OjVerdict.AC.matches(row.getStatus())) {
                    overall = row.getStatus();
                    break;
                }
            }
            result.setStatus(overall);
            result.setJudgeMessage(overall);
        }
        return result;
    }

    private static Map<String, JsonNode> indexSandboxCases(JsonNode body) {
        java.util.HashMap<String, JsonNode> map = new java.util.HashMap<>();
        if (body == null) {
            return map;
        }
        JsonNode arr = body.path("cases");
        if (!arr.isArray()) {
            return map;
        }
        for (JsonNode node : arr) {
            String id = text(node, "case_id");
            if (id == null) {
                id = text(node, "caseId");
            }
            if (id != null) {
                map.put(id, node);
            }
        }
        return map;
    }

    private static String text(JsonNode node, String field) {
        if (node == null || node.isMissingNode() || node.isNull()) {
            return null;
        }
        JsonNode v = node.get(field);
        if (v == null || v.isNull()) {
            return null;
        }
        String t = v.asText();
        return StringUtils.hasText(t) ? t : ("".equals(t) ? "" : null);
    }
}
