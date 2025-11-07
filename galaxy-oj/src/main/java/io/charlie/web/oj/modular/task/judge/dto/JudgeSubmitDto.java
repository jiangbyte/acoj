package io.charlie.web.oj.modular.task.judge.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
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
public class JudgeSubmitDto implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    // 提交参数

    @Schema(description = "用户ID")
    private String userId;

    @Schema(description = "题目ID")
    private String problemId;

    @Schema(description = "语言")
    private String language;

    @Schema(description = "代码")
    private String code;

    @Schema(description = "执行类型")
    private Boolean submitType;

    @Schema(description = "模块类型")
    private String moduleType;
    @Schema(description = "模块ID")
    private String moduleId;

    // 题目参数

    @Schema(description = "时间限制")
    private BigDecimal maxTime;

    @Schema(description = "内存限制")
    private BigDecimal maxMemory;

    @Schema(description = "用例")
    private List<TestCase> testCase;

    // 任务参数

    @Schema(description = "主键")
    private String id;

    @Schema(description = "任务ID")
    private String judgeTaskId;
}
