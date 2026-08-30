package github.jiangbyte.io.auth.modules.login.param;

import io.swagger.v3.oas.annotations.media.Schema;
import github.jiangbyte.io.common.core.enums.AccountType;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 登录请求参数：支持账号/邮箱/手机 + 密码或 OTP，并携带图形验证码。
 *
 * Author: Charlie
 */
@Schema(description = "登录请求参数：支持账号/邮箱/手机 + 密码或 OTP，并携带图形验证码。")
@Data
public class LoginParam {

    @NotBlank
    @Schema(description = "登录账号/用户名")
    private String account;

    @Schema(description = "loginMode 为 PASSWORD 时必填。")
    /**
     * loginMode 为 PASSWORD 时必填。
     */
    private String password;

    @Schema(description = "loginMode 为 PASSWORD 时必填。")
    /** loginMode 为 PASSWORD 时必填。 */
    private String passwordKeyId;

    @NotBlank
    @Schema(description = "captchaId")
    private String captchaId;

    @NotBlank
    @Schema(description = "captchaValue")
    private String captchaValue;
    @Schema(description = "identityType")

    private String identityType = "ACCOUNT";

    @Schema(description = "PASSWORD 或 OTP")
    /**
     * PASSWORD 或 OTP
     */
    private String loginMode = "PASSWORD";
    @Schema(description = "otpCode")

    private String otpCode;
    @Schema(description = "rememberMe")

    private Boolean rememberMe = true;

    @Schema(description = "由控制器设置；非强类型接口的客户端载荷字段。")
    /** 由控制器设置；非强类型接口的客户端载荷字段。 */
    private AccountType accountType = AccountType.ADMIN;
}
