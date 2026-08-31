package github.jiangbyte.io.oj.modules.judge.heartbeat;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import github.jiangbyte.io.common.core.exception.BizException;
import github.jiangbyte.io.oj.modules.judge.enums.OjCircuitState;
import github.jiangbyte.io.oj.modules.judge.enums.OjJudgeAdminStatus;
import github.jiangbyte.io.oj.modules.judge.enums.OjJudgeRuntimeStatus;
import github.jiangbyte.io.oj.modules.judge.node.entity.OjJudgeNode;
import github.jiangbyte.io.oj.modules.judge.node.mapper.OjJudgeNodeMapper;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

/**
 * 沙箱主动心跳：按 hostname:port 推导节点并 upsert（不覆盖人工运维字段含 base_url）。
 *
 * Author: Charlie
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SandboxHeartbeatService {

    private final OjJudgeNodeMapper ojJudgeNodeMapper;
    private final ObjectMapper objectMapper;

    @Transactional
    public void upsertFromHeartbeat(JsonNode body) {
        JsonNode node = body.path("node");
        JsonNode listen = body.path("listen");
        JsonNode capacity = body.path("capacity");

        String hostname = text(node, "hostname");
        int port = listen.path("port").asInt(0);
        if (!StringUtils.hasText(hostname) || port <= 0 || port > 65535) {
            throw new BizException(400, "heartbeat missing hostname or listen.port");
        }

        String ip = resolveIp(node);
        if (!StringUtils.hasText(ip)) {
            throw new BizException(400, "heartbeat missing usable IP (primary_ip / ips)");
        }

        String code = hostname.trim() + ":" + port;
        String baseUrl = "http://" + ip.trim() + ":" + port;
        int maxConcurrency = capacity.path("max_concurrency").asInt(0);
        if (maxConcurrency < 1) {
            maxConcurrency = 4;
        }

        OffsetDateTime now = OffsetDateTime.now(ZoneOffset.UTC);
        Map<String, Object> extra = buildExtra(body);
        List<String> languageKeys = extractLanguageKeys(body.path("languages"));

        OjJudgeNode existing = ojJudgeNodeMapper.selectOne(
                Wrappers.<OjJudgeNode>lambdaQuery().eq(OjJudgeNode::getCode, code).last("LIMIT 1"));

        if (existing == null) {
            OjJudgeNode created = new OjJudgeNode();
            created.setCode(code);
            created.setName(code);
            created.setBaseUrl(baseUrl);
            created.setSigningEnabled(true);
            created.setSigningSecretCipher(null);
            created.setAdminStatus(OjJudgeAdminStatus.ENABLED.name());
            created.setRuntimeStatus(OjJudgeRuntimeStatus.ONLINE.name());
            created.setCircuitState(OjCircuitState.CLOSED.name());
            created.setWeight(100);
            created.setPriority(100);
            created.setMaxConcurrency(maxConcurrency);
            created.setInflightCount(0);
            created.setEpoch(0L);
            created.setTotalDispatch(0L);
            created.setTotalSuccess(0L);
            created.setTotalTransportFail(0L);
            created.setConsecutiveFailCount(0);
            created.setLastHeartbeatAt(now);
            created.setSupportedLanguages(languageKeys);
            created.setExtra(extra);
            ojJudgeNodeMapper.insert(created);
            log.info("sandbox heartbeat registered node code={} baseUrl={} langs={}",
                    code, baseUrl, languageKeys.size());
            return;
        }

        // base_url 仅新建时写入初始值，后续由 Admin 运维维护，心跳不覆盖
        existing.setMaxConcurrency(maxConcurrency);
        existing.setSigningEnabled(true);
        existing.setLastHeartbeatAt(now);
        existing.setSupportedLanguages(languageKeys);
        existing.setExtra(extra);
        // 心跳成功不得单独关 OPEN 熔断，也不得清零 consecutive_fail（避免 health 通但 run_cases 全挂）
        if (!OjCircuitState.OPEN.matches(existing.getCircuitState())) {
            existing.setRuntimeStatus(OjJudgeRuntimeStatus.ONLINE.name());
        }
        ojJudgeNodeMapper.updateById(existing);
    }

    /** 从 languages.keys 提取语言 ID；无 keys 时返回空列表（调度侧视为全语言）。 */
    private static List<String> extractLanguageKeys(JsonNode languages) {
        List<String> keys = new ArrayList<>();
        if (languages == null || languages.isMissingNode() || languages.isNull()) {
            return keys;
        }
        JsonNode arr = languages.path("keys");
        if (!arr.isArray()) {
            return keys;
        }
        for (JsonNode item : arr) {
            String id = item.asText("");
            if (StringUtils.hasText(id)) {
                keys.add(id.trim());
            }
        }
        return keys;
    }

    private static String resolveIp(JsonNode node) {
        String primary = text(node, "primary_ip");
        if (StringUtils.hasText(primary) && !primary.startsWith("127.")) {
            return primary.trim();
        }
        JsonNode ips = node.path("ips");
        if (ips.isArray()) {
            for (JsonNode item : ips) {
                String ip = item.asText("");
                if (StringUtils.hasText(ip) && !ip.startsWith("127.")) {
                    return ip.trim();
                }
            }
        }
        return null;
    }

    private Map<String, Object> buildExtra(JsonNode body) {
        Map<String, Object> extra = new LinkedHashMap<>();
        putIfPresent(extra, "version", text(body, "version"));
        if (body.path("uptime_seconds").isNumber()) {
            extra.put("uptime_seconds", body.path("uptime_seconds").asDouble());
        }
        putConverted(extra, "node", sliceObject(body.path("node"), List.of(
                "hostname", "pid", "platform", "arch", "python", "primary_ip", "ips")));
        putConverted(extra, "resources", body.path("resources"));
        putConverted(extra, "pool", body.path("pool"));
        putConverted(extra, "compile_cache", body.path("compile_cache"));
        putConverted(extra, "capacity", body.path("capacity"));
        putConverted(extra, "listen", body.path("listen"));
        putConverted(extra, "languages", truncateLanguages(body.path("languages")));

        try {
            String json = objectMapper.writeValueAsString(extra);
            if (json.length() > 12_000) {
                Map<String, Object> trimmed = new LinkedHashMap<>();
                trimmed.put("version", extra.get("version"));
                trimmed.put("uptime_seconds", extra.get("uptime_seconds"));
                trimmed.put("node", extra.get("node"));
                trimmed.put("capacity", extra.get("capacity"));
                trimmed.put("listen", extra.get("listen"));
                trimmed.put("languages", extra.get("languages"));
                return trimmed;
            }
        } catch (Exception ignored) {
            // keep full extra
        }
        return extra.isEmpty() ? new HashMap<>() : extra;
    }

    /** languages 过大时只保留 keys + 前若干条公开元数据。 */
    private JsonNode truncateLanguages(JsonNode languages) {
        if (languages == null || languages.isMissingNode() || !languages.isObject()) {
            return languages;
        }
        var out = objectMapper.createObjectNode();
        JsonNode keys = languages.get("keys");
        if (keys != null) {
            out.set("keys", keys);
        }
        JsonNode list = languages.get("languages");
        if (list != null && list.isArray()) {
            var trimmed = objectMapper.createArrayNode();
            int limit = Math.min(list.size(), 64);
            for (int i = 0; i < limit; i++) {
                trimmed.add(list.get(i));
            }
            out.set("languages", trimmed);
            if (list.size() > limit) {
                out.put("languages_truncated", true);
                out.put("languages_total", list.size());
            }
        }
        return out;
    }

    private JsonNode sliceObject(JsonNode src, List<String> fields) {
        if (src == null || !src.isObject()) {
            return null;
        }
        var obj = objectMapper.createObjectNode();
        for (String f : fields) {
            JsonNode v = src.get(f);
            if (v != null && !v.isNull()) {
                obj.set(f, v);
            }
        }
        return obj;
    }

    @SuppressWarnings("unchecked")
    private void putConverted(Map<String, Object> target, String key, JsonNode src) {
        if (src == null || src.isMissingNode() || src.isNull()) {
            return;
        }
        if (!src.isObject() && !src.isArray()) {
            return;
        }
        try {
            Object converted = objectMapper.convertValue(src, Object.class);
            if (converted instanceof Map<?, ?> map && map.isEmpty()) {
                return;
            }
            target.put(key, converted);
        } catch (Exception ex) {
            log.debug("heartbeat extra convert skip key={}: {}", key, ex.toString());
        }
    }

    private static void putIfPresent(Map<String, Object> map, String key, String value) {
        if (StringUtils.hasText(value)) {
            map.put(key, value);
        }
    }

    private static String text(JsonNode parent, String field) {
        if (parent == null || parent.isMissingNode()) {
            return "";
        }
        String v = parent.path(field).asText("");
        return v == null ? "" : v.trim();
    }
}
