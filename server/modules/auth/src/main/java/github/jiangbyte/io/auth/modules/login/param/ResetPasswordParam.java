package github.jiangbyte.io.auth.modules.login.param;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 通过邮件重置令牌设置新密码的请求参数。
 *
 * Author: Charlie
 */
@Schema(description = "通过邮件重置令牌设置新密码的请求参数。")
@Data
public class ResetPasswordParam {
    @Schema(description = "可选；重置基于 token。")
    /**
     * 可选；重置基于 token。
     */
    private String email;
    @NotBlank
    @Schema(description = "token")
    private String token;
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
