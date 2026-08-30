package github.jiangbyte.io.common.notify.push;

import tools.jackson.databind.ObjectMapper;
import github.jiangbyte.io.common.core.exception.BizException;
import github.jiangbyte.io.common.core.security.SafeUrlValidator;
import github.jiangbyte.io.common.notify.NotifyConfigSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Base64;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

/**
 * 推送发送门面：按厂商配置下发 App 推送消息。
 *
 * Author: Charlie
 */
public class PushSenderFacade {

    private static final Logger log = LoggerFactory.getLogger(PushSenderFacade.class);
    private static final ObjectMapper MAPPER = new ObjectMapper();
    private static final HttpClient HTTP = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .followRedirects(HttpClient.Redirect.NEVER)
            .build();

    private final NotifyConfigSource config;

    public PushSenderFacade(NotifyConfigSource config) {
        this.config = config;
    }

    /** 按配置下发推送消息。 */
    public void send(String title, String content) {
        String engine = config.get("DEFAULT_MESSAGE_PUSH_ENGINE", "DINGTALK").trim().toUpperCase(Locale.ROOT);
        String text = StringUtils.hasText(title) ? title + "\n" + content : content;
        switch (engine) {
            case "DINGTALK" -> sendDingtalk(text);
            case "LARK", "FEISHU" -> sendLark(text);
            case "WECOM", "WECHAT_WORK", "WECHATWORK" -> sendWecom(text);
            default -> throw new BizException("Unsupported push engine: " + engine);
        }
    }

    private void sendDingtalk(String text) {
        String webhook = trim(config.get("PUSH_DINGTALK_WEBHOOK", ""));
        if (!StringUtils.hasText(webhook)) {
            throw new BizException("钉钉推送未配置: PUSH_DINGTALK_WEBHOOK");
        }
        String secret = trim(config.get("PUSH_DINGTALK_SECRET", ""));
        String url = webhook;
        if (StringUtils.hasText(secret)) {
            String timestamp = String.valueOf(System.currentTimeMillis());
            String sign = signDingtalk(timestamp, secret);
            String sep = url.contains("?") ? "&" : "?";
            url = url + sep + "timestamp=" + timestamp + "&sign=" + sign;
        }
        postJson(url, Map.of("msgtype", "text", "text", Map.of("content", text)), "钉钉");
    }

    private void sendLark(String text) {
        String webhook = trim(config.get("PUSH_LARK_WEBHOOK", ""));
        if (!StringUtils.hasText(webhook)) {
            throw new BizException("飞书推送未配置: PUSH_LARK_WEBHOOK");
        }
        Map<String, Object> payload = new HashMap<>();
        payload.put("msg_type", "text");
        payload.put("content", Map.of("text", text));
        String secret = trim(config.get("PUSH_LARK_SECRET", ""));
        if (StringUtils.hasText(secret)) {
            String timestamp = String.valueOf(System.currentTimeMillis() / 1000);
            payload.put("timestamp", timestamp);
            payload.put("sign", signFeishu(timestamp, secret));
        }
        postJson(webhook, payload, "飞书");
    }

    private void sendWecom(String text) {
        String webhook = trim(config.get("PUSH_WECHAT_WORK_WEBHOOK", ""));
        if (!StringUtils.hasText(webhook)) {
            throw new BizException("企业微信推送未配置: PUSH_WECHAT_WORK_WEBHOOK");
        }
        postJson(webhook, Map.of("msgtype", "text", "text", Map.of("content", text)), "企业微信");
    }

    private void postJson(String url, Map<String, Object> payload, String label) {
        try {
            SafeUrlValidator.validate(url);
            String body = MAPPER.writeValueAsString(payload);
            HttpRequest request = HttpRequest.newBuilder(URI.create(url))
                    .timeout(Duration.ofSeconds(10))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(body))
                    .build();
            HttpResponse<String> response = HTTP.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() >= 400) {
                throw new BizException(label + "推送失败: HTTP " + response.statusCode());
            }
        } catch (BizException ex) {
            throw ex;
        } catch (IllegalArgumentException ex) {
            throw new BizException(label + "推送失败: " + ex.getMessage());
        } catch (Exception ex) {
            log.error("{} push failed", label, ex);
            throw new BizException(label + "推送失败");
        }
    }

    private static String signFeishu(String timestamp, String secret) {
        try {
            String stringToSign = timestamp + "\n" + secret;
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(stringToSign.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            return Base64.getEncoder().encodeToString(mac.doFinal(new byte[0]));
        } catch (Exception ex) {
            throw new BizException("签名失败: " + ex.getMessage());
        }
    }

    private static String signDingtalk(String timestamp, String secret) {
        try {
            String stringToSign = timestamp + "\n" + secret;
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            byte[] signData = mac.doFinal(stringToSign.getBytes(StandardCharsets.UTF_8));
            return URLEncoder.encode(Base64.getEncoder().encodeToString(signData), StandardCharsets.UTF_8);
        } catch (Exception ex) {
            throw new BizException("签名失败: " + ex.getMessage());
        }
    }

    private static String trim(String value) {
        return value == null ? "" : value.trim();
    }
}
