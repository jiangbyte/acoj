package github.jiangbyte.io.auth.login;

/**
 * 跨模块密码传输解密：按一次性 RSA {@code password_key_id} 解密前端密文密码。
 * 供登录、改密等需要还原明文再做哈希/校验的调用方使用。
 *
 * Author: Charlie
 */
public interface PasswordCryptoApi {

    /** 使用一次性私钥解密密码；无 keyId 时原样返回。 */
    String decryptPassword(String passwordKeyId, String password);
}
