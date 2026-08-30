package github.jiangbyte.io.profile.modules.identity.param;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * 提交实名认证工单（人工通道）。
 *
 * Author: Charlie
 */
@Schema(description = "提交实名认证工单（人工通道）。")
@Data
public class RealNameCaseSubmitParam {
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
    @Schema(description = "附件ID列表（JSON 数组）")
    private List<String> attachmentIds = new ArrayList<>();
    @Schema(description = "applicantContact")
    private String applicantContact;
}
