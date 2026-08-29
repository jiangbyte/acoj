package io.charlie.web.modular.data.set.param;

import java.io.Serializable;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import java.io.Serial;

/**
* @author Charlie Zhang
* @version v1.0
* @date 2025-09-20
* @description 题集 分页参数
*/
@Data
@Schema(name = "DataSet", description = "题集 分页参数")
public class DataSetPageParam implements Serializable {
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
}