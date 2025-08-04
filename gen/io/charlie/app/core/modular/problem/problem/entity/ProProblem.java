package io.charlie.app.core.modular.problem.problem.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.TableName;
import io.charlie.galaxy.pojo.CommonEntity;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.io.Serial;
import java.util.Date;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
* @author Charlie Zhang
* @version v1.0
* @date 2025-07-27
* @description 题目表
*/
@EqualsAndHashCode(callSuper = true)
@Data
@TableName("pro_problem")
@Schema(name = "ProProblem", description = "题目表")
public class ProProblem extends CommonEntity {
    @Serial
    @TableField(exist = false)
    private static final long serialVersionUID = 1L;

    @Schema(description = "主键")
    private String id;

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
    private String testCase;

    @Schema(description = "开放语言")
    private String allowedLanguages;

    @Schema(description = "难度")
    private Integer difficulty;

    @Schema(description = "使用模板")
    private Boolean useTemplate;

    @Schema(description = "模板代码")
    private String codeTemplate;

    @Schema(description = "解决")
    private Long solved;
}
