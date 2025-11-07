package io.charlie.web.oj.modular.sys.auth.param;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * @author Charlie Zhang
 * @version v1.0
 * @date 23/07/2025
 * @description 密码更改参数
 */
@Data
public class PasswordChangeParam {
    @Schema(description = "旧密码")
    @NotBlank
    private String oldPassword;

    @Schema(description = "新密码")
    @NotBlank
    private String newPassword;

    @Schema(description = "确认密码")
    @NotBlank
    private String confirmPassword;
}
