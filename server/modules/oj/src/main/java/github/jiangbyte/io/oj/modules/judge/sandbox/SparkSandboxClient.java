package github.jiangbyte.io.oj.modules.judge.sandbox;

import github.jiangbyte.io.oj.config.OjProperties;
import github.jiangbyte.io.oj.modules.judge.node.entity.OjJudgeNode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * SparkSandbox HTTP 客户端：run_cases，可选 HMAC。
 * <p>节点探活由沙箱主动心跳完成，本客户端不再调用 {@code GET /v1/health}。
 *
 * Author: Charlie
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class SparkSandboxClient {

    private final OjProperties ojProperties;
    private final ObjectMapper objectMapper;
    private final HttpClient httpClient = HttpClient.newBuilder()
            .version(HttpClient.Version.HTTP_1_1)
            .connectTimeout(Duration.ofSeconds(5))
            .build();

    public RunCasesResult runCases(OjJudgeNode node, RunCasesRequest request) {
        String url = trimSlash(node.getBaseUrl()) + "/v1/run_cases";
        try {
            Map<String, Object> body = new LinkedHashMap<>();
            body.put("language", request.language());
            body.put("source", request.source());
            List<Map<String, Object>> cases = new ArrayList<>();
            for (CaseInput c : request.cases()) {
                Map<String, Object> item = new LinkedHashMap<>();
                item.put("case_id", c.caseId());
                item.put("stdin", c.stdin() == null ? "" : c.stdin());
                cases.add(item);
            }
            body.put("cases", cases);
            body.put("stop_on_first_error", request.stopOnFirstError());
            body.put("case_parallelism", request.caseParallelism());
            Map<String, Object> limits = new LinkedHashMap<>();
            limits.put("cpu_time_ms", request.cpuTimeMs());
            limits.put("real_time_ms", request.realTimeMs());
            limits.put("memory_bytes", request.memoryBytes());
            if (request.stackBytes() != null) {
                limits.put("stack_bytes", request.stackBytes());
            }
            if (request.outputBytes() != null) {
                limits.put("output_bytes", request.outputBytes());
            }
            body.put("limits", limits);

            byte[] json = objectMapper.writeValueAsBytes(body);
            Duration timeout = Duration.ofMillis(Math.max(30_000L, request.realTimeMs() * Math.max(1, request.cases().size()) + 30_000L));

            HttpRequest.Builder builder = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .timeout(timeout)
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofByteArray(json));
            signIfNeeded(builder, node);

            HttpResponse<String> response = httpClient.send(builder.build(), HttpResponse.BodyHandlers.ofString());
            int status = response.statusCode();
            String responseBody = response.body() == null ? "" : response.body();
            if (status == 502 || status == 503 || status == 504) {
                return RunCasesResult.transportFail(status, formatHttpError(status, responseBody), null);
            }
            if (status < 200 || status >= 300) {
                log.warn("sandbox run_cases rejected node={} status={} detail={}",
                        node.getId(), status, truncate(responseBody, 256));
                return RunCasesResult.transportFail(status, formatHttpError(status, responseBody), null);
            }
            JsonNode root;
            try {
                root = objectMapper.readTree(responseBody.isEmpty() ? "{}" : responseBody);
            } catch (Exception parseEx) {
                return RunCasesResult.transportFail(status, "响应无法解析", null);
            }
            String topStatus = root.path("status").asText("");
            if ("internal_error".equalsIgnoreCase(topStatus)
                    && !root.path("compile").isObject()
                    && !root.path("cases").isArray()) {
                return RunCasesResult.transportFail(status, "sandbox internal_error", root);
            }
            return RunCasesResult.ok(status, root);
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            return RunCasesResult.transportFail(0, "interrupted", null);
        } catch (Exception ex) {
            String msg = ex.getClass().getSimpleName() + ": " + ex.getMessage();
            log.warn("sandbox run_cases transport fail node={} err={}", node.getId(), msg);
            return RunCasesResult.transportFail(0, msg, null);
        }
    }

    private void signIfNeeded(HttpRequest.Builder builder, OjJudgeNode node) {
        if (!Boolean.TRUE.equals(node.getSigningEnabled())) {
            return;
        }
        String secret = resolveSecret(node);
        if (!StringUtils.hasText(secret)) {
            return;
        }
        String ts = String.valueOf(System.currentTimeMillis() / 1000L);
        String nonce = UUID.randomUUID().toString().replace("-", "");
        String signature = SparkSandboxSigning.hmacSha256Hex(
                secret, SparkSandboxSigning.canonical(ts, nonce));
        builder.header(SparkSandboxSigning.HEADER_TIMESTAMP, ts);
        builder.header(SparkSandboxSigning.HEADER_NONCE, nonce);
        builder.header(SparkSandboxSigning.HEADER_SIGNATURE, signature);
    }

    private String resolveSecret(OjJudgeNode node) {
        // P0：signingSecretCipher 字段暂作明文密钥使用
        if (StringUtils.hasText(node.getSigningSecretCipher())) {
            return node.getSigningSecretCipher().trim();
        }
        String def = ojProperties.getJudge().getDefaultSigningSecret();
        return StringUtils.hasText(def) ? def.trim() : null;
    }

    /** 连接失败或 5xx 视为节点基础设施故障；4xx 为请求/配置问题，不应标记节点不健康。 */
    public static boolean isNodeInfrastructureFailure(int httpStatus) {
        return httpStatus == 0 || httpStatus >= 500;
    }

    private String formatHttpError(int status, String body) {
        String detail = extractErrorDetail(body);
        return detail != null ? "HTTP " + status + ": " + detail : "HTTP " + status;
    }

    private String extractErrorDetail(String body) {
        if (!StringUtils.hasText(body)) {
            return null;
        }
        String trimmed = body.trim();
        if (trimmed.startsWith("{")) {
            try {
                JsonNode root = objectMapper.readTree(trimmed);
                String detail = root.path("detail").asText("");
                if (StringUtils.hasText(detail)) {
                    return detail.trim();
                }
                String message = root.path("message").asText("");
                if (StringUtils.hasText(message)) {
                    return message.trim();
                }
            } catch (Exception ignored) {
                // fall through
            }
        }
        return trimmed.length() <= 200 ? trimmed : trimmed.substring(0, 200);
    }

    private static String truncate(String text, int max) {
        if (text == null) {
            return null;
        }
        return text.length() <= max ? text : text.substring(0, max);
    }

    private static String trimSlash(String baseUrl) {
        if (baseUrl == null) {
            return "";
        }
        String s = baseUrl.trim();
        while (s.endsWith("/")) {
            s = s.substring(0, s.length() - 1);
        }
        return s;
    }

    public record CaseInput(String caseId, String stdin) {
    }

    public record RunCasesRequest(
            String language,
            String source,
            List<CaseInput> cases,
            boolean stopOnFirstError,
            int caseParallelism,
            int cpuTimeMs,
            int realTimeMs,
            long memoryBytes,
            Long stackBytes,
            Long outputBytes) {
    }

    public record RunCasesResult(
            boolean transportFail,
            int httpStatus,
            String errorMessage,
            JsonNode body) {
        public static RunCasesResult ok(int httpStatus, JsonNode body) {
            return new RunCasesResult(false, httpStatus, null, body);
        }

        public static RunCasesResult transportFail(int httpStatus, String error, JsonNode body) {
            return new RunCasesResult(true, httpStatus, error, body);
        }
    }
}
