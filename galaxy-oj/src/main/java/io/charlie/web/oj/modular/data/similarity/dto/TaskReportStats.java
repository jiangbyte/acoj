package io.charlie.web.oj.modular.data.similarity.dto;

import lombok.Data;

import java.math.BigDecimal;

/**
 * @author ZhangJiangHu
 * @version v1.0
 * @date 21/10/2025
 */
@Data
public class TaskReportStats {
    private Integer sampleCount;
    private Integer groupCount;
    private BigDecimal avgSimilarity;
    private BigDecimal maxSimilarity;
}
