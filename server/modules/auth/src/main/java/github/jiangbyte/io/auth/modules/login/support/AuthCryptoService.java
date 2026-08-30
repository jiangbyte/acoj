package github.jiangbyte.io.auth.modules.login.support;

import github.jiangbyte.io.auth.modules.login.result.CaptchaResult;
import github.jiangbyte.io.auth.modules.login.result.PasswordKeyResult;

import java.time.Duration;

/**
 * 认证加解密与短期凭证存储：验证码、RSA 密码密钥、重置令牌与 OTP（基于 Redis）。
 *
 * Author: Charlie
 */
public interface AuthCryptoService {

    /** 创建图形验证码并缓存哈希值。 */
    CaptchaResult createCaptcha(String format);

    /** 校验并消费验证码（一次性）。 */
    void verifyCaptcha(String captchaId, String captchaValue);

    /** 生成 RSA 密钥对，私钥缓存于 Redis，公钥返回客户端。 */
    PasswordKeyResult createPasswordKey();

    /** 用一次性私钥解密单个密文密码。 */
    String decryptPassword(String passwordKeyId, String encryptedPassword);

    /** 用同一私钥批量解密多个密文密码（消费密钥）。 */
    String[] decryptPasswords(String passwordKeyId, String... encryptedPasswords);

    /** 缓存密码重置令牌与账号 ID 的映射。 */
    void storeResetToken(String token, String accountId, Duration ttl);

    /** 消费重置令牌并返回账号 ID；无效或过期返回 null。 */
    String consumeResetToken(String token);

    /**
     * 缓存登录 OTP。
     *
     * @param accountType 账号类型名
     * @param channel     EMAIL 或 PHONE
     * @param target      规范化后的邮箱/手机
     * @param code        验证码明文
     * @param ttl         有效期
     */
    void storeLoginOtp(String accountType, String channel, String target, String code, Duration ttl);

    /**
     * 校验并消费登录 OTP。
     *
     * @param accountType 账号类型名
     * @param channel     EMAIL 或 PHONE
     * @param target      规范化后的邮箱/手机
     * @param code        用户提交的验证码
     */
    boolean consumeLoginOtp(String accountType, String channel, String target, String code);

    /**
     * 缓存改密 OTP（按账号 ID 绑定）。
     *
     * @param accountType 账号类型名
     * @param channel     EMAIL 或 PHONE
     * @param accountId   账号 ID
     * @param code        验证码明文
     * @param ttl         有效期
     */
    void storeChangePasswordOtp(String accountType, String channel, String accountId, String code, Duration ttl);

    /**
     * 校验并消费改密 OTP。
     *
     * @param accountType 账号类型名
     * @param channel     EMAIL 或 PHONE
     * @param accountId   账号 ID
     * @param code        用户提交的验证码
     */
    boolean consumeChangePasswordOtp(String accountType, String channel, String accountId, String code);

    /**
     * 缓存注册 OTP。
     *
     * @param channel EMAIL 或 PHONE
     * @param target  规范化后的邮箱/手机
     * @param code    验证码明文
     * @param ttl     有效期
     */
    void storeRegisterOtp(String channel, String target, String code, Duration ttl);

    /**
     * 校验并消费注册 OTP。
     *
     * @param channel EMAIL 或 PHONE
     * @param target  规范化后的邮箱/手机
     * @param code    用户提交的验证码
     */
    boolean consumeRegisterOtp(String channel, String target, String code);

    /**
     * 缓存绑定 OTP（按账号 ID + 新目标）。
     *
     * @param accountType 账号类型名
     * @param channel     EMAIL 或 PHONE
     * @param accountId   账号 ID
     * @param target      规范化后的新邮箱/手机
     * @param code        验证码明文
     * @param ttl         有效期
     */
    void storeBindOtp(String accountType, String channel, String accountId, String target, String code, Duration ttl);

    /**
     * 校验并消费绑定 OTP。
     *
     * @param accountType 账号类型名
     * @param channel     EMAIL 或 PHONE
     * @param accountId   账号 ID
     * @param target      规范化后的新邮箱/手机
     * @param code        用户提交的验证码
     */
    boolean consumeBindOtp(String accountType, String channel, String accountId, String target, String code);

    /**
     * 缓存手机找回密码 OTP。
     */
    void storeResetPasswordOtp(String accountType, String phone, String code, Duration ttl);

    /**
     * 校验并消费手机找回密码 OTP。
     */
    boolean consumeResetPasswordOtp(String accountType, String phone, String code);

    /**
     * 尝试标记密码即将过期通知已发送（24 小时内不重复）。
     *
     * @return true 表示本次可发送
     */
    boolean tryMarkPasswordExpiryNotified(String accountId);

}
