package io.charlie.app.core.modular.problem.problem.param;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import io.charlie.galaxy.pojo.CommonEntity;
import java.io.Serializable;
import java.time.LocalDateTime;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import java.io.Serial;

/**
* @author Charlie Zhang
* @version v1.0
* @date 2025-07-25
* @description 题目 增加参数
*/
@Data
@Schema(name = "ProProblem", description = "题目 增加参数")
public class ProProblemAddParam implements Serializable {
    @Serial
    @TableField(exist = false)
    private static final long serialVersionUID = 1L;

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