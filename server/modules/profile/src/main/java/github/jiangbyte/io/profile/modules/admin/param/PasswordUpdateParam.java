package github.jiangbyte.io.profile.modules.admin.param;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 管理端修改密码请求：新密码必填；旧密码或 OTP 按系统改密校验方式选填。
 *
 * Author: Charlie
 */
@Schema(description = "管理端修改密码请求：新密码必填；旧密码或 OTP 按系统改密校验方式选填。")
@Data
public class PasswordUpdateParam {
    @NotBlank
    @Schema(description = "passwordKeyId")
    private String passwordKeyId;
    @Schema(description = "PASSWORD_CHANGE_VERIFY_METHOD 为 OLD_PASSWORD 时必填。")
    /**
     * PASSWORD_CHANGE_VERIFY_METHOD 为 OLD_PASSWORD 时必填。
     */
    private String oldPassword;
    @NotBlank
    @Schema(description = "newPassword")
    private String newPassword;
    @Schema(description = "校验方式为 EMAIL_CODE 或 PHONE_CODE 时必填。")
    /**
     * 校验方式为 EMAIL_CODE 或 PHONE_CODE 时必填。
     */
    private String otpCode;
}
