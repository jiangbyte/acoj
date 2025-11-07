package io.charlie.web.oj.modular.data.problem.entity;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

/**
 * @author ZhangJiangHu
 * @version v1.0
 * @date 02/11/2025
 * @description TODO
 */
@Data
public class ProblemSourceLimit {
    @Schema(description = "时间限制")
    private BigDecimal maxTime;

    @Schema(description = "内存限制")
    private BigDecimal maxMemory;
}
