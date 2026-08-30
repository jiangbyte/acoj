package github.jiangbyte.io.auth.modules.login.param;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 发送登录 OTP 验证码的请求参数（邮箱或手机）。
 *
 * Author: Charlie
 */
@Schema(description = "发送登录 OTP 验证码的请求参数（邮箱或手机）。")
@Data
public class SendLoginCodeParam {

    @NotBlank
    @Schema(description = "target")
    private String target;

    @Schema(description = "EMAIL 或 PHONE")
    /** EMAIL 或 PHONE */
    @NotBlank
    private String channel;

    @NotBlank
    @Schema(description = "captchaId")
    private String captchaId;

    @NotBlank
    @Schema(description = "captchaValue")
    private String captchaValue;
}
