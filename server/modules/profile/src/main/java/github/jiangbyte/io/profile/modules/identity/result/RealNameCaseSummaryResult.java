package github.jiangbyte.io.profile.modules.identity.result;

import io.swagger.v3.oas.annotations.media.Schema;
import github.jiangbyte.io.common.core.jackson.Sensitive;
import github.jiangbyte.io.common.core.jackson.SensitiveStrategy;
import lombok.Data;

import java.time.OffsetDateTime;

/**
 * 实名业务工单摘要（列表/状态联查）。
 *
 * Author: Charlie
 */
@Schema(description = "实名业务工单摘要（列表/状态联查）。")
@Data
public class RealNameCaseSummaryResult {
    @Schema(description = "caseId")

    private String caseId;
    @Schema(description = "账户ID")
    private String accountId;
    @Schema(description = "业务类型")
    private String businessType;
    @Schema(description = "认证通道：THIRD_PARTY（三方）/ MANUAL（人工）")
    private String verifyChannel;
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
    @Schema(description = "创建时间")
    private OffsetDateTime createdAt;
    @Schema(description = "reviewedAt")
    private OffsetDateTime reviewedAt;
    @Schema(description = "rejectReason")
    private String rejectReason;
}
