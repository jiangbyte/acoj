package github.jiangbyte.io.oj.modules.judge.schedule;

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
            "CE", "SE", "RE", "TLE", "MLE", "OLE", "WA", "AC");

    /**
     * 根据沙箱响应与期望输出聚合整单结果。
     *
     * @param stopOnFirstError 与传给沙箱一致
     */
    public AggregateResult aggregate(
            JsonNode sandboxResponse,
            List<CaseLoader.LoadedCase> cases,
            boolean stopOnFirstError) {
        if (sandboxResponse == null) {
            return AggregateResult.systemError("空沙箱响应");
        }

        String topStatus = text(sandboxResponse, "status");
        JsonNode compile = sandboxResponse.path("compile");
        String compileStatus = text(compile, "status");
        if ("compile_failed".equalsIgnoreCase(topStatus)
                || "compile_failed".equalsIgnoreCase(compileStatus)) {
            String compileOut = firstNonBlank(
                    text(compile, "stderr"),
                    text(compile, "stdout"),
                    text(sandboxResponse, "message"));
            return AggregateResult.builder()
                    .status("CE")
                    .score(0)
                    .compileOutput(compileOut)
                    .judgeMessage("Compile Error")
                    .caseResults(List.of())
                    .build();
        }

        Map<String, CaseLoader.LoadedCase> byKey = new LinkedHashMap<>();
        for (CaseLoader.LoadedCase c : cases) {
            byKey.put(c.caseKey(), c);
        }

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

                if ("AC".equals(verdict) || "succeeded".equalsIgnoreCase(sandboxStatus)) {
                    String stdout = firstNonBlank(text(node, "stdout"), "");
                    String expect = expected == null ? "" : expected.expectedStdout();
                    if (textEqualsRtrim(expect, stdout)) {
                        verdict = "AC";
                    } else {
                        verdict = "WA";
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

                if (!"AC".equals(verdict) && firstNonAc == null) {
                    firstNonAc = verdict;
                    if (stopOnFirstError) {
                        break;
                    }
                }
            }
        }

        String overall;
        if (stopOnFirstError && firstNonAc != null) {
            overall = firstNonAc;
        } else if (caseResults.isEmpty()) {
            if ("internal_error".equalsIgnoreCase(topStatus)) {
                return AggregateResult.systemError("sandbox internal_error");
            }
            overall = "SE";
        } else {
            overall = pickWorst(caseResults);
        }

        return AggregateResult.builder()
                .status(overall)
                .score("AC".equals(overall) ? 100 : 0)
                .timeMs(maxTime > 0 ? maxTime : null)
                .memoryBytes(maxMem > 0 ? maxMem : null)
                .caseResults(caseResults)
                .judgeMessage(overall)
                .compileOutput(text(compile, "stderr"))
                .build();
    }

    private static String mapCaseStatus(String sandboxStatus) {
        if (!StringUtils.hasText(sandboxStatus)) {
            return "SE";
        }
        return switch (sandboxStatus.toLowerCase()) {
            case "succeeded" -> "AC";
            case "time_limit_exceeded" -> "TLE";
            case "memory_limit_exceeded" -> "MLE";
            case "output_limit_exceeded" -> "OLE";
            case "runtime_error", "security_violation" -> "RE";
            case "internal_error" -> "SE";
            case "compile_failed" -> "CE";
            default -> "SE";
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
                    .status("SE")
                    .score(0)
                    .judgeMessage(message)
                    .caseResults(List.of())
                    .build();
        }
    }
}
