package github.jiangbyte.io.profile.modules.identity.param;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 发起第三方实人认证。
 *
 * Author: Charlie
 */
@Schema(description = "发起第三方实人认证。")
@Data
public class RealNameCaseInitThirdPartyParam {
    @Schema(description = "业务类型")

    private String businessType;
    @NotBlank
    @Schema(description = "证件类型：ID_CARD/PASSPORT 等")
    private String documentType;
    @NotBlank
    @Schema(description = "realName")
    private String realName;
    @NotBlank
    @Schema(description = "documentNo")
    private String documentNo;
    @Schema(description = "第三方服务提供方")
    private String provider;
}
