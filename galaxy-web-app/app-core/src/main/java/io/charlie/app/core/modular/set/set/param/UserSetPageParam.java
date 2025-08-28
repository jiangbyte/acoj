package io.charlie.app.core.modular.set.set.param;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
* @author Charlie Zhang
* @version v1.0
* @date 2025-07-25
* @description 用户题集 分页参数
*/
@Data
@Schema(name = "ProSet", description = "用户题集 分页参数")
public class UserSetPageParam implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "当前页码")
    private Integer current;

    @Schema(description = "每页条数")
    private Integer size;

    @Schema(description = "排序字段")
    private String sortField;

    @Schema(description = "排序方式")
    private String sortOrder;

    @Schema(description = "关键词")
    private String keyword;

    @Schema(description = "分类")
    private String categoryId;

    @Schema(description = "难度")
    private String difficulty;

    @Schema(description = "类型")
    private String setType;

    @Schema(description = "用户id")
    @NotNull(message = "用户id不能为空")
    private String userId;
}