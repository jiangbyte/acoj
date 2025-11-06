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

    @Schema(description = "模块类型")
    private String moduleType;
    @Schema(description = "模块ID")
    private String moduleId;

    @Schema(description = "执行类型")
    private Boolean submitType;

    @Schema(description = "提交ID")
    private String submitId;

    @Schema(description = "是否解决")
    private Boolean solved;

}
