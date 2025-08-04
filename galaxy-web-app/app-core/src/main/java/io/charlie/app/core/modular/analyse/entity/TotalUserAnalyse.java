package io.charlie.app.core.modular.analyse.entity;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * @author Charlie Zhang
 * @version v1.0
 * @date 25/07/2025
 * @description 用户分析
 */
@Data
public class TotalUserAnalyse {
    @Schema(description = "注册用户数")
    private Long registerUser;

    @Schema(description = "日活跃用户数")
    private Long dailyActiveUser;

    @Schema(description = "周活跃用户数")
    private Long weeklyActiveUser;

    @Schema(description = "月活跃用户数")
    private Long monthlyActiveUser;
}
