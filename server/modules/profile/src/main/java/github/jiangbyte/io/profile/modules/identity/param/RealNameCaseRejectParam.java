package github.jiangbyte.io.profile.modules.identity.param;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 管理端驳回实名认证工单。
 *
 * Author: Charlie
 */
@Schema(description = "管理端驳回实名认证工单。")
@Data
public class RealNameCaseRejectParam {

    @NotBlank
    @Schema(description = "caseId")
    private String caseId;
    @NotBlank
    @Schema(description = "rejectReason")
    private String rejectReason;
}
