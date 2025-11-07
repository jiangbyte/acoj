package io.charlie.web.oj.modular.data.solved.entity;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 系统整体统计信息
 */
@Data
public class ProblemOverallStats {
    // 总题目数
    private BigDecimal totalProblems = BigDecimal.ZERO;
    // 有提交的题目数
    private BigDecimal problemsWithSubmissions = BigDecimal.ZERO;
    // 有提交题目的比例
    private BigDecimal submissionRatio = BigDecimal.ZERO;
    // 总参与人次
    private BigDecimal totalParticipants = BigDecimal.ZERO;
    // 总通过人次
    private BigDecimal totalAcceptedParticipants = BigDecimal.ZERO;
    // 平均通过率
    private BigDecimal averageAcceptanceRate = BigDecimal.ZERO;
    // 最低通过率
    private BigDecimal minAcceptanceRate = BigDecimal.ZERO;
    // 最高通过率
    private BigDecimal maxAcceptanceRate = BigDecimal.ZERO;
}