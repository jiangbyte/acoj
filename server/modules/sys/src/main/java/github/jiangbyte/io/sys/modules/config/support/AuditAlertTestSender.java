package github.jiangbyte.io.sys.modules.config.support;

import github.jiangbyte.io.common.core.exception.BizException;
import github.jiangbyte.io.common.core.security.SafeUrlValidator;
import github.jiangbyte.io.common.notify.push.PushSenderFacade;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import tools.jackson.databind.ObjectMapper;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import lombok.RequiredArgsConstructor;

/**
 * 审计告警 Webhook 测试发送器。
 *
 * Author: Charlie
 */
@Component
@RequiredArgsConstructor
public class AuditAlertTestSender {

    private static final String TEST_TITLE = "审计告警测试";
    private static final String TEST_CONTENT =
            "HEI-Boot 审计告警系统测试消息\n\n如果收到此消息，说明配置正确。";

    private final ObjectMapper objectMapper;
    private final PushSenderFacade pushSenderFacade;
    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .followRedirects(HttpClient.Redirect.NEVER)
            .build();

    public void testWebhook(String webhookUrl, String webhookSecret) {
        if (!StringUtils.hasText(webhookUrl)) {
            throw new BizException("Webhook URL 为空");
        }
        try {
            SafeUrlValidator.validate(webhookUrl.trim());
        } catch (IllegalArgumentException ex) {
            throw new BizException("Webhook URL 不安全: " + ex.getMessage());
        }
        Map<String, Object> payload = new HashMap<>();
        payload.put("msg_type", "text");
        payload.put("content", Map.of("text", TEST_CONTENT));
        if (StringUtils.hasText(webhookSecret)) {
            String timestamp = String.valueOf(System.currentTimeMillis() / 1000);
            payload.put("timestamp", timestamp);
            payload.put("sign", signFeishu(timestamp, webhookSecret));
        }
        postJson(webhookUrl.trim(), payload, "Webhook");
    }

    public void testPush() {
        pushSenderFacade.send(TEST_TITLE, TEST_CONTENT);
    }

    private void postJson(String url, Map<String, Object> payload, String label) {
        try {
            String body = objectMapper.writeValueAsString(payload);
            HttpRequest request = HttpRequest.newBuilder(URI.create(url))
                    .timeout(Duration.ofSeconds(10))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(body))
                    .build();
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() >= 400) {
                String snippet = response.body() == null
                        ? ""
                        : response.body().substring(0, Math.min(200, response.body().length()));
                throw new BizException(label + "推送失败: HTTP " + response.statusCode()
                        + (StringUtils.hasText(snippet) ? ": " + snippet : ""));
            }
        } catch (BizException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new BizException(label + "推送失败: " + ex.getMessage());
        }
    }

    private static String signFeishu(String timestamp, String secret) {
        try {
            String stringToSign = timestamp + "\n" + secret;
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(stringToSign.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            return Base64.getEncoder().encodeToString(mac.doFinal(new byte[]{}));
        } catch (Exception ex) {
            throw new BizException("签名失败: " + ex.getMessage());
        }
    }
}
