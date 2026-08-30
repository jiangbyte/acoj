package github.jiangbyte.io.auth.modules.login.param;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 通过手机找回密码：校验图形验证码后向绑定手机发送重置 OTP。
 *
 * Author: Charlie
 */
@Schema(description = "通过手机找回密码：校验图形验证码后向绑定手机发送重置 OTP。")
@Data
public class ForgotPasswordByPhoneParam {
    @NotBlank
    @Schema(description = "phone")
    private String phone;
    @NotBlank
    @Schema(description = "captchaId")
    private String captchaId;
    @NotBlank
    @Schema(description = "captchaValue")
    private String captchaValue;
}
