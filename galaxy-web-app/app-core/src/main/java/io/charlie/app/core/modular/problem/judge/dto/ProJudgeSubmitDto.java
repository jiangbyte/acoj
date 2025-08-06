package io.charlie.app.core.modular.problem.judge.dto;

import io.charlie.app.core.modular.problem.problem.entity.TestCase;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

/**
 * @author Charlie Zhang
 * @version v1.0
 * @date 25/07/2025
 * @description 题目判题提交参数
 */
@Data
public class ProJudgeSubmitDto implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    // 用户 提交参数

    @Schema(description = "题目ID")
    private String problemId;

    @Schema(description = "语言")
    private String language;

    @Schema(description = "代码")
    private String code;

    @Schema(description = "执行类型")
    private Boolean submitType;

    // 题目参数

    @Schema(description = "时间限制")
    private Integer maxTime;

    @Schema(description = "内存限制")
    private Integer maxMemory;

    @Schema(description = "用例")
    private List<TestCase> testCase;

    // 任务参数

    @Schema(description = "主键")
    private String id;

}
