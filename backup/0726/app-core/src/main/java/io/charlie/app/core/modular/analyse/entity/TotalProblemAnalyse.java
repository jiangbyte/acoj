package io.charlie.app.core.modular.analyse.entity;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * @author Charlie Zhang
 * @version v1.0
 * @date 25/07/2025
 * @description 题目分析
 */
@Data
public class TotalProblemAnalyse {

    @Schema(description = "题目数量")
    private Long problems;

    @Schema(description = "通过占比")
    private Double passRate;

}
