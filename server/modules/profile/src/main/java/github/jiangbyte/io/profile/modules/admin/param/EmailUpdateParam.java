package github.jiangbyte.io.profile.modules.admin.param;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 管理端更新邮箱请求：需密码传输密钥与登录密码确认，并可选启用邮箱登录。
 *
 * Author: Charlie
 */
@Schema(description = "管理端更新邮箱请求：需密码传输密钥与登录密码确认，并可选启用邮箱登录。")
@Data
public class EmailUpdateParam {
    @NotBlank
    @Schema(description = "passwordKeyId")
    private String passwordKeyId;
    @NotBlank
    @Schema(description = "password")
    private String password;
    @Schema(description = "email")
    private String email;
    @Schema(description = "是否启用邮箱登录")
    private Boolean emailLoginEnabled = false;
    @Schema(description = "绑定/换绑时必填的邮箱 OTP")
    /** 绑定/换绑时必填的邮箱 OTP */
    private String otpCode;
}
