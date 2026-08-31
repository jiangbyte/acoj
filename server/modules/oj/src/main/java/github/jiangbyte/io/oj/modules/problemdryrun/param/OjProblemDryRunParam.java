package github.jiangbyte.io.oj.modules.problemdryrun.param;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 管理端题目试跑入参。
 *
 * Author: Charlie
 */
@Schema(description = "OJ 题目试跑入参")
@Data
public class OjProblemDryRunParam {
    @NotBlank
    @Schema(description = "题目ID")
    private String problemId;
    @NotBlank
    @Schema(description = "语言 key")
    private String language;
    @Schema(description = "覆盖源码；空则用 STORED 参考答案")
    private String source;
    @Schema(description = "SINGLE 时测例号；空则为 ALL")
    private String caseKey;
    @NotBlank
    @Schema(description = "PROBLEM/RELAXED")
    private String limitMode;
    @Schema(description = "遇错是否停止；默认 false（便于摸底峰值）")
    private Boolean stopOnFirstError;
}
