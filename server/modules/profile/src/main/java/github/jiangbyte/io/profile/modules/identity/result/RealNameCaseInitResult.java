package github.jiangbyte.io.profile.modules.identity.result;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 第三方实人认证发起结果。
 *
 * Author: Charlie
 */
@Schema(description = "第三方实人认证发起结果。")
@Data
public class RealNameCaseInitResult {
    @Schema(description = "caseId")

    private String caseId;
    @Schema(description = "第三方服务提供方")
    private String provider;
    @Schema(description = "第三方业务订单号")
    private String providerOrderNo;
    @Schema(description = "redirectUrl")
    private String redirectUrl;
}
