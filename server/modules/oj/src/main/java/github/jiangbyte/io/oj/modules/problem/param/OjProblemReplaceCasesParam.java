package github.jiangbyte.io.oj.modules.problem.param;

/**
 * 题目测例整包替换（升版本）入参。
 *
 * Author: Charlie
 */

import github.jiangbyte.io.oj.modules.problemcase.param.OjProblemCaseItemParam;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

@Schema(description = "题目测例整包替换入参")
@Data
public class OjProblemReplaceCasesParam {

    @NotBlank
    @Size(max = 64)
    @Schema(description = "题目ID")
    private String problemId;

    @NotNull
    @Valid
    @Schema(description = "新版本测例列表")
    private List<OjProblemCaseItemParam> cases;
}
