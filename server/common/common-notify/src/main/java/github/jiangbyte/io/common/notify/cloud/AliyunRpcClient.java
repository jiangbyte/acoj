package github.jiangbyte.io.common.notify.cloud;

import cn.hutool.core.util.IdUtil;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.TreeMap;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

/**
 * 阿里云 RPC 调用客户端：封装签名与 HTTP 请求，供短信等通道复用。
 *
 * Author: Charlie
 */
public final class AliyunRpcClient {

    private static final ObjectMapper MAPPER = new ObjectMapper();
    private static final DateTimeFormatter TIMESTAMP =
            DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'").withZone(ZoneOffset.UTC);
    private static final HttpClient HTTP = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();

    private AliyunRpcClient() {
    }

    public static Map<String, Object> get(
            String endpoint,
            String accessKeyId,
            String accessKeySecret,
            String action,
            String version,
            Map<String, String> businessParams) {
        Map<String, String> params = new LinkedHashMap<>();
        params.put("Format", "JSON");
        params.put("Version", version);
        params.put("AccessKeyId", accessKeyId);
        params.put("SignatureMethod", "HMAC-SHA1");
        params.put("Timestamp", TIMESTAMP.format(Instant.now()));
        params.put("SignatureVersion", "1.0");
        params.put("SignatureNonce", IdUtil.simpleUUID());
        params.put("Action", action);
        if (businessParams != null) {
            params.putAll(businessParams);
        }
        params.put("Signature", signRpcParams(params, accessKeySecret));

        StringBuilder query = new StringBuilder();
        for (Map.Entry<String, String> entry : params.entrySet()) {
            if (!query.isEmpty()) {
                query.append('&');
            }
            query.append(urlEncode(entry.getKey())).append('=').append(urlEncode(entry.getValue()));
        }
        String url = "https://" + endpoint + "/?" + query;
        try {
            HttpRequest request = HttpRequest.newBuilder(URI.create(url))
                    .timeout(Duration.ofSeconds(30))
                    .GET()
                    .build();
            HttpResponse<String> response = HTTP.send(request, HttpResponse.BodyHandlers.ofString());
            Map<String, Object> data = parseJson(response.body());
            if (response.statusCode() >= 400) {
                throw new IllegalStateException("Aliyun RPC HTTP " + response.statusCode() + ": " + data);
            }
            Object code = data.get("Code");
            if (code != null) {
                String codeText = String.valueOf(code).toUpperCase();
                if (!"OK".equals(codeText) && !"200".equals(codeText)) {
                    throw new IllegalStateException("Aliyun RPC error: " + code + " " + data.get("Message"));
                }
            }
            return data;
        } catch (IllegalStateException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new IllegalStateException("Aliyun RPC request failed: " + ex.getMessage(), ex);
        }
    }

    static String signRpcParams(Map<String, String> params, String accessKeySecret) {
        TreeMap<String, String> sorted = new TreeMap<>(params);
        StringBuilder sortedQuery = new StringBuilder();
        for (Map.Entry<String, String> entry : sorted.entrySet()) {
            if (!sortedQuery.isEmpty()) {
                sortedQuery.append('&');
            }
            sortedQuery.append(percentEncode(entry.getKey()))
                    .append('=')
                    .append(percentEncode(entry.getValue() == null ? "" : entry.getValue()));
        }
        String stringToSign = "GET&%2F&" + percentEncode(sortedQuery.toString());
        try {
            Mac mac = Mac.getInstance("HmacSHA1");
            mac.init(new SecretKeySpec((accessKeySecret + "&").getBytes(StandardCharsets.UTF_8), "HmacSHA1"));
            return Base64.getEncoder().encodeToString(mac.doFinal(stringToSign.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception ex) {
            throw new IllegalStateException("Aliyun RPC sign failed", ex);
        }
    }

    private static String percentEncode(String value) {
        StringBuilder sb = new StringBuilder();
        for (byte b : value.getBytes(StandardCharsets.UTF_8)) {
            int c = b & 0xFF;
            if ((c >= 'A' && c <= 'Z')
                    || (c >= 'a' && c <= 'z')
                    || (c >= '0' && c <= '9')
                    || c == '-'
                    || c == '_'
                    || c == '.'
                    || c == '~') {
                sb.append((char) c);
            } else {
                sb.append(String.format("%%%02X", c));
            }
        }
        return sb.toString();
    }

    private static String urlEncode(String value) {
        return URLEncoder.encode(value == null ? "" : value, StandardCharsets.UTF_8);
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
