package github.jiangbyte.io.common.core.vault;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.core.Ordered;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;
import org.springframework.util.StringUtils;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Vault KV 注入：{@code hei.vault.enabled=true} 时将 secret data 写入高优先级 PropertySource。
 * <p>
 * 支持 Token 或 AppRole。KV v2 路径：{@code /v1/{mount}/data/{path}}。
 * Secret 中的键若形如 Spring 属性名（含点），将直接覆盖对应配置，例如 {@code hei.config.crypto-key}。
 *
 * Author: Charlie
 */
public class VaultEnvironmentPostProcessor implements EnvironmentPostProcessor, Ordered {

    public static final String PROPERTY_SOURCE = "heiVaultSecrets";

    private static final ObjectMapper MAPPER = new ObjectMapper();

    @Override
    public void postProcessEnvironment(ConfigurableEnvironment environment, SpringApplication application) {
        boolean enabled = environment.getProperty("hei.vault.enabled", Boolean.class, false);
        if (!enabled) {
            return;
        }
        String uri = environment.getProperty("hei.vault.uri", "");
        if (!StringUtils.hasText(uri)) {
            failOrWarn(environment, "hei.vault.enabled=true but hei.vault.uri is empty");
            return;
        }
        try {
            String token = resolveToken(environment, uri);
            Map<String, Object> secrets = fetchKv(environment, uri, token);
            if (!secrets.isEmpty()) {
                environment.getPropertySources().addFirst(new MapPropertySource(PROPERTY_SOURCE, secrets));
            }
        } catch (Exception ex) {
            failOrWarn(environment, "Failed to load secrets from Vault: " + ex.getMessage());
        }
    }

    private String resolveToken(ConfigurableEnvironment environment, String uri) throws Exception {
        String token = environment.getProperty("hei.vault.token", "");
        if (StringUtils.hasText(token)) {
            return token.trim();
        }
        String roleId = environment.getProperty("hei.vault.role-id", "");
        String secretId = environment.getProperty("hei.vault.secret-id", "");
        if (!StringUtils.hasText(roleId) || !StringUtils.hasText(secretId)) {
            throw new IllegalStateException("Provide hei.vault.token or AppRole role-id/secret-id");
        }
        String body = MAPPER.createObjectNode()
                .put("role_id", roleId.trim())
                .put("secret_id", secretId.trim())
                .toString();
        HttpRequest.Builder req = HttpRequest.newBuilder()
                .uri(URI.create(trimSlash(uri) + "/v1/auth/approle/login"))
                .timeout(Duration.ofSeconds(10))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(body));
        applyNamespace(environment, req);
        HttpResponse<String> resp = HttpClient.newHttpClient().send(req.build(), HttpResponse.BodyHandlers.ofString());
        if (resp.statusCode() >= 300) {
            throw new IllegalStateException("AppRole login failed HTTP " + resp.statusCode());
        }
        JsonNode root = MAPPER.readTree(resp.body());
        String clientToken = root.path("auth").path("client_token").asText(null);
        if (!StringUtils.hasText(clientToken)) {
            throw new IllegalStateException("AppRole login response missing client_token");
        }
        return clientToken;
    }

    private Map<String, Object> fetchKv(ConfigurableEnvironment environment, String uri, String token) throws Exception {
        String mount = environment.getProperty("hei.vault.kv-mount", "secret");
        String path = environment.getProperty("hei.vault.kv-path", "hei/boot");
        String url = trimSlash(uri) + "/v1/" + trimSlash(mount) + "/data/" + trimLeadingSlash(path);
        HttpRequest.Builder req = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .timeout(Duration.ofSeconds(10))
                .header("X-Vault-Token", token)
                .GET();
        applyNamespace(environment, req);
        HttpResponse<String> resp = HttpClient.newHttpClient().send(req.build(), HttpResponse.BodyHandlers.ofString());
        if (resp.statusCode() >= 300) {
            throw new IllegalStateException("KV read failed HTTP " + resp.statusCode() + " for " + url);
        }
        JsonNode data = MAPPER.readTree(resp.body()).path("data").path("data");
        Map<String, Object> out = new LinkedHashMap<>();
        if (data != null && data.isObject()) {
            Iterator<Map.Entry<String, JsonNode>> fields = data.properties().iterator();
            while (fields.hasNext()) {
                Map.Entry<String, JsonNode> e = fields.next();
                JsonNode v = e.getValue();
                if (v == null || v.isNull()) {
                    continue;
                }
                out.put(e.getKey(), v.isValueNode() ? v.asText() : v.toString());
            }
        }
        return out;
    }

    private static void applyNamespace(ConfigurableEnvironment environment, HttpRequest.Builder req) {
        String ns = environment.getProperty("hei.vault.namespace", "");
        if (StringUtils.hasText(ns)) {
            req.header("X-Vault-Namespace", ns.trim());
        }
    }

    private static void failOrWarn(ConfigurableEnvironment environment, String message) {
        boolean failFast = environment.getProperty("hei.vault.fail-fast", Boolean.class, true);
        if (failFast) {
            throw new IllegalStateException(message);
        }
        System.err.println("[hei.vault] " + message);
    }

    private static String trimSlash(String s) {
        if (s == null) {
            return "";
        }
        while (s.endsWith("/")) {
            s = s.substring(0, s.length() - 1);
        }
        return s;
    }

    private static String trimLeadingSlash(String s) {
        if (s == null) {
            return "";
        }
        while (s.startsWith("/")) {
            s = s.substring(1);
        }
        return s;
    }

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE + 20;
    }
}
