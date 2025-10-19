package io.charlie.web.oj.modular.task.similarity.utils;

import io.charlie.web.oj.modular.data.similarity.entity.TaskSimilarity;
import io.charlie.web.oj.modular.task.similarity.enums.CloneLevelEnum;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

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
public class DynamicCloneLevelDetector implements Serializable {
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
                oneMinusBase.multiply(new BigDecimal("0.7"))
        ).setScale(4, RoundingMode.HALF_UP);

        this.mediumSuspectedThreshold = baseThreshold.add(
                oneMinusBase.multiply(new BigDecimal("0.5"))
        ).setScale(4, RoundingMode.HALF_UP);

        this.lowSuspectedThreshold = baseThreshold.add(
                oneMinusBase.multiply(new BigDecimal("0.2"))
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
    @RequiredArgsConstructor
    public static class CloneLevel implements Serializable {
        private String cloneLevel;
        private String cloneLevelName;
        private BigDecimal similarity;
        private Integer count;
        private BigDecimal percentage; // 相似度占比
    }

    /**
     * 获取所有克隆级别的阈值信息
     *
     * @return 克隆级别阈值列表
     */
    public List<CloneLevel> getLevels() {
        List<CloneLevel> levels = new ArrayList<>();
        levels.add(new CloneLevel(CloneLevelEnum.HIGHLY_SUSPECTED.getValue(), CloneLevelEnum.HIGHLY_SUSPECTED.getLabel(), highlySuspectedThreshold, 0, BigDecimal.ZERO));
        levels.add(new CloneLevel(CloneLevelEnum.MEDIUM_SUSPECTED.getValue(), CloneLevelEnum.MEDIUM_SUSPECTED.getLabel(), mediumSuspectedThreshold, 0, BigDecimal.ZERO));
        levels.add(new CloneLevel(CloneLevelEnum.LOW_SUSPECTED.getValue(), CloneLevelEnum.LOW_SUSPECTED.getLabel(), lowSuspectedThreshold, 0, BigDecimal.ZERO));
        levels.add(new CloneLevel(CloneLevelEnum.NOT_REACHED.getValue(), CloneLevelEnum.NOT_REACHED.getLabel(), baseThreshold, 0, BigDecimal.ZERO));
        return levels;
    }

    /**
     * 静态方法：获取指定基础阈值的克隆级别分类
     */
    public static List<CloneLevel> getLevels(BigDecimal baseThreshold) {
        DynamicCloneLevelDetector detector = new DynamicCloneLevelDetector(baseThreshold);
        return detector.getLevels();
    }

    // 相似分布
    public List<Integer> similarityDistribution(List<TaskSimilarity> similarities) {
        // 相似度分布
        int[] distribution = new int[10];
        similarities.forEach(detail -> {
            int index = Math.min((int) (detail.getSimilarity().doubleValue() * 10), 9);
            distribution[index]++;
        });

        return Arrays.stream(distribution).boxed().collect(Collectors.toList());
    }

    public List<DynamicCloneLevelDetector.CloneLevel> similarityDegreeStatistics(List<TaskSimilarity> details) {

        List<CloneLevel> levels = this.getLevels();

        if (details.isEmpty() || levels.isEmpty()) return levels;

        // 按相似度阈值从高到低排序，确保正确的范围匹配
        List<DynamicCloneLevelDetector.CloneLevel> sortedLevels = levels.stream()
                .sorted(Comparator.comparing(DynamicCloneLevelDetector.CloneLevel::getSimilarity).reversed())
                .collect(Collectors.toList());

        int[] counts = new int[sortedLevels.size()];
        details.forEach(detail -> {
            BigDecimal similarity = detail.getSimilarity();
            int levelIndex = findMatchingLevelIndex(similarity, sortedLevels);
            if (levelIndex >= 0) counts[levelIndex]++;
        });

        int total = details.size();
        IntStream.range(0, sortedLevels.size()).forEach(i -> {
            DynamicCloneLevelDetector.CloneLevel level = sortedLevels.get(i);
            level.setCount(counts[i]);
            BigDecimal percentage = total > 0 ?
                    new BigDecimal(counts[i]).divide(new BigDecimal(total), 4, RoundingMode.HALF_UP).multiply(new BigDecimal(100)) :
                    BigDecimal.ZERO;
            level.setPercentage(percentage);
        });
        return sortedLevels;
    }

    private int findMatchingLevelIndex(BigDecimal similarity, List<DynamicCloneLevelDetector.CloneLevel> levels) {
        for (int i = 0; i < levels.size(); i++) {
            DynamicCloneLevelDetector.CloneLevel level = levels.get(i);
            if (i == 0 && similarity.compareTo(level.getSimilarity()) >= 0) return i;
            if (i > 0 && similarity.compareTo(level.getSimilarity()) >= 0 &&
                    similarity.compareTo(levels.get(i - 1).getSimilarity()) < 0) return i;
        }
        return levels.size() - 1; // 默认最低级别
    }
}
