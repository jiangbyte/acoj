package github.jiangbyte.io.profile.modules.identity.param;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 管理端通过实名认证工单。
 *
 * Author: Charlie
 */
@Schema(description = "管理端通过实名认证工单。")
@Data
public class RealNameCaseApproveParam {

    @NotBlank
    @Schema(description = "caseId")
    private String caseId;
    @Schema(description = "备注说明")
    private String remark;
}
