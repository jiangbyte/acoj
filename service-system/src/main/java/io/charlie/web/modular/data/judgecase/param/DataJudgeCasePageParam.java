package io.charlie.web.modular.data.judgecase.param;

import java.io.Serializable;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;

/**
 * @author Charlie Zhang
 * @version v1.0
 * @date 2025-10-26
 * @description 判题结果用例 分页参数
 */
@Data
@Schema(name = "DataJudgeCase", description = "判题结果用例 分页参数")
public class DataJudgeCasePageParam implements Serializable {
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

    @Schema(description = "提交ID")
    private String submitId;
}