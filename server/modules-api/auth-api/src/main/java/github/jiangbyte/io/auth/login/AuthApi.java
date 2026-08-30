package github.jiangbyte.io.auth.login;

/**
 * 跨模块认证操作门面：当前登录用户的密码/手机/邮箱变更及验证码发送。
 * HTTP 请求/响应类型位于 {@code module/auth}，由 auth 模块提供实现。
 *
 * Author: Charlie
 */
public interface AuthApi {

    /** 向当前用户发送修改密码验证码。 */
    void sendChangePasswordCode();

    /**
     * 使用旧密码与 OTP 更新当前用户密码。
     *
     * @param passwordKeyId RSA 传输密钥 id；无加密传输时可为空
     * @param oldPassword   旧密码（可能为密文）
     * @param newPassword   新密码（可能为密文）
     * @param otpCode       短信/邮箱 OTP
     */
    void updateCurrentPassword(String passwordKeyId, String oldPassword, String newPassword, String otpCode);

    /**
     * 向待绑定邮箱发送验证码。
     *
     * @param target 新邮箱
     */
    void sendBindEmailCode(String target);

    /**
     * 向待绑定手机发送验证码。
     *
     * @param target 新手机号
     */
    void sendBindPhoneCode(String target);

    /**
     * 校验密码与（绑定时）OTP 后更新当前用户手机号，并可开启手机登录。
     *
     * @param passwordKeyId     RSA 传输密钥 id
     * @param password          当前密码（可能为密文）
     * @param phone             新手机号
     * @param phoneLoginEnabled 是否启用手机号登录身份
     * @param otpCode           绑定 OTP；解绑时可空
     */
    void updateCurrentPhone(String passwordKeyId, String password, String phone, boolean phoneLoginEnabled, String otpCode);

    /**
     * 校验密码与（绑定时）OTP 后更新当前用户邮箱，并可开启邮箱登录。
     *
     * @param passwordKeyId     RSA 传输密钥 id
     * @param password          当前密码（可能为密文）
     * @param email             新邮箱
     * @param emailLoginEnabled 是否启用邮箱登录身份
     * @param otpCode           绑定 OTP；解绑时可空
     */
    void updateCurrentEmail(String passwordKeyId, String password, String email, boolean emailLoginEnabled, String otpCode);
}
