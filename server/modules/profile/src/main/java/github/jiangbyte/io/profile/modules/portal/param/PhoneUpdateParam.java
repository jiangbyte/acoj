package github.jiangbyte.io.profile.modules.portal.param;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 门户端更新手机号请求：需密码传输密钥与登录密码确认，并可选启用手机登录。
 *
 * Author: Charlie
 */
@Schema(description = "门户端更新手机号请求：需密码传输密钥与登录密码确认，并可选启用手机登录。")
@Data
public class PhoneUpdateParam {
    @NotBlank
    @Schema(description = "passwordKeyId")
    private String passwordKeyId;
    @NotBlank
    @Schema(description = "password")
    private String password;
    @Schema(description = "phone")
    private String phone;
    @Schema(description = "是否启用手机号登录")
    private Boolean phoneLoginEnabled = false;
    @Schema(description = "绑定/换绑时必填的手机 OTP")
    /** 绑定/换绑时必填的手机 OTP */
    private String otpCode;
}
