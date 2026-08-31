package github.jiangbyte.io.oj.modules.judge.schedule;

import github.jiangbyte.io.oj.modules.judge.enums.SandboxCaseStatus;
import github.jiangbyte.io.oj.modules.submission.enums.OjVerdict;
import tools.jackson.databind.JsonNode;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 业务裁决：沙箱状态映射 + RTRIM 行比对 + 整单聚合。
 *
 * Author: Charlie
 */
@Component
public class VerdictAggregator {

    private static final List<String> PRIORITY = List.of(
            OjVerdict.CE.name(),
            OjVerdict.SE.name(),
            OjVerdict.RE.name(),
            OjVerdict.TLE.name(),
            OjVerdict.MLE.name(),
            OjVerdict.OLE.name(),
            OjVerdict.WA.name(),
            OjVerdict.AC.name());

    /**
     * 根据沙箱响应与期望输出聚合整单结果。
     * 边界：只做业务裁决与计分，不写库、不调沙箱；{@code stopOnFirstError} 须与传给沙箱一致。
     */
    public AggregateResult aggregate(
            JsonNode sandboxResponse,
            List<CaseLoader.LoadedCase> cases,
            boolean stopOnFirstError) {
        // 1. 空响应直接 SE
        if (sandboxResponse == null) {
            return AggregateResult.systemError("空沙箱响应");
        }

        // 2. 编译失败：收集诊断输出，整单 CE、不计测点
        String topStatus = text(sandboxResponse, "status");
        JsonNode compile = sandboxResponse.path("compile");
        String compileStatus = text(compile, "status");
        if (SandboxCaseStatus.COMPILE_FAILED.matches(topStatus)
                || SandboxCaseStatus.COMPILE_FAILED.matches(compileStatus)) {
            // SparkSandbox 把编译器诊断放在顶层 compiler_output；compile.message 多为 "non-zero exit code"
            String compileOut = firstNonBlank(
                    text(sandboxResponse, "compiler_output"),
                    text(compile, "stderr"),
                    text(compile, "stdout"),
                    text(compile, "message"),
                    text(sandboxResponse, "message"));
            return AggregateResult.builder()
                    .status(OjVerdict.CE.name())
                    .score(0)
                    .compileOutput(compileOut)
                    .judgeMessage("Compile Error")
                    .caseResults(List.of())
                    .build();
        }

        // 3. 期望输出按 caseKey 索引，供逐测比对
        Map<String, CaseLoader.LoadedCase> byKey = new LinkedHashMap<>();
        for (CaseLoader.LoadedCase c : cases) {
            byKey.put(c.caseKey(), c);
        }

        // 4. 逐测例：映射沙箱状态 → 有期望则 RTRIM 比对 → 记 max 时/空与首个非 AC
        JsonNode caseNodes = sandboxResponse.path("cases");
        List<Map<String, Object>> caseResults = new ArrayList<>();
        int maxTime = 0;
        long maxMem = 0L;
        String firstNonAc = null;

        if (caseNodes != null && caseNodes.isArray()) {
            for (JsonNode node : caseNodes) {
                String caseId = firstNonBlank(text(node, "case_id"), text(node, "caseId"));
                CaseLoader.LoadedCase expected = caseId == null ? null : byKey.get(caseId);
                String sandboxStatus = text(node, "status");
                String verdict = mapCaseStatus(sandboxStatus);
                String message = "";

                if (OjVerdict.AC.matches(verdict) || SandboxCaseStatus.SUCCEEDED.matches(sandboxStatus)) {
                    String stdout = firstNonBlank(text(node, "stdout"), "");
                    String expect = expected == null ? null : expected.expectedStdout();
                    if (expect == null) {
                        // 无期望输出：以沙箱执行结果为准（succeeded → AC）
                        verdict = SandboxCaseStatus.SUCCEEDED.matches(sandboxStatus) || OjVerdict.AC.matches(verdict)
                                ? OjVerdict.AC.name()
                                : verdict;
                    } else if (textEqualsRtrim(expect, stdout)) {
                        verdict = OjVerdict.AC.name();
                    } else {
                        verdict = OjVerdict.WA.name();
                        message = "Wrong Answer";
                    }
                }

                Integer timeMs = caseTimeMs(node);
                Long mem = caseMemoryBytes(node);
                if (timeMs != null) {
                    maxTime = Math.max(maxTime, timeMs);
                }
                if (mem != null) {
                    maxMem = Math.max(maxMem, mem);
                }

                Map<String, Object> row = new HashMap<>();
                row.put("case_key", caseId == null ? "" : caseId);
                row.put("status", verdict);
                row.put("time_ms", timeMs);
                row.put("memory_bytes", mem);
                row.put("message", message);
                caseResults.add(row);

                if (!OjVerdict.AC.matches(verdict) && firstNonAc == null) {
                    firstNonAc = verdict;
                    if (stopOnFirstError) {
                        break;
                    }
                }
            }
        }

        // 5. 整单状态：遇错即停取首个非 AC；无测点则 SE；否则取最差优先级
        String overall;
        if (stopOnFirstError && firstNonAc != null) {
            overall = firstNonAc;
        } else if (caseResults.isEmpty()) {
            if (SandboxCaseStatus.INTERNAL_ERROR.matches(topStatus)) {
                return AggregateResult.systemError("sandbox internal_error");
            }
            overall = OjVerdict.SE.name();
        } else {
            overall = pickWorst(caseResults);
        }

        // 6. 计分并打包返回
        return AggregateResult.builder()
                .status(overall)
                .score(computeScore(overall, cases, caseResults))
                .timeMs(maxTime > 0 ? maxTime : null)
                .memoryBytes(maxMem > 0 ? maxMem : null)
                .caseResults(caseResults)
                .judgeMessage(overall)
                .compileOutput(firstNonBlank(
                        text(sandboxResponse, "compiler_output"),
                        text(compile, "stderr"),
                        text(compile, "stdout")))
                .build();
    }

    /**
     * 按测例分值计分：AC 测例得分之和 / 总分 × 100。
     * 若全部测例 score 为 0，则等权（每测例 1 分）。
     * CE / SE 为 0；整单 AC 恒为 100。
     */
    static int computeScore(
            String overall,
            List<CaseLoader.LoadedCase> cases,
            List<Map<String, Object>> caseResults) {
        // 1. CE/SE 零分；整单 AC 满分
        if (OjVerdict.CE.matches(overall) || OjVerdict.SE.matches(overall)) {
            return 0;
        }
        if (OjVerdict.AC.matches(overall)) {
            return 100;
        }
        if (cases == null || cases.isEmpty()) {
            return 0;
        }
        // 2. 任一测例 score>0 则加权，否则等权 1
        boolean weighted = false;
        for (CaseLoader.LoadedCase c : cases) {
            if (c != null && c.score() > 0) {
                weighted = true;
                break;
            }
        }
        // 3. 汇总权重表与总分
        int total = 0;
        Map<String, Integer> weightByKey = new HashMap<>();
        for (CaseLoader.LoadedCase c : cases) {
            if (c == null || !StringUtils.hasText(c.caseKey())) {
                continue;
            }
            int w = weighted ? Math.max(0, c.score()) : 1;
            weightByKey.put(c.caseKey(), w);
            total += w;
        }
        if (total <= 0) {
            return 0;
        }
        // 4. 累加 AC 测例权重，换算百分制
        int earned = 0;
        if (caseResults != null) {
            for (Map<String, Object> row : caseResults) {
                if (row == null || !OjVerdict.AC.matches(String.valueOf(row.get("status")))) {
                    continue;
                }
                Object keyObj = row.get("case_key");
                String key = keyObj == null ? "" : String.valueOf(keyObj);
                Integer w = weightByKey.get(key);
                if (w != null) {
                    earned += w;
                }
            }
        }
        return (int) Math.round(100.0 * earned / total);
    }

    private static String mapCaseStatus(String sandboxStatus) {
        SandboxCaseStatus status = SandboxCaseStatus.fromWire(sandboxStatus);
        if (status == null) {
            return OjVerdict.SE.name();
        }
        return switch (status) {
            case SUCCEEDED -> OjVerdict.AC.name();
            case TIME_LIMIT_EXCEEDED -> OjVerdict.TLE.name();
            case MEMORY_LIMIT_EXCEEDED -> OjVerdict.MLE.name();
            case OUTPUT_LIMIT_EXCEEDED -> OjVerdict.OLE.name();
            case RUNTIME_ERROR, SECURITY_VIOLATION -> OjVerdict.RE.name();
            case INTERNAL_ERROR -> OjVerdict.SE.name();
            case COMPILE_FAILED -> OjVerdict.CE.name();
        };
    }

    static boolean textEqualsRtrim(String expected, String actual) {
        return normalize(expected).equals(normalize(actual));
    }

    private static String normalize(String text) {
        if (text == null) {
            return "";
        }
        String unified = text.replace("\r\n", "\n").replace('\r', '\n');
        String[] lines = unified.split("\n", -1);
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < lines.length; i++) {
            String line = rtrim(lines[i]);
            if (i > 0) {
                sb.append('\n');
            }
            sb.append(line);
        }
        // 去掉全文末尾多余空行
        int end = sb.length();
        while (end > 0 && sb.charAt(end - 1) == '\n') {
            end--;
        }
        return sb.substring(0, end);
    }

    private static String rtrim(String s) {
        int end = s.length();
        while (end > 0 && Character.isWhitespace(s.charAt(end - 1))) {
            end--;
        }
        return s.substring(0, end);
    }

    private static String pickWorst(List<Map<String, Object>> caseResults) {
        int bestIdx = PRIORITY.size() - 1;
        for (Map<String, Object> row : caseResults) {
            Object st = row.get("status");
            int idx = PRIORITY.indexOf(String.valueOf(st));
            if (idx >= 0 && idx < bestIdx) {
                bestIdx = idx;
            }
        }
        return PRIORITY.get(bestIdx);
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
        return StringUtils.hasText(t) ? t : null;
    }

    private static String firstNonBlank(String... values) {
        if (values == null) {
            return null;
        }
        for (String v : values) {
            if (StringUtils.hasText(v)) {
                return v;
            }
        }
        return null;
    }

    /** SparkSandbox 测例指标在 {@code run} 子对象内（cpu_time_ms / memory_bytes）。 */
    private static Integer caseTimeMs(JsonNode caseNode) {
        Integer direct = intOrNull(caseNode, "time_ms", "cpu_time_ms", "real_time_ms");
        if (direct != null) {
            return direct;
        }
        JsonNode run = caseNode.path("run");
        if (run.isMissingNode() || run.isNull()) {
            return null;
        }
        return intOrNull(run, "cpu_time_ms", "time_ms", "real_time_ms");
    }

    private static Long caseMemoryBytes(JsonNode caseNode) {
        Long direct = longOrNull(caseNode, "memory_bytes", "memory");
        if (direct != null) {
            return direct;
        }
        JsonNode run = caseNode.path("run");
        if (run.isMissingNode() || run.isNull()) {
            return null;
        }
        return longOrNull(run, "memory_bytes", "memory");
    }

    private static Integer intOrNull(JsonNode node, String... fields) {
        for (String f : fields) {
            JsonNode v = node.get(f);
            if (v != null && v.isNumber()) {
                return v.asInt();
            }
            if (v != null && v.isTextual()) {
                try {
                    return Integer.parseInt(v.asText());
                } catch (NumberFormatException ignored) {
                    // continue
                }
            }
        }
        return null;
    }

    private static Long longOrNull(JsonNode node, String... fields) {
        for (String f : fields) {
            JsonNode v = node.get(f);
            if (v != null && v.isNumber()) {
                return v.asLong();
            }
            if (v != null && v.isTextual()) {
                try {
                    return Long.parseLong(v.asText());
                } catch (NumberFormatException ignored) {
                    // continue
                }
            }
        }
        return null;
    }

    /** 整单裁决结果。 */
    @lombok.Builder
    @lombok.Data
    public static class AggregateResult {
        private String status;
        private int score;
        private Integer timeMs;
        private Long memoryBytes;
        private String compileOutput;
        private String judgeMessage;
        private List<Map<String, Object>> caseResults;

        static AggregateResult systemError(String message) {
            return AggregateResult.builder()
                    .status(OjVerdict.SE.name())
                    .score(0)
                    .judgeMessage(message)
                    .caseResults(List.of())
                    .build();
        }
    }
}
