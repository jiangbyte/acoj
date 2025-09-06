package io.charlie.app.core.modular.problem.similarity.param;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * @author ZhangJiangHu
 * @version v1.0
 * @date 06/09/2025
 * @description 题目相似度报告配置参数
 */
@Data
public class ProblemReportConfig {
    @Schema(description = "题目ID")
    private String problemId;

    @Schema(description = "是否使用题目自身阈值")
    private Boolean useProblemThreshold;

    @Schema(description = "阈值")
    private BigDecimal threshold;

    @Schema(description = "使用单种编程语言")
    private Boolean useSingleLanguage;

    @Schema(description = "题目支持的编程语言")
    private List<String> languages;

    @Schema(description = "使用单种提交用户")
    private Boolean useSingleSubmitUser;

    @Schema(description = "是否对全局用户进行检测")
    private Boolean useGlobalSubmitUser;

    @Schema(description = "通过提交的用户")
    private List<String> submitUsers;

    @Schema(description = "样本数")
    private Integer sampleCount;

}
