package github.jiangbyte.io.oj.modules.problem.param;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * 门户样例试跑入参（不入提交记录）。
 * <p>
 * Author: Charlie
 */
@Schema(description = "门户样例试跑入参")
@Data
public class OjProblemPortalRunParam {
    @NotBlank
    @Schema(description = "题目ID")
    private String problemId;
    @NotBlank
    @Schema(description = "语言 key")
    private String language;
    @NotBlank
    @Schema(description = "源代码")
    private String sourceCode;
    @Valid
    @Schema(description = "自定义用例；空则用题面 samples")
    private List<CaseItem> cases = new ArrayList<>();

    @Schema(description = "单组用例")
    @Data
    public static class CaseItem {
        @Schema(description = "标准输入")
        private String input;
        @Schema(description = "期望输出；空则只跑不比对")
        private String output;
    }
}
