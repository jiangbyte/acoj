package github.jiangbyte.io.oj.modules.problem.result;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * 门户样例试跑结果。
 * <p>
 * Author: Charlie
 */
@Schema(description = "门户样例试跑结果")
@Data
public class OjProblemPortalRunResult {
    @Schema(description = "整单状态 AC/WA/CE/...")
    private String status;
    @Schema(description = "编译输出")
    private String compileOutput;
    @Schema(description = "简短说明")
    private String judgeMessage;
    @Schema(description = "耗时峰值 ms")
    private Integer timeMs;
    @Schema(description = "内存峰值字节")
    private Long memoryBytes;
    @Schema(description = "逐例结果")
    private List<CaseResult> caseResults = new ArrayList<>();

    @Schema(description = "单例结果")
    @Data
    public static class CaseResult {
        private String caseKey;
        private String status;
        private Integer timeMs;
        private Long memoryBytes;
        private String message;
        private String stdin;
        private String expected;
        private String stdout;
    }
}
