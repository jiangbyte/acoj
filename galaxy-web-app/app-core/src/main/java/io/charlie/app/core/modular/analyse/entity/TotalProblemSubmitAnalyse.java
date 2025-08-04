package io.charlie.app.core.modular.analyse.entity;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * @author Charlie Zhang
 * @version v1.0
 * @date 25/07/2025
 * @description 提交分析
 */
@Data
public class TotalProblemSubmitAnalyse {
    @Schema(description = "今日提交")
    private Long dailySubmit;

    @Schema(description = "总提交")
    private Long totalSubmit;

    @Schema(description = "今日通过")
    private Long dailyAC;

    @Schema(description = "总通过数")
    private Long totalAC;
}
