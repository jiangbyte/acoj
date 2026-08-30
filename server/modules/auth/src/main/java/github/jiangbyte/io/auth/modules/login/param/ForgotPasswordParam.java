package github.jiangbyte.io.auth.modules.login.param;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 忘记密码请求参数：校验验证码后，向绑定邮箱发送重置链接。
 *
 * Author: Charlie
 */
@Schema(description = "忘记密码请求参数：校验验证码后，向绑定邮箱发送重置链接。")
@Data
public class ForgotPasswordParam {
    @NotBlank
    @Schema(description = "email")
    private String email;
    @NotBlank
    @Schema(description = "captchaId")
    private String captchaId;
    @NotBlank
    @Schema(description = "captchaValue")
    private String captchaValue;
}
