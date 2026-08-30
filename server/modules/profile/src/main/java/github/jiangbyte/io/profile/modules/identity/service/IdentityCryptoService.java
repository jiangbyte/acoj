package github.jiangbyte.io.profile.modules.identity.service;

import github.jiangbyte.io.common.core.crypto.FernetCodec;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.HexFormat;
import java.util.Locale;

/**
 * 实名敏感字段加解密、哈希与脱敏工具。
 *
 * Author: Charlie
 */
@Slf4j
@Service
public class IdentityCryptoService {

    @Value("${hei.profile.identity.crypto-key:${hei.config.crypto-key:${APP__CONFIG_CRYPTO_KEY:}}}")
    private String cryptoKey;

    private volatile FernetCodec codec;

    @PostConstruct
    void init() {
        if (!StringUtils.hasText(cryptoKey)) {
            log.warn("hei.profile.identity.crypto-key is empty; identity encryption disabled");
            return;
        }
        try {
            codec = new FernetCodec(cryptoKey.trim());
            log.info("Profile identity Fernet crypto enabled");
        } catch (Exception ex) {
            log.error("Invalid hei.profile.identity.crypto-key: {}", ex.getMessage());
        }
    }

    public String encrypt(String plaintext) {
        requireCodec();
        if (!StringUtils.hasText(plaintext)) {
            return null;
        }
        return codec.encrypt(plaintext.trim());
    }

    public String decrypt(String ciphertext) {
        requireCodec();
        if (!StringUtils.hasText(ciphertext)) {
            return null;
        }
        String plain = codec.tryDecrypt(ciphertext.trim());
        if (plain == null) {
            throw new IllegalStateException("Failed to decrypt identity field");
        }
        return plain;
    }

    public String hashDocumentNo(String documentType, String documentNo) {
        if (!StringUtils.hasText(documentNo)) {
            return null;
        }
        String normalizedType = documentType == null ? "" : documentType.trim().toUpperCase(Locale.ROOT);
        String normalizedNo = documentNo.trim().toUpperCase(Locale.ROOT);
        return sha256Hex(normalizedType + "|" + normalizedNo);
    }

    public String maskRealName(String realName) {
        if (!StringUtils.hasText(realName)) {
            return null;
        }
        String trimmed = realName.trim();
        if (trimmed.length() <= 1) {
            return "*";
        }
        return trimmed.charAt(0) + "*".repeat(trimmed.length() - 1);
    }

    public String maskDocumentNo(String documentNo) {
        if (!StringUtils.hasText(documentNo)) {
            return null;
        }
        String trimmed = documentNo.trim();
        if (trimmed.length() <= 7) {
            return "*".repeat(trimmed.length());
        }
        int keepPrefix = Math.min(3, trimmed.length());
        int keepSuffix = Math.min(4, trimmed.length() - keepPrefix);
        return trimmed.substring(0, keepPrefix)
                + "*".repeat(trimmed.length() - keepPrefix - keepSuffix)
                + trimmed.substring(trimmed.length() - keepSuffix);
    }

    public boolean enabled() {
        return codec != null;
    }

    private void requireCodec() {
        if (codec == null) {
            throw new IllegalStateException(
                    "Identity crypto is not configured; set hei.profile.identity.crypto-key");
        }
    }

    private static String sha256Hex(String input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(input.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (Exception ex) {
            throw new IllegalStateException("SHA-256 hash failed", ex);
        }
    }
}
