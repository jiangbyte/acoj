package github.jiangbyte.io.auth.modules.login.provider;

import github.jiangbyte.io.auth.login.PasswordCryptoApi;
import github.jiangbyte.io.auth.modules.login.support.AuthCryptoService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

/**
 * 跨模块 {@link PasswordCryptoApi} 适配器：在具备 keyId 时用 RSA 私钥解密传输中的密码。
 *
 * Author: Charlie
 */
@Service
@RequiredArgsConstructor
public class PasswordCryptoApiProvider implements PasswordCryptoApi {

    private final AuthCryptoService authCryptoService;

    @Override
    public String decryptPassword(String passwordKeyId, String password) {
        // 无密钥或空密文时原样返回，兼容明文场景
        if (!StringUtils.hasText(passwordKeyId) || !StringUtils.hasText(password)) {
            return password;
        }
        return authCryptoService.decryptPassword(passwordKeyId, password);
    }
}
