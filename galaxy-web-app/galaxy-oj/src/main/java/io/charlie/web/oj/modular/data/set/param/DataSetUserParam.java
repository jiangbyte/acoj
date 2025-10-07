package io.charlie.web.oj.modular.data.set.param;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
* @author Charlie Zhang
* @version v1.0
* @date 2025-09-20
* @description 题集
*/
@Data
@Schema(name = "DataSet", description = "题集 ID参数")
public class DataSetUserParam implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @NotBlank(message = "id不能为空")
    private String setId;

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
}