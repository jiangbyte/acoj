package github.jiangbyte.io.oj.modules.problemsolution.param;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 编辑 OJ 参考答案入参。
 *
 * Author: Charlie
 */
@Schema(description = "OjProblemSolution 编辑入参")
@Data
public class OjProblemSolutionEditParam {

    @NotBlank
    @Size(max = 64)
    @Schema(description = "主键ID")
    private String id;
    @NotBlank
    @Schema(description = "语言 key")
    private String language;
    @NotBlank
    @Schema(description = "参考答案源码")
    private String source;
    @Schema(description = "是否同题默认答案")
    private Boolean isDefault;
    @NotBlank
    @Schema(description = "状态：ENABLED/DISABLED")
    private String status;
    @Schema(description = "备注")
    private String remark;
}
