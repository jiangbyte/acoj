package github.jiangbyte.io.iam.modules.account.param;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 管理端更新账号邮箱/手机号登录身份入参。
 *
 * Author: Charlie
 */
@Schema(description = "管理端更新账号邮箱/手机号登录身份入参。")
@Data
public class SysAccountUpdateLoginIdentityParam {

    @NotBlank
    @Schema(description = "账号 ID")
    private String id;

    @Schema(description = "是否启用邮箱登录")
    private Boolean emailLoginEnabled = false;

    @Schema(description = "邮箱登录标识")
    private String email;

    @Schema(description = "是否启用手机号登录")
    private Boolean phoneLoginEnabled = false;

    @Schema(description = "手机号登录标识")
    private String phone;
}
