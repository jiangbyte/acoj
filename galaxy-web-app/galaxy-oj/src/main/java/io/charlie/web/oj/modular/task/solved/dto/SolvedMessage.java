package io.charlie.web.oj.modular.task.solved.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * @author ZhangJiangHu
 * @version v1.0
 * @date 02/11/2025
 * @description TODO
 */
@Data
public class SolvedMessage {
    @Schema(description = "用户ID")
    private String userId;

    @Schema(description = "题目ID")
    private String problemId;

    @Schema(description = "题集ID")
    private String setId;

    @Schema(description = "执行类型")
    private Boolean submitType;

    @Schema(description = "是否是题集")
    private Boolean isSet;

    @Schema(description = "提交ID")
    private String submitId;

    @Schema(description = "是否解决")
    private Boolean solved;

}
