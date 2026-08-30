package github.jiangbyte.io.profile.modules.identity.param;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 管理端撤销账号实名认证。
 *
 * Author: Charlie
 */
@Schema(description = "管理端撤销账号实名认证。")
@Data
public class IdentityRevokeParam {

    @NotBlank
    @Schema(description = "账户ID")
    private String accountId;
    @Schema(description = "备注说明")
    private String remark;
}
