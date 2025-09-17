package io.charlie.app.core.modular.similarity.utils;

import cn.hutool.core.util.StrUtil;
import io.charlie.app.core.modular.judge.enums.JudgeStatus;
import io.charlie.app.core.modular.similarity.factory.LanguageStrategy;
import io.charlie.app.core.modular.similarity.factory.LanguageStrategyFactory;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

/**
 * 代码相似度计算工具类
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class CodeSimilarityCalculator {
    private final LanguageStrategyFactory languageStrategyFactory;

    private LanguageStrategy languageStrategy;

    /**
     * 计算两个代码的相似度
     *
     * @param code1          第一个代码
     * @param code2          第二个代码
     * @param minMatchLength 控制匹配敏感度
     * @return 相似度百分比 (0.0 - 1.0)
     */
    public double calculateSimilarity(String language, String code1, String code2, int minMatchLength) {
        if (StrUtil.isBlank(code1) || StrUtil.isBlank(code2)) return 0.0;
        languageStrategy = languageStrategyFactory.languageStrategy(language);
        List<Integer> tokens1 = languageStrategy.getTokenInfo(code1);
        List<Integer> tokens2 = languageStrategy.getTokenInfo(code2);
        if (tokens1.isEmpty() || tokens2.isEmpty()) return 0.0;
        int matches = greedyStringTiling(tokens1, tokens2, minMatchLength);
        double similarity = (double) (matches * 2) / (tokens1.size() + tokens2.size());
        return Double.parseDouble(new DecimalFormat("0.00").format(similarity));
    }

    /**
     * 贪婪字符串匹配算法实现
     *
     * @param list1          第一个Token序列
     * @param list2          第二个Token序列
     * @param minMatchLength 控制匹配敏感度
     * @return 匹配的Token数量
     */
    private int greedyStringTiling(List<Integer> list1, List<Integer> list2, int minMatchLength) {
        List<MatchTile> tiles = new ArrayList<>();
        boolean[] matched1 = new boolean[list1.size()];
        boolean[] matched2 = new boolean[list2.size()];

        int maxMatch;
        do {
            maxMatch = minMatchLength;
            List<MatchTile> maxTiles = new ArrayList<>();

            // 寻找所有最大匹配块
            for (int i = 0; i < list1.size(); i++) {
                if (matched1[i]) continue;

                for (int j = 0; j < list2.size(); j++) {
                    if (matched2[j]) continue;

                    int k = 0;
                    while (i + k < list1.size() &&
                            j + k < list2.size() &&
                            !matched1[i + k] &&
                            !matched2[j + k] &&
                            list1.get(i + k).equals(list2.get(j + k))) {
                        k++;
                    }

                    if (k > maxMatch) {
                        maxMatch = k;
                        maxTiles.clear();
                        maxTiles.add(new MatchTile(i, j, k));
                    } else if (k == maxMatch) {
                        maxTiles.add(new MatchTile(i, j, k));
                    }
                }
            }

            // 标记已匹配的块
            for (MatchTile tile : maxTiles) {
                for (int k = 0; k < tile.length; k++) {
                    matched1[tile.start1 + k] = true;
                    matched2[tile.start2 + k] = true;
                }
                tiles.add(tile);
            }
        } while (maxMatch > minMatchLength);

        // 计算总匹配数
        int totalMatches = 0;
        for (MatchTile tile : tiles) {
            totalMatches += tile.length;
        }

        return totalMatches;
    }

    private static class MatchTile {
        int start1;
        int start2;
        int length;

        public MatchTile(int start1, int start2, int length) {
            this.start1 = start1;
            this.start2 = start2;
            this.length = length;
        }
    }

    /**
     * 获取详细的Token匹配信息
     *
     * @param code1 第一个代码
     * @param code2 第二个代码
     * @return 包含匹配详细信息的对象
     */
    public SimilarityDetail getSimilarityDetail(String language, String code1, String code2, int minMatchLength) {
        languageStrategy = languageStrategyFactory.languageStrategy(language);

        List<Integer> tokens1 = languageStrategy.getTokenInfo(code1);
        List<Integer> tokens2 = languageStrategy.getTokenInfo(code2);

        List<String> names1 = languageStrategy.getTokenNames(tokens1);
        List<String> names2 = languageStrategy.getTokenNames(tokens2);

        List<String> texts1 = languageStrategy.getTokenTexts(tokens1, code1);
        List<String> texts2 = languageStrategy.getTokenTexts(tokens2, code2);

        double similarity = calculateSimilarity(language, code1, code2, minMatchLength);

        return new SimilarityDetail(similarity, names1, names2, texts1, texts2);
    }

    /**
     * 相似度详细信息类
     */
    @Setter
    @Getter
    public static class SimilarityDetail {
        private double similarity;
        private String submitUser; // 提交用户
        private String originUser; // 样本集用户
        private List<String> tokenNames1;
        private List<String> tokenNames2;
        private List<String> tokenTexts1;
        private List<String> tokenTexts2;

        public SimilarityDetail() {

        }

        public SimilarityDetail(double similarity, List<String> tokenNames1,
                                List<String> tokenNames2, List<String> tokenTexts1,
                                List<String> tokenTexts2) {
            this.similarity = similarity;
            this.tokenNames1 = tokenNames1;
            this.tokenNames2 = tokenNames2;
            this.tokenTexts1 = tokenTexts1;
            this.tokenTexts2 = tokenTexts2;
        }
    }

    public static JudgeStatus getStatus(Double similarity, Double threshold) {
        if (similarity >= threshold) {
            return JudgeStatus.SIMILARITY_SUSPICIOUS;
        } else {
            return JudgeStatus.SIMILARITY_ACCEPTED;
        }
    }
}