package io.charlie.app.core.modular.problem.problem.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import io.charlie.app.core.modular.sys.category.entity.SysCategory;
import io.charlie.galaxy.pojo.CommonEntity;

import java.io.Serial;
import java.math.BigDecimal;
import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.dromara.core.trans.anno.Trans;
import org.dromara.core.trans.constant.TransType;

/**
 * @author Charlie Zhang
 * @version v1.0
 * @date 2025-07-25
 * @description 题目表
 */
@EqualsAndHashCode(callSuper = true)
@Data
@TableName(value = "pro_problem", autoResultMap = true)
@Schema(name = "ProProblem", description = "题目表")
public class ProProblem extends CommonEntity {
    @Serial
    @TableField(exist = false)
    private static final long serialVersionUID = 1L;

    @Schema(description = "主键")
    private String id;

    @Schema(description = "显示ID")
    private String displayId;

    @Schema(description = "分类")
    @Trans(type = TransType.SIMPLE, target = SysCategory.class, fields = "name", ref = "categoryName")
    private String categoryId;

    @Schema(description = "分类名称")
    @TableField(exist = false)
    private String categoryName;

    @Schema(description = "标题")
    private String title;

    @Schema(description = "来源")
    private String source;

    @Schema(description = "链接")
    private String url;

    @Schema(description = "时间限制")
    private Integer maxTime;

    @Schema(description = "内存限制")
    private Integer maxMemory;

    @Schema(description = "描述")
    private String description;

    @Schema(description = "用例")
    @TableField(typeHandler = JacksonTypeHandler.class)
    private List<TestCase> testCase;

    @Schema(description = "开放语言")
    @TableField(typeHandler = JacksonTypeHandler.class)
    private List<String> allowedLanguages;

    @Schema(description = "难度")
    @Trans(type = TransType.DICTIONARY, key = "PROBLEM_DIFFICULTY", ref = "difficultyName")
    private Integer difficulty;

    @Schema(description = "难度名称")
    @TableField(exist = false)
    private String difficultyName;

    @Schema(description = "使用模板")
    @Trans(type = TransType.DICTIONARY, key = "YES_NO", ref = "useTemplateName")
    private Boolean useTemplate;

    @Schema(description = "使用模板名称")
    @TableField(exist = false)
    private String useTemplateName;

    @Schema(description = "模板代码")
    @TableField(typeHandler = JacksonTypeHandler.class)
    private List<CodeTemplate> codeTemplate;

    @Schema(description = "解决")
    private Long solved;

    @Schema(description = "标签ID")
    @TableField(exist = false)
    private List<String> tagIds;

    @Schema(description = "标签名称")
    @TableField(exist = false)
    private List<String> tagNames;

    @Schema(description = "当前用户是否已解决")
    @TableField(exist = false)
    private Boolean currentUserSolved;

    @Schema(description = "阈值")
    private BigDecimal threshold;

    @Schema(description = "通过率")
    @TableField(exist = false)
    private BigDecimal acceptance;

    @Schema(description = "参与人数")
    @TableField(exist = false)
    private String participantCount;

    @Schema(description = "题集用 上一个题目ID")
    @TableField(exist = false)
    private String previousProblemId;

    @Schema(description = "题集用 下一个题目ID")
    @TableField(exist = false)
    private String nextProblemId;

    @Schema(description = "题集用 当前题目位次")
    @TableField(exist = false)
    private Integer sort;

    @Schema(description = "题集用 总题目数")
    @TableField(exist = false)
    private Integer totalProblemCount;

    @Schema(description = "题集用 是否是最后一个题目")
    @TableField(exist = false)
    private Boolean isLastProblem;

    @Schema(description = "是否公开")
    @Trans(type = TransType.DICTIONARY, key = "YES_NO", ref = "isPublicName")
    private Boolean isPublic;

    @Schema(description = "是否可以使用相似报告")
    @TableField(exist = false)
    private Boolean canUseSimilarReport;

    @Schema(description = "是否公开名称")
    @TableField(exist = false)
    private String isPublicName;

    @Schema(description = "是否 LLM 增强")
    @Trans(type = TransType.DICTIONARY, key = "YES_NO", ref = "isLlmEnhancedName")
    private Boolean isLlmEnhanced;

    @Schema(description = "是否 LLM 增强名称")
    @TableField(exist = false)
    private String isLlmEnhancedName;
}
