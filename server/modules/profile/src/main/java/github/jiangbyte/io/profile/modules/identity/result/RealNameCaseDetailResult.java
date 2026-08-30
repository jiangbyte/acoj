package github.jiangbyte.io.profile.modules.identity.result;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.ArrayList;
import java.util.List;

/**
 * 实名业务工单详情（含附件）。
 *
 * Author: Charlie
 */
@Schema(description = "实名业务工单详情（含附件）。")
@Data
@EqualsAndHashCode(callSuper = true)
public class RealNameCaseDetailResult extends RealNameCaseSummaryResult {
    @Schema(description = "第三方服务提供方")

    private String provider;
    @Schema(description = "第三方业务订单号")
    private String providerOrderNo;
    @Schema(description = "submitterId")
    private String submitterId;
    @Schema(description = "reviewerId")
    private String reviewerId;
    @Schema(description = "attachments")
    private List<RealNameCaseAttachmentResult> attachments = new ArrayList<>();
}
