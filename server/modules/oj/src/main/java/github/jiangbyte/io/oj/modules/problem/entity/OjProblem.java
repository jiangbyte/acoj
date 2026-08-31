package github.jiangbyte.io.oj.modules.problem.entity;

import io.swagger.v3.oas.annotations.media.Schema;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import github.jiangbyte.io.common.core.domain.BaseEntity;
import github.jiangbyte.io.common.mybatis.handler.JacksonJsonTypeHandler;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;
import java.util.Map;

/**
 * OJ 题目实体，对应表 {@code oj_problem}。
 *
 * Author: Charlie
 */
@Schema(description = "OJ 题目")
@Data
@EqualsAndHashCode(callSuper = true)
@TableName(value = "oj_problem", autoResultMap = true)
public class OjProblem extends BaseEntity {
    @Schema(description = "对外题号，如 P1001")
    private String problemKey;
    @Schema(description = "标题")
    private String title;
    @Schema(description = "题面 Markdown")
    private String statementMd;
    @Schema(description = "输入格式说明")
    private String inputFormat;
    @Schema(description = "输出格式说明")
    private String outputFormat;
    @Schema(description = "提示")
    private String hint;
    @TableField(typeHandler = JacksonJsonTypeHandler.class)
    @Schema(description = "题面样例 [{input,output,explanation?}]")
    private List<Map<String, Object>> samples;
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
    @TableField(typeHandler = JacksonJsonTypeHandler.class)
    @Schema(description = "允许语言 key 数组")
    private List<String> allowedLanguages;
    @Schema(description = "测例变更版本")
    private Integer caseVersion;
    @Schema(description = "状态：DRAFT/PUBLISHED/DISABLED")
    private String status;
    @Schema(description = "提交总数（冗余）")
    private Integer submitCount;
    @Schema(description = "AC 总数（冗余）")
    private Integer acceptCount;
    @Schema(description = "来源文案")
    private String source;
    @TableField(typeHandler = JacksonJsonTypeHandler.class)
    @Schema(description = "扩展信息")
    private Map<String, Object> extra;

    /** 非表字段：详情/列表回显用标签。 */
    @TableField(exist = false)
    @Schema(description = "关联标签列表")
    private List<OjTagBrief> tags;

    /** 非表字段：编辑回显用标签 ID。 */
    @TableField(exist = false)
    @Schema(description = "关联标签 ID 列表")
    private List<String> tagIds;

    @Data
    @Schema(description = "题目标签摘要")
    public static class OjTagBrief {
        private String id;
        private String name;
    }
}
