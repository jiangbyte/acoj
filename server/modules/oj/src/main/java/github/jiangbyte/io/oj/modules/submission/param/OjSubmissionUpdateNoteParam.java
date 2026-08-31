package github.jiangbyte.io.oj.modules.submission.param;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 门户端更新提交备注入参。
 * <p>
 * Author: Charlie
 */
@Schema(description = "门户端更新提交备注入参")
@Data
public class OjSubmissionUpdateNoteParam {
    @NotBlank
    @Schema(description = "提交 ID")
    private String id;

    @Size(max = 255)
    @Schema(description = "用户备注；空串表示清空")
    private String note;
}
