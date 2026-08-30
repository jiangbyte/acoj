package github.jiangbyte.io.sys.modules.config.support;

import github.jiangbyte.io.common.core.crypto.FernetCodec;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

/**
 * 配置加解密服务：敏感配置项加解密。
 *
 * Author: Charlie
 */
@Component
public class ConfigCryptoService {

    private static final Logger log = LoggerFactory.getLogger(ConfigCryptoService.class);

    @Value("${hei.config.crypto-key:${APP__CONFIG_CRYPTO_KEY:}}")
    private String cryptoKey;

    private volatile FernetCodec codec;

    @PostConstruct
    void init() {
        // 无配置密钥初始化 Fernet
        if (!StringUtils.hasText(cryptoKey)) {
            log.warn("hei.config.crypto-key / APP__CONFIG_CRYPTO_KEY is empty; "
                    + "startup will fail if sys_config contains Fernet-encrypted values");
            return;
        }
        try {
            codec = new FernetCodec(cryptoKey.trim());
            log.info("Config Fernet crypto enabled");
        } catch (Exception ex) {
            log.error("Invalid hei.config.crypto-key: {}", ex.getMessage());
        }
    }

    public String decryptForRead(String value) {
        if (!StringUtils.hasText(value)) {
            return value;
        }
        if (!FernetCodec.looksLikeToken(value)) {
            return value;
        }
        if (codec == null) {
            throw new IllegalStateException(
                    "Encrypted config value present but hei.config.crypto-key / APP__CONFIG_CRYPTO_KEY is empty");
        }
        String plain = codec.tryDecrypt(value);
        if (plain == null) {
            throw new IllegalStateException("Failed to decrypt config value with current crypto-key");
        }
        return plain;
    }

    public String encryptForWrite(String configKey, String value, boolean sensitive) {
        // 非敏感或未启用加密则原样返回
        if (!sensitive || !StringUtils.hasText(value) || codec == null) {
            return value;
        }
        // 已是当前密钥下的密文则跳过
        if (FernetCodec.looksLikeToken(value) && codec.tryDecrypt(value) != null) {
            return value;
        }
        return codec.encrypt(value);
    }

    public boolean enabled() {
        return codec != null;
    }
}
