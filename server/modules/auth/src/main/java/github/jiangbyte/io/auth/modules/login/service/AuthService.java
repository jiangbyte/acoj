package github.jiangbyte.io.auth.modules.login.service;

import github.jiangbyte.io.auth.modules.login.param.CancelAccountParam;
import github.jiangbyte.io.auth.modules.login.param.ForgotPasswordByPhoneParam;
import github.jiangbyte.io.auth.modules.login.param.ForgotPasswordParam;
import github.jiangbyte.io.auth.modules.login.param.LoginParam;
import github.jiangbyte.io.auth.modules.login.param.RegisterParam;
import github.jiangbyte.io.auth.modules.login.param.ResetPasswordByPhoneParam;
import github.jiangbyte.io.auth.modules.login.param.ResetPasswordParam;
import github.jiangbyte.io.auth.modules.login.param.SendLoginCodeParam;
import github.jiangbyte.io.auth.modules.login.result.AuthOptionsResult;
import github.jiangbyte.io.auth.modules.login.result.CaptchaResult;
import github.jiangbyte.io.auth.modules.login.result.CurrentUserResult;
import github.jiangbyte.io.auth.modules.login.result.LoginResult;
import github.jiangbyte.io.auth.modules.login.result.PasswordKeyResult;
import github.jiangbyte.io.auth.modules.login.result.RegisterResult;
import github.jiangbyte.io.common.core.enums.AccountType;

/**
 * 认证领域服务：验证码、登录注册、会话刷新、找回密码及个人账号安全变更。
 *
 * Author: Charlie
 */
public interface AuthService {

    /** 生成图形验证码（format 支持 svg/png）。 */
    CaptchaResult captcha(String format);

    /** 生成一次性 RSA 密码传输密钥对。 */
    PasswordKeyResult passwordKey();

    /** 读取指定账号类型的登录页公开配置。 */
    AuthOptionsResult authOptions(AccountType accountType);

    /** 向邮箱或手机发送登录 OTP。 */
    void sendLoginCode(SendLoginCodeParam request, AccountType accountType);

    /** 向邮箱或手机发送门户注册 OTP（图形验证码通过后）。 */
    void sendRegisterCode(SendLoginCodeParam request);

    /** 密码或 OTP 登录并签发会话。 */
    LoginResult login(LoginParam request);

    /** 三方登录成功后签发会话。 */
    LoginResult issueLoginForAccount(
            github.jiangbyte.io.iam.account.AccountInfo account,
            AccountType accountType,
            String loginLabel);

    /** 门户用户自助注册。 */
    RegisterResult registerPortal(RegisterParam request);

    /** 发送忘记密码重置邮件（账号不存在时静默返回）。 */
    void forgotPassword(ForgotPasswordParam request, AccountType accountType);

    /** 向绑定手机发送找回密码 OTP（账号不存在时静默返回）。 */
    void forgotPasswordByPhone(ForgotPasswordByPhoneParam request, AccountType accountType);

    /** 使用重置令牌设置新密码。 */
    void resetPassword(ResetPasswordParam request, AccountType accountType);

    /** 使用手机 OTP 设置新密码。 */
    void resetPasswordByPhone(ResetPasswordByPhoneParam request, AccountType accountType);

    /** 获取当前登录用户摘要。 */
    CurrentUserResult currentUser();

    /** 注销当前会话。 */
    void logout();

    /** 续期当前 Token 并返回最新会话信息。 */
    LoginResult refreshSession();

    /** 注销（停用）当前账号并退出登录。 */
    void cancelAccount(CancelAccountParam request);

    /** 按配置向绑定邮箱/手机发送改密验证码。 */
    void sendChangePasswordCode();

    /**
     * 修改当前用户密码（旧密码或 OTP 校验，取决于系统配置）。
     *
     * @param passwordKeyId 密码传输密钥 ID
     * @param oldPassword   加密后的旧密码（旧密码校验模式时使用）
     * @param newPassword   加密后的新密码
     * @param otpCode       邮箱/手机验证码（验证码模式时使用）
     */
    void updateCurrentPassword(String passwordKeyId, String oldPassword, String newPassword, String otpCode);

    /** 向待绑定邮箱发送验证码。 */
    void sendBindEmailCode(String target);

    /** 向待绑定手机发送验证码。 */
    void sendBindPhoneCode(String target);

    /**
     * 更新当前用户手机号及是否允许手机登录。
     *
     * @param passwordKeyId     密码传输密钥 ID
     * @param password          加密后的登录密码（用于身份确认）
     * @param phone             新手机号
     * @param phoneLoginEnabled 是否启用手机登录身份
     * @param otpCode           绑定 OTP；解绑时可空
     */
    void updateCurrentPhone(String passwordKeyId, String password, String phone, boolean phoneLoginEnabled, String otpCode);

    /**
     * 更新当前用户邮箱及是否允许邮箱登录。
     *
     * @param passwordKeyId     密码传输密钥 ID
     * @param password          加密后的登录密码（用于身份确认）
     * @param email             新邮箱
     * @param emailLoginEnabled 是否启用邮箱登录身份
     * @param otpCode           绑定 OTP；解绑时可空
     */
    void updateCurrentEmail(String passwordKeyId, String password, String email, boolean emailLoginEnabled, String otpCode);
}
