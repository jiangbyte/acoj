package io.charlie.app.core.modular.problem.problem.param;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
* @author Charlie Zhang
* @version v1.0
* @date 2025-07-25
* @description 用户题目 分页参数
*/
@Data
@Schema(name = "ProProblem", description = "用户题目 分页参数")
public class SetProblemListParam implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "排序字段")
    private String sortField;

    @Schema(description = "排序方式")
    private String sortOrder;

    @Schema(description = "关键词")
    private String keyword;

    @Schema(description = "标签")
    private String tagId;

    @Schema(description = "分类")
    private String categoryId;

    @Schema(description = "难度")
    private String difficulty;

    @Schema(description = "用户id")
    @NotNull(message = "题集id不能为空")
    private String setId;

}