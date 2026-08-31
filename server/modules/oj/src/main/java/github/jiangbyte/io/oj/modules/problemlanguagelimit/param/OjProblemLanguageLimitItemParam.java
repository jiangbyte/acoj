package github.jiangbyte.io.oj.modules.problemlanguagelimit.param;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 题目语言限额入参（嵌套于题目创建/更新）。
 * <p>
 * Author: Charlie
 */
@Schema(description = "题目语言限额项")
@Data
public class OjProblemLanguageLimitItemParam {
    @NotBlank
    @Schema(description = "语言 key")
    private String language;
    @NotNull
    @Min(1)
    @Schema(description = "CPU 时限毫秒")
    private Integer timeLimitMs;
    @NotNull
    @Min(1)
    @Schema(description = "内存限额字节")
    private Long memoryLimitBytes;
    @Schema(description = "栈限额字节，空则用沙箱默认")
    private Long stackLimitBytes;
    @Schema(description = "输出限额字节，空则用沙箱默认")
    private Long outputLimitBytes;
}
