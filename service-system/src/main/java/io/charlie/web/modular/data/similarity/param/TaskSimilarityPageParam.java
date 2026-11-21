package io.charlie.web.modular.data.similarity.param;

import java.io.Serializable;
import java.math.BigDecimal;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import lombok.Data;
import java.io.Serial;

/**
* @author Charlie Zhang
* @version v1.0
* @date 2025-09-21
* @description 检测结果任务库 分页参数
*/
@Data
@Schema(name = "TaskSimilarity", description = "检测结果任务库 分页参数")
public class TaskSimilarityPageParam implements Serializable {
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

    @Schema(description = "任务ID")
    private String taskId;

    @Schema(description = "题目ID")
    private String problemId;

    @Schema(description = "提交ID")
    private String submitId;

    @Schema(description = "用户ID（提交侧或样本侧）")
    private String userId;

    @Schema(description = "提交用户ID")
    private String submitUser;

    @Schema(description = "样本用户ID")
    private String originUser;

    @Schema(description = "相似度下限，闭区间，与库中 similarity 一致，取值 0~1")
    @DecimalMin(value = "0", message = "minSimilarity must be between 0 and 1")
    @DecimalMax(value = "1", message = "minSimilarity must be between 0 and 1")
    private BigDecimal minSimilarity;

    @Schema(description = "相似度上限，闭区间，与库中 similarity 一致，取值 0~1")
    @DecimalMin(value = "0", message = "maxSimilarity must be between 0 and 1")
    @DecimalMax(value = "1", message = "maxSimilarity must be between 0 and 1")
    private BigDecimal maxSimilarity;
}