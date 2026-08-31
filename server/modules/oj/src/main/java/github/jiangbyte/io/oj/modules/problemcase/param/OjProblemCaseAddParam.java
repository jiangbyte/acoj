package github.jiangbyte.io.oj.modules.problemcase.param;

/**
 * 创建 OJ 题目测例入参。
 *
 * Author: Charlie
 */

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Schema(description = "OjProblemCase 创建入参")
@Data
@EqualsAndHashCode(callSuper = true)
public class OjProblemCaseAddParam extends OjProblemCaseItemParam {

    @NotBlank
    @Size(max = 64)
    @Schema(description = "所属题目ID")
    private String problemId;

    @Schema(description = "测例包版本；空则使用题目当前 caseVersion")
    private Integer caseVersion;
}
