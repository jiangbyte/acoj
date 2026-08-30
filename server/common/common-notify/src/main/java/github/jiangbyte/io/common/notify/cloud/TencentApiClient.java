package github.jiangbyte.io.common.notify.cloud;

import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.HexFormat;
import java.util.Map;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

/**
 * 腾讯云 API 调用客户端：封装签名与 HTTP 请求，供短信/推送等通道复用。
 *
 * Author: Charlie
 */
public final class TencentApiClient {

    private static final ObjectMapper MAPPER = new ObjectMapper();
    private static final DateTimeFormatter DATE = DateTimeFormatter.ofPattern("yyyy-MM-dd").withZone(ZoneOffset.UTC);
    private static final HttpClient HTTP = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();

    private TencentApiClient() {
    }

    @SuppressWarnings("unchecked")
    public static Map<String, Object> post(
            String service,
            String host,
            String action,
            String version,
            String region,
            String secretId,
            String secretKey,
            Map<String, Object> payload) {
        try {
            String body = MAPPER.writeValueAsString(payload == null ? Map.of() : payload);
            String timestamp = String.valueOf(Instant.now().getEpochSecond());
            String date = DATE.format(Instant.ofEpochSecond(Long.parseLong(timestamp)));

            String canonicalHeaders = "content-type:application/json; charset=utf-8\nhost:" + host + "\n";
            String signedHeaders = "content-type;host";
            String canonicalRequest = "POST\n/\n\n"
                    + canonicalHeaders + "\n"
                    + signedHeaders + "\n"
                    + sha256Hex(body);
            String credentialScope = date + "/" + service + "/tc3_request";
            String stringToSign = "TC3-HMAC-SHA256\n"
                    + timestamp + "\n"
                    + credentialScope + "\n"
                    + sha256Hex(canonicalRequest);

            byte[] secretDate = hmacSha256(("TC3" + secretKey).getBytes(StandardCharsets.UTF_8), date);
            byte[] secretService = hmacSha256(secretDate, service);
            byte[] secretSigning = hmacSha256(secretService, "tc3_request");
            String signature = HexFormat.of().formatHex(hmacSha256(secretSigning, stringToSign));
            String authorization = "TC3-HMAC-SHA256 "
                    + "Credential=" + secretId + "/" + credentialScope + ", "
                    + "SignedHeaders=" + signedHeaders + ", "
                    + "Signature=" + signature;

            HttpRequest request = HttpRequest.newBuilder(URI.create("https://" + host))
                    .timeout(Duration.ofSeconds(30))
                    .header("Authorization", authorization)
                    .header("Content-Type", "application/json; charset=utf-8")
                    .header("Host", host)
                    .header("X-TC-Action", action)
                    .header("X-TC-Timestamp", timestamp)
                    .header("X-TC-Version", version)
                    .header("X-TC-Region", region)
                    .POST(HttpRequest.BodyPublishers.ofString(body))
                    .build();
            HttpResponse<String> response = HTTP.send(request, HttpResponse.BodyHandlers.ofString());
            Map<String, Object> data = parseJson(response.body());
            if (response.statusCode() >= 400) {
                throw new IllegalStateException("Tencent API HTTP " + response.statusCode() + ": " + data);
            }
            Object responseNode = data.get("Response");
            if (responseNode instanceof Map<?, ?> responseMap) {
                Object error = responseMap.get("Error");
                if (error instanceof Map<?, ?> err) {
                    throw new IllegalStateException(
                            "Tencent API error: " + err.get("Code") + " " + err.get("Message"));
                }
                return (Map<String, Object>) responseMap;
            }
            return data;
        } catch (IllegalStateException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new IllegalStateException("Tencent API request failed: " + ex.getMessage(), ex);
        }
    }

    private static byte[] hmacSha256(byte[] key, String msg) throws Exception {
        Mac mac = Mac.getInstance("HmacSHA256");
        mac.init(new SecretKeySpec(key, "HmacSHA256"));
        return mac.doFinal(msg.getBytes(StandardCharsets.UTF_8));
    }

    private static String sha256Hex(String content) throws Exception {
        java.security.MessageDigest digest = java.security.MessageDigest.getInstance("SHA-256");
        return HexFormat.of().formatHex(digest.digest(content.getBytes(StandardCharsets.UTF_8)));
    }

    private static Map<String, Object> parseJson(String body) {
        if (body == null || body.isBlank()) {
            return Map.of();
        }
        try {
            return MAPPER.readValue(body, new TypeReference<>() {
            });
        } catch (Exception ex) {
            return Map.of("raw", body);
        }
    }
}
