package io.charlie.app.core.modular.analyse.entity;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * @author Charlie Zhang
 * @version v1.0
 * @date 03/08/2025
 * @description 总分析数据
 */
@Data
public class TotalAnalyseData {
    @Schema(description = "用户数")
    private Long totalUserCount;

    @Schema(description = "题目数")
    private Long totalProblemCount;

    @Schema(description = "题集数")
    private Long totalSetCount;

    @Schema(description = "总提交数")
    private Long totalSubmitCount;
}
