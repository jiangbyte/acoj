package github.jiangbyte.io.oj.modules.judge.sandbox;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Base64;
import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

/**
 * SparkSandbox 主动心跳 AES-256-GCM 信封解密。
 * <p>密钥：{@code SHA256("spark-sandbox-heartbeat-aes256-v1" || 0x00 || utf8(secret))}；
 * AAD = utf8(X-Spark-Timestamp)。
 *
 * Author: Charlie
 */
public final class SparkSandboxHeartbeatCrypto {

    public static final int ENC_VERSION = 1;
    public static final String ENC_ALG = "A256GCM";
    private static final byte[] KEY_INFO = "spark-sandbox-heartbeat-aes256-v1".getBytes(StandardCharsets.UTF_8);
    private static final int GCM_TAG_BITS = 128;

    private SparkSandboxHeartbeatCrypto() {
    }

    public static byte[] deriveKey(String secret) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            md.update(KEY_INFO);
            md.update((byte) 0);
            md.update(secret.getBytes(StandardCharsets.UTF_8));
            return md.digest();
        } catch (Exception ex) {
            throw new IllegalStateException("derive heartbeat AES key failed", ex);
        }
    }

    /**
     * 解密信封为 UTF-8 JSON 明文对象。
     *
     * @param secret    signing_secret（与沙箱相同）
     * @param envelope  {@code {v, alg, iv, ciphertext}}
     * @param timestamp {@code X-Spark-Timestamp} 原文字符串（作 AAD）
     */
    public static JsonNode decryptToJson(String secret, JsonNode envelope, String timestamp, ObjectMapper objectMapper) {
        if (envelope == null || !envelope.isObject()) {
            throw new IllegalArgumentException("heartbeat envelope must be an object");
        }
        int version = envelope.path("v").asInt(0);
        if (version != ENC_VERSION) {
            throw new IllegalArgumentException("unsupported heartbeat envelope version: " + version);
        }
        String alg = envelope.path("alg").asText("");
        if (!ENC_ALG.equals(alg)) {
            throw new IllegalArgumentException("unsupported heartbeat alg: " + alg);
        }
        String ivB64 = envelope.path("iv").asText("");
        String ctB64 = envelope.path("ciphertext").asText("");
        if (ivB64.isBlank() || ctB64.isBlank()) {
            throw new IllegalArgumentException("heartbeat envelope missing iv/ciphertext");
        }

        byte[] iv = Base64.getDecoder().decode(ivB64);
        byte[] ciphertext = Base64.getDecoder().decode(ctB64);
        byte[] key = deriveKey(secret);
        byte[] aad = timestamp == null ? new byte[0] : timestamp.getBytes(StandardCharsets.UTF_8);

        try {
            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            cipher.init(Cipher.DECRYPT_MODE, new SecretKeySpec(key, "AES"), new GCMParameterSpec(GCM_TAG_BITS, iv));
            cipher.updateAAD(aad);
            byte[] plaintext = cipher.doFinal(ciphertext);
            JsonNode root = objectMapper.readTree(plaintext);
            if (root == null || !root.isObject()) {
                throw new IllegalArgumentException("decrypted heartbeat payload must be an object");
            }
            return root;
        } catch (IllegalArgumentException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new IllegalArgumentException("heartbeat decrypt failed: " + ex.getMessage(), ex);
        }
    }
}
