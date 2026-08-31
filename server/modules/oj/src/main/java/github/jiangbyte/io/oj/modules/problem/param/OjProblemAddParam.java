package github.jiangbyte.io.oj.modules.problem.param;

/**
 * 创建 OJ 题目入参。
 *
 * Author: Charlie
 */

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.List;
import java.util.Map;

@Schema(description = "OjProblem 创建入参")
@Data
public class OjProblemAddParam {
    @NotBlank
    @Schema(description = "对外题号，如 P1001")
    private String problemKey;
    @NotBlank
    @Schema(description = "标题")
    private String title;
    @NotBlank
    @Schema(description = "题面 Markdown")
    private String statementMd;
    @Schema(description = "输入格式说明")
    private String inputFormat;
    @Schema(description = "输出格式说明")
    private String outputFormat;
    @Schema(description = "提示")
    private String hint;
    @Schema(description = "题面样例 [{input,output,explanation?}]")
    private List<Map<String, Object>> samples;
    @NotBlank
    @Schema(description = "难度：EASY/MEDIUM/HARD")
    private String difficulty;
    @Schema(description = "CPU 时限毫秒")
    private Integer timeLimitMs;
    @Schema(description = "内存限额字节")
    private Long memoryLimitBytes;
    @Schema(description = "栈限额字节，空则用沙箱默认")
    private Long stackLimitBytes;
    @Schema(description = "输出限额字节，空则用沙箱默认")
    private Long outputLimitBytes;
    @Schema(description = "判题模式；P0: STANDARD")
    private String judgeMode;
    @Schema(description = "允许语言 key 数组")
    private List<String> allowedLanguages;
    @Schema(description = "状态：DRAFT/PUBLISHED/DISABLED")
    private String status;
    @Schema(description = "来源文案")
    private String source;
    @Schema(description = "扩展信息")
    private Map<String, Object> extra;
    @Schema(description = "绑定标签 ID 列表，最多 2 个")
    private List<String> tagIds;
}
