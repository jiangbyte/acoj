package io.charlie.web.oj.modular.task.judge.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

/**
 * @author Charlie Zhang
 * @version v1.0
 * @date 25/07/2025
 * @description 题集判题提交参数
 */
@Data
public class JudgeResultDto implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    // 提交参数

    @Schema(description = "用户ID")
    private String userId;

    @Schema(description = "题目ID")
    private String problemId;

    @Schema(description = "题集ID")
    private String setId;

    @Schema(description = "语言")
    private String language;

    @Schema(description = "代码")
    private String code;

    @Schema(description = "执行类型")
    private Boolean submitType;

    @Schema(description = "是否是题集")
    private Boolean isSet;

    // 题目参数

    @Schema(description = "时间限制")
    private BigDecimal maxTime;

    @Schema(description = "内存限制")
    private BigDecimal maxMemory;

    @Schema(description = "用例")
    private List<SubmitTestCase> testCase;

    // 任务参数

    @Schema(description = "主键")
    private String id;

    @Schema(description = "消息")
    private String message;

    @Schema(description = "状态")
    private String status;

    @Schema(description = "退出码")
    private Integer exitCode;

    @Schema(description = "任务ID")
    private String judgeTaskId;
}
