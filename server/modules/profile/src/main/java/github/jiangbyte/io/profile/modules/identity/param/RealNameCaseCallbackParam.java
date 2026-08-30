package github.jiangbyte.io.profile.modules.identity.param;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 第三方实人认证回调参数。
 *
 * Author: Charlie
 */
@Schema(description = "第三方实人认证回调参数。")
@Data
public class RealNameCaseCallbackParam {

    @NotBlank
    @Schema(description = "caseId")
    private String caseId;
    @Schema(description = "第三方业务订单号")
    private String providerOrderNo;
    @Schema(description = "是否成功：1 成功 / 0 失败")
    private Boolean success;
    @Schema(description = "提示信息")
    private String message;
}
