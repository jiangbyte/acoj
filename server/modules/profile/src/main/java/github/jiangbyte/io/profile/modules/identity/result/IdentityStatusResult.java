package github.jiangbyte.io.profile.modules.identity.result;

import io.swagger.v3.oas.annotations.media.Schema;
import github.jiangbyte.io.common.core.jackson.Sensitive;
import github.jiangbyte.io.common.core.jackson.SensitiveStrategy;
import lombok.Data;

import java.time.OffsetDateTime;

/**
 * 当前账号实名认证快照与进行中工单摘要。
 *
 * Author: Charlie
 */
@Schema(description = "当前账号实名认证快照与进行中工单摘要。")
@Data
public class IdentityStatusResult {
    @Schema(description = "状态")

    private String status;
    @Schema(description = "证件类型：ID_CARD/PASSPORT 等")
    private String documentType;
    @Sensitive(strategy = SensitiveStrategy.KEEP, keepPrefix = 1, keepSuffix = 0)
    @Schema(description = "realNameMasked")
    private String realNameMasked;
    @Sensitive(strategy = SensitiveStrategy.KEEP, keepPrefix = 3, keepSuffix = 4)
    @Schema(description = "documentNoMasked")
    private String documentNoMasked;
    @Schema(description = "认证通道：THIRD_PARTY（三方）/ MANUAL（人工）")
    private String verifyChannel;
    @Schema(description = "第三方服务提供方")
    private String provider;
    @Schema(description = "verifiedAt")
    private OffsetDateTime verifiedAt;
    @Schema(description = "revokedAt")
    private OffsetDateTime revokedAt;
    @Schema(description = "pendingCase")
    private RealNameCaseSummaryResult pendingCase;
}
