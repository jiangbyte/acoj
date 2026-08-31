package github.jiangbyte.io.oj.modules.submission.param;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 门户端创建提交入参。
 *
 * Author: Charlie
 */
@Schema(description = "门户端创建 OJ 提交入参")
@Data
public class OjSubmissionCreateParam {
    @NotBlank
    @Schema(description = "题目ID")
    private String problemId;
    @NotBlank
    @Schema(description = "语言 key，与 SparkSandbox 一致")
    private String language;
    @NotBlank
    @Schema(description = "源代码")
    private String sourceCode;
}
