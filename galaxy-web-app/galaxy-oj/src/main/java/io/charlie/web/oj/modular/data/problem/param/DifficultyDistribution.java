package io.charlie.web.oj.modular.data.problem.param;

import lombok.Data;

import java.math.BigDecimal;

/**
 * @author ZhangJiangHu
 * @version v1.0
 * @date 27/08/2025
 * @description 难度分布
 */
@Data
public class DifficultyDistribution {
    // 难度
    private Integer difficulty;

    // 难度
    private String difficultyName;

    // 数量
    private Long count;

    // 百分比
    private BigDecimal percentage;
}
