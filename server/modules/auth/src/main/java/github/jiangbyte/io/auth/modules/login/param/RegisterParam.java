package github.jiangbyte.io.auth.modules.login.param;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 门户用户注册请求：通道 ACCOUNT / EMAIL / PHONE，均需密码与图形验证码；邮箱/手机通道另需 OTP。
 *
 * Author: Charlie
 */
@Schema(description = "门户用户注册请求：通道 ACCOUNT / EMAIL / PHONE，均需密码与图形验证码；邮箱/手机通道另需 OTP。")
@Data
public class RegisterParam {

    @Schema(description = "ACCOUNT | EMAIL | PHONE")
    /** ACCOUNT | EMAIL | PHONE */
    @NotBlank
    private String registerChannel;

    @Size(min = 3, max = 64)
    @Pattern(regexp = "^[a-zA-Z0-9_]{3,64}$", message = "账号仅允许字母、数字和下划线，长度 3-64")
    @Schema(description = "登录账号/用户名")
    private String account;

    @Size(max = 128)
    @Schema(description = "email")
    private String email;

    @Size(max = 32)
    @Schema(description = "phone")
    private String phone;

    @Schema(description = "邮箱/手机通道注册 OTP")
    /** 邮箱/手机通道注册 OTP */
    private String otpCode;

    @NotBlank
    @Schema(description = "password")
    private String password;

    @NotBlank
    @Schema(description = "passwordKeyId")
    private String passwordKeyId;

    @NotBlank
    @Schema(description = "captchaId")
    private String captchaId;

    @NotBlank
    @Schema(description = "captchaValue")
    private String captchaValue;
}
