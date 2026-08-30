package github.jiangbyte.io.sys.modules.feedback.param;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 管理端更新反馈的请求参数（状态与回复内容）。
 *
 * Author: Charlie
 */
@Schema(description = "管理端更新反馈的请求参数（状态与回复内容）。")
@Data
public class SysFeedbackEditParam {

    @NotBlank
    @Size(max = 64)
    @Schema(description = "主键ID")
    private String id;
    @Schema(description = "反馈状态：PENDING/REPLIED/CLOSED 等")
    private String status;
    @Schema(description = "管理员回复内容")
    private String reply;
}
