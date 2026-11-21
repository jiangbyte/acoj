package io.charlie.web.modular.task.trace.param;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 跨工作流追溯查询参数。
 */
@Data
@Schema(name = "WorkflowTraceQueryParam", description = "跨工作流追溯查询参数")
public class WorkflowTraceQueryParam implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "题目ID")
    private String problemId;

    @Schema(description = "用户ID")
    private String userId;

    @Schema(description = "每类记录最大条数，默认 50，上限 100")
    @Min(1)
    @Max(100)
    private Integer limit = 50;
}
