package github.jiangbyte.io.oj.modules.judge.heartbeat;

import github.jiangbyte.io.common.core.exception.BizException;
import github.jiangbyte.io.oj.config.OjProperties;
import github.jiangbyte.io.oj.modules.judge.sandbox.SparkSandboxHeartbeatCrypto;
import github.jiangbyte.io.oj.modules.judge.sandbox.SparkSandboxSigning;
import io.swagger.v3.oas.annotations.Hidden;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

/**
 * 沙箱主动心跳接收：HMAC 验签 → AES-GCM 解密信封 → upsert 执行机。
 *
 * Author: Charlie
 */
@Hidden
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class InternalSandboxHeartbeatController {

    private final OjProperties ojProperties;
    private final SparkSandboxNonceCache nonceCache;
    private final SandboxHeartbeatService sandboxHeartbeatService;
    private final ObjectMapper objectMapper;

    @Operation(summary = "SparkSandbox 主动心跳（加密信封）")
    @PostMapping("/v1/internal/oj/sandbox/heartbeat")
    public ResponseEntity<Void> heartbeat(
            @RequestHeader(value = SparkSandboxSigning.HEADER_TIMESTAMP, required = false) String timestamp,
            @RequestHeader(value = SparkSandboxSigning.HEADER_NONCE, required = false) String nonce,
            @RequestHeader(value = SparkSandboxSigning.HEADER_SIGNATURE, required = false) String signature,
            @RequestBody JsonNode envelope) {
        String secret = requireSecret();
        String ts = timestamp == null ? "" : timestamp.trim();
        verifyHmac(secret, ts, nonce, signature);
        JsonNode payload = decryptEnvelope(secret, envelope, ts);
        sandboxHeartbeatService.upsertFromHeartbeat(payload);
        return ResponseEntity.noContent().build();
    }

    private String requireSecret() {
        String secret = ojProperties.getJudge().getDefaultSigningSecret();
        if (!StringUtils.hasText(secret)) {
            throw new BizException(401, "signing secret not configured");
        }
        return secret.trim();
    }

    private void verifyHmac(String secret, String timestamp, String nonce, String signature) {
        if (!StringUtils.hasText(timestamp) || !StringUtils.hasText(nonce) || !StringUtils.hasText(signature)) {
            throw new BizException(401, "missing signature headers");
        }
        long skew = ojProperties.getJudge().getAuthSkewSeconds();
        long ts;
        try {
            ts = Long.parseLong(timestamp);
        } catch (NumberFormatException ex) {
            throw new BizException(401, "invalid timestamp");
        }
        long now = System.currentTimeMillis() / 1000L;
        if (Math.abs(now - ts) > skew) {
            throw new BizException(401, "timestamp skew too large");
        }
        if (!SparkSandboxSigning.verify(secret, timestamp, nonce.trim(), signature)) {
            throw new BizException(401, "invalid signature");
        }
        if (!nonceCache.accept(nonce.trim())) {
            throw new BizException(401, "nonce replay or invalid");
        }
    }

    private JsonNode decryptEnvelope(String secret, JsonNode envelope, String timestamp) {
        try {
            return SparkSandboxHeartbeatCrypto.decryptToJson(secret, envelope, timestamp, objectMapper);
        } catch (IllegalArgumentException ex) {
            throw new BizException(400, ex.getMessage());
        }
    }
}
