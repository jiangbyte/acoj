package io.charlie.app.core.modular.similarity.utils;

import io.charlie.app.core.modular.similarity.enums.CloneLevelEnum;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

/**
 * @author ZhangJiangHu
 * @version v1.0
 * @date 05/09/2025
 * @description 动态克隆等级检测器
 * 高度疑似阈值(CloneLevelEnum类 HIGHLY_SUSPECTED) = 基础阈值 + (1 - 基础阈值) × 0.5
 * 中度疑似阈值(CloneLevelEnum类 MEDIUM_SUSPECTED) = 基础阈值 + (1 - 基础阈值) × 0.25
 * 轻度疑似阈值(CloneLevelEnum类 LOW_SUSPECTED) = 基础阈值 + (1 - 基础阈值) × 0.1
 * 未达阈值(CloneLevelEnum类 NOT_REACHED) = 基础阈值以下的区间
 * <p>
 * 1. 创建对象时接收基础阈值
 * 2. 方法：给出一个列表，阈值的判断标准列表：
 * - 高度疑似阈值(CloneLevelEnum类 HIGHLY_SUSPECTED) = 基础阈值 + (1 - 基础阈值) × 0.5
 * - 中度疑似阈值(CloneLevelEnum类 MEDIUM_SUSPECTED) = 基础阈值 + (1 - 基础阈值) × 0.25
 * - 轻度疑似阈值(CloneLevelEnum类 LOW_SUSPECTED) = 基础阈值 + (1 - 基础阈值) × 0.1
 * - 未达阈值(CloneLevelEnum类 NOT_REACHED) = 基础阈值以下的区间
 * 3. 方法：接收相似度检测结果，返回对应的CloneLevel枚举值
 */
@Data
public class DynamicCloneLevelDetector {
    private BigDecimal baseThreshold;
    private BigDecimal highlySuspectedThreshold;
    private BigDecimal mediumSuspectedThreshold;
    private BigDecimal lowSuspectedThreshold;

    public DynamicCloneLevelDetector(BigDecimal baseThreshold) {
        this.baseThreshold = baseThreshold;
        calculateThresholds();
    }

    /**
     * 计算各个级别的阈值
     */
    private void calculateThresholds() {
        // 计算：基础阈值 + (1 - 基础阈值) × 系数
        BigDecimal oneMinusBase = BigDecimal.ONE.subtract(baseThreshold);

        this.highlySuspectedThreshold = baseThreshold.add(
                oneMinusBase.multiply(new BigDecimal("0.5"))
        ).setScale(4, RoundingMode.HALF_UP);

        this.mediumSuspectedThreshold = baseThreshold.add(
                oneMinusBase.multiply(new BigDecimal("0.25"))
        ).setScale(4, RoundingMode.HALF_UP);

        this.lowSuspectedThreshold = baseThreshold.add(
                oneMinusBase.multiply(new BigDecimal("0.1"))
        ).setScale(4, RoundingMode.HALF_UP);
    }

    /**
     * 动态判断克隆级别
     *
     * @param similarity 相似度
     * @return 克隆级别枚举值
     */
    public CloneLevelEnum detect(BigDecimal similarity) {
        if (similarity.compareTo(baseThreshold) < 0) {
            return CloneLevelEnum.NOT_REACHED;
        } else if (similarity.compareTo(highlySuspectedThreshold) >= 0) {
            return CloneLevelEnum.HIGHLY_SUSPECTED;
        } else if (similarity.compareTo(mediumSuspectedThreshold) >= 0) {
            return CloneLevelEnum.MEDIUM_SUSPECTED;
        } else if (similarity.compareTo(lowSuspectedThreshold) >= 0) {
            return CloneLevelEnum.LOW_SUSPECTED;
        } else {
            return CloneLevelEnum.NOT_REACHED; // 理论上不会走到这里
        }
    }

    /**
     * 获取克隆级别分类
     */
    @AllArgsConstructor
    @Data
    public static class CloneLevel {
        private CloneLevelEnum cloneLevel;
        private BigDecimal similarity;
    }

    /**
     * 获取所有克隆级别的阈值信息
     *
     * @return 克隆级别阈值列表
     */
    public List<CloneLevel> getLevels() {
        List<CloneLevel> levels = new ArrayList<>();
        levels.add(new CloneLevel(CloneLevelEnum.HIGHLY_SUSPECTED, highlySuspectedThreshold));
        levels.add(new CloneLevel(CloneLevelEnum.MEDIUM_SUSPECTED, mediumSuspectedThreshold));
        levels.add(new CloneLevel(CloneLevelEnum.LOW_SUSPECTED, lowSuspectedThreshold));
        levels.add(new CloneLevel(CloneLevelEnum.NOT_REACHED, baseThreshold));
        return levels;
    }

    /**
     * 静态方法：获取指定基础阈值的克隆级别分类
     */
    public static List<CloneLevel> getLevels(BigDecimal baseThreshold) {
        DynamicCloneLevelDetector detector = new DynamicCloneLevelDetector(baseThreshold);
        return detector.getLevels();
    }
}
