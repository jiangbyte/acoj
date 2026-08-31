package github.jiangbyte.io.oj.modules.problemcase.param;

/**
 * 编辑 OJ 题目测例入参。
 *
 * Author: Charlie
 */

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.Map;

@Schema(description = "OjProblemCase 编辑入参")
@Data
public class OjProblemCaseEditParam {

    @NotBlank
    @Size(max = 64)
    @Schema(description = "主键ID")
    private String id;
    @NotBlank
    @Size(max = 64)
    @Schema(description = "所属题目ID")
    private String problemId;
    @NotNull
    @Schema(description = "测例包版本")
    private Integer caseVersion;
    @NotBlank
    @Schema(description = "题内测例号，如 1、sample1")
    private String caseKey;
    @Schema(description = "判题与展示顺序")
    private Integer sortNo;
    @Schema(description = "是否样例（可对用户展示）")
    private Boolean isSample;
    @Schema(description = "预留 OI 分值；P0 STANDARD 可忽略")
    private Integer score;
    @NotBlank
    @Schema(description = "输入存储：INLINE/OBJECT")
    private String inputStorage;
    @NotBlank
    @Schema(description = "输出存储：INLINE/OBJECT")
    private String outputStorage;
    @Schema(description = "INLINE 输入；OBJECT 时为空")
    private String inputText;
    @Schema(description = "INLINE 期望输出；OBJECT 时为空")
    private String outputText;
    @Schema(description = "OBJECT 输入对象键")
    private String inputObjectKey;
    @Schema(description = "OBJECT 期望输出对象键")
    private String outputObjectKey;
    @Schema(description = "输入字节数")
    private Integer inputBytes;
    @Schema(description = "输出字节数")
    private Integer outputBytes;
    @Schema(description = "可选校验 SHA256")
    private String checksumSha256;
    @NotBlank
    @Schema(description = "状态：ENABLED/DISABLED")
    private String status;
    @Schema(description = "扩展信息")
    private Map<String, Object> extra;
}
