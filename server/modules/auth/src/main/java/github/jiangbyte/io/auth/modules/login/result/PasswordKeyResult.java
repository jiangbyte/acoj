package github.jiangbyte.io.auth.modules.login.result;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 密码传输加密公钥响应：一次性 keyId 与 RSA 公钥（Base64）。
 *
 * Author: Charlie
 */
@Schema(description = "密码传输加密公钥响应：一次性 keyId 与 RSA 公钥（Base64）。")
@Data
public class PasswordKeyResult {
    @Schema(description = "keyId")
    private String keyId;
    @Schema(description = "publicKey")
    private String publicKey;
}
