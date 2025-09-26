package io.charlie.web.oj.modular.task.similarity.data;

import lombok.Data;

@Data
public class Config {
    private int minSampleSize; // 最小相似样本数
    private int recentSampleSize; // 最近相似样本数
    private int minMatchLength; // 最小匹配长度

    public static boolean shouldSkip(Config config) {
        return config.getMinSampleSize() <= 0 || config.getRecentSampleSize() <= 0 || config.getMinMatchLength() <= 0;
    }
}