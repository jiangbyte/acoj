package github.jiangbyte.io.oj.modules.problemcase.entity;

import io.swagger.v3.oas.annotations.media.Schema;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import github.jiangbyte.io.common.core.domain.BaseEntity;
import github.jiangbyte.io.common.mybatis.handler.JacksonJsonTypeHandler;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Map;

/**
 * OJ 题目测例实体，对应表 {@code oj_problem_case}。
 *
 * Author: Charlie
 */
@Schema(description = "OJ 题目测例")
@Data
@EqualsAndHashCode(callSuper = true)
@TableName(value = "oj_problem_case", autoResultMap = true)
public class OjProblemCase extends BaseEntity {
    @Schema(description = "所属题目ID")
    private String problemId;
    @Schema(description = "测例包版本，与 oj_problem.case_version 对齐")
    private Integer caseVersion;
    @Schema(description = "题内测例号，如 1、sample1")
    private String caseKey;
    @Schema(description = "判题与展示顺序")
    private Integer sortNo;
    @Schema(description = "是否样例（可对用户展示）")
    private Boolean isSample;
    @Schema(description = "预留 OI 分值；P0 STANDARD 可忽略")
    private Integer score;
    @Schema(description = "输入存储：INLINE/OBJECT")
    private String inputStorage;
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
    @Schema(description = "状态：ENABLED/DISABLED")
    private String status;
    @TableField(typeHandler = JacksonJsonTypeHandler.class)
    @Schema(description = "扩展信息")
    private Map<String, Object> extra;
}
