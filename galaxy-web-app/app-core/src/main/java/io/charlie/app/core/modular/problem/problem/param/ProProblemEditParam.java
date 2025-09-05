package io.charlie.app.core.modular.problem.problem.param;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import io.charlie.app.core.modular.problem.problem.entity.CodeTemplate;
import io.charlie.app.core.modular.problem.problem.entity.TestCase;
import io.charlie.galaxy.pojo.CommonEntity;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import java.io.Serial;
import java.util.List;

/**
* @author Charlie Zhang
* @version v1.0
* @date 2025-07-25
* @description 题目 编辑参数
*/
@Data
@Schema(name = "ProProblem", description = "题目 编辑参数")
public class ProProblemEditParam implements Serializable {
    @Serial
    @TableField(exist = false)
    private static final long serialVersionUID = 1L;

    @Schema(description = "主键")
    private String id;

    @Schema(description = "显示ID")
    private String displayId;

    @Schema(description = "分类")
    private String categoryId;

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
    private List<TestCase> testCase;

    @Schema(description = "开放语言")
    private List<String> allowedLanguages;

    @Schema(description = "难度")
    private Integer difficulty;

    @Schema(description = "使用模板")
    private Boolean useTemplate;

    @Schema(description = "模板代码")
    private List<CodeTemplate> codeTemplate;

    @Schema(description = "标签ID")
    @TableField(exist = false)
    private List<String> tagIds;

    @Schema(description = "阈值")
    private BigDecimal threshold;

    @Schema(description = "是否公开")
    private Boolean isPublic;


    @Schema(description = "是否 LLM 增强")
    private Boolean isLlmEnhanced;

}