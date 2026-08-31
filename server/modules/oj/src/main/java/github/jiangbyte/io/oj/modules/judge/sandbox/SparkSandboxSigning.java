package github.jiangbyte.io.oj.modules.judge.sandbox;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.HexFormat;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

/**
 * SparkSandbox HMAC：canonical 为 {@code timestamp + "\\n" + nonce}。
 *
 * Author: Charlie
 */
public final class SparkSandboxSigning {

    public static final String HEADER_TIMESTAMP = "X-Spark-Timestamp";
    public static final String HEADER_NONCE = "X-Spark-Nonce";
    public static final String HEADER_SIGNATURE = "X-Spark-Signature";

    private SparkSandboxSigning() {
    }

    public static String canonical(String timestamp, String nonce) {
        return timestamp + "\n" + nonce;
    }

    public static String hmacSha256Hex(String secret, String payload) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            return HexFormat.of().formatHex(mac.doFinal(payload.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception ex) {
            throw new IllegalStateException("HMAC 计算失败", ex);
        }
    }

    /** 恒定时间比较签名（期望为小写 hex）。 */
    public static boolean verify(String secret, String timestamp, String nonce, String signature) {
        if (secret == null || secret.isBlank() || signature == null || signature.isBlank()) {
            return false;
        }
        String expected = hmacSha256Hex(secret, canonical(timestamp, nonce));
        byte[] a = expected.getBytes(StandardCharsets.UTF_8);
        byte[] b = signature.strip().toLowerCase().getBytes(StandardCharsets.UTF_8);
        return MessageDigest.isEqual(a, b);
    }
}
