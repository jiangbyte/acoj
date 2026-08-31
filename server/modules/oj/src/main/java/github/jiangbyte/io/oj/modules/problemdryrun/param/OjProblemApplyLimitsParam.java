package github.jiangbyte.io.oj.modules.problemdryrun.param;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 将建议限额写回题目入参。
 *
 * Author: Charlie
 */
@Schema(description = "写回题目资源限额入参")
@Data
public class OjProblemApplyLimitsParam {
    @NotBlank
    @Schema(description = "题目ID")
    private String problemId;
    @NotNull
    @Schema(description = "时限毫秒")
    private Integer timeLimitMs;
    @NotNull
    @Schema(description = "内存限额字节")
    private Long memoryLimitBytes;
}
