package github.jiangbyte.io.auth.modules.login.param;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 通过手机 OTP 重置密码。
 *
 * Author: Charlie
 */
@Schema(description = "通过手机 OTP 重置密码。")
@Data
public class ResetPasswordByPhoneParam {
    @NotBlank
    @Schema(description = "phone")
    private String phone;
    @NotBlank
    @Schema(description = "otpCode")
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
