package github.jiangbyte.io.auth.modules.oauth.param;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 管理端强制解绑三方账号。
 *
 * Author: Charlie
 */
@Schema(description = "管理端强制解绑三方账号。")
@Data
public class AdminOauthUnbindParam {
    @NotBlank
    @Schema(description = "账户ID")
    private String accountId;
    @NotBlank
    @Schema(description = "第三方服务提供方")
    private String provider;
}
