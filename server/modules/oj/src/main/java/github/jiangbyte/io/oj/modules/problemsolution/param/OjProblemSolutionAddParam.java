package github.jiangbyte.io.oj.modules.problemsolution.param;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 创建 OJ 参考答案入参。
 *
 * Author: Charlie
 */
@Schema(description = "OjProblemSolution 创建入参")
@Data
public class OjProblemSolutionAddParam {
    @NotBlank
    @Schema(description = "所属题目ID")
    private String problemId;
    @NotBlank
    @Schema(description = "语言 key")
    private String language;
    @NotBlank
    @Schema(description = "参考答案源码")
    private String source;
    @Schema(description = "是否同题默认答案")
    private Boolean isDefault;
    @Schema(description = "状态：ENABLED/DISABLED")
    private String status;
    @Schema(description = "备注")
    private String remark;
}
