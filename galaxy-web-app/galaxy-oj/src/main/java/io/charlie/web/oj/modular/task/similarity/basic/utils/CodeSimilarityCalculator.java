package io.charlie.web.oj.modular.task.similarity.basic.utils;

import cn.hutool.core.util.StrUtil;
import io.charlie.web.oj.modular.task.judge.enums.JudgeStatus;
import io.charlie.web.oj.modular.task.similarity.basic.factory.LanguageStrategy;
import io.charlie.web.oj.modular.task.similarity.basic.factory.LanguageStrategyFactory;
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
     * @param submit          第一个代码
     * @param library          第二个代码
     * @param minMatchLength 控制匹配敏感度
     * @return 相似度百分比 (0.0 - 1.0)
     */
    public double calculateSimilarity(String language, String submit, String library, int minMatchLength) {
        if (StrUtil.isBlank(submit) || StrUtil.isBlank(library)) return 0.0;
        languageStrategy = languageStrategyFactory.languageStrategy(language);
        List<Integer> submitTokens = languageStrategy.getTokenInfo(submit);
        List<Integer> libraryTokens = languageStrategy.getTokenInfo(library);
        if (submitTokens.isEmpty() || libraryTokens.isEmpty()) return 0.0;
        int matches = greedyStringTiling(submitTokens, libraryTokens, minMatchLength);
        double similarity = (double) (matches * 2) / (submitTokens.size() + libraryTokens.size());
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
     * @param submit 第一个代码
     * @param library 第二个代码
     * @return 包含匹配详细信息的对象
     */
    public SimilarityDetail getSimilarityDetail(String language, String submit, String library, int minMatchLength) {
        languageStrategy = languageStrategyFactory.languageStrategy(language);

        List<Integer> submitTokens = languageStrategy.getTokenInfo(submit);
        List<String> submitNames = languageStrategy.getTokenNames(submitTokens);
        List<String> submitTexts = languageStrategy.getTokenTexts(submitTokens, submit);

        List<Integer> libraryTokens = languageStrategy.getTokenInfo(library);
        List<String> libraryNames = languageStrategy.getTokenNames(libraryTokens);
        List<String> libraryTexts = languageStrategy.getTokenTexts(libraryTokens, library);

        double similarity = calculateSimilarity(language, submit, library, minMatchLength);

        return new SimilarityDetail(similarity, submitNames, libraryNames, submitTexts, libraryTexts);
    }

    /**
     * 相似度详细信息类
     */
    @Setter
    @Getter
    public static class SimilarityDetail {
        private double similarity;

        private String submitUser; // 提交用户
        private String libraryUser; // 样本集用户

        private List<String> submitTokenNames;
        private List<String> libraryTokenNames;
        private List<String> submitTokenTexts;
        private List<String> libraryTokenTexts;

        public SimilarityDetail() {

        }

        public SimilarityDetail(double similarity, List<String> submitTokenNames,
                                List<String> libraryTokenNames, List<String> submitTokenTexts,
                                List<String> libraryTokenTexts) {
            this.similarity = similarity;

            this.submitTokenNames = submitTokenNames;
            this.submitTokenTexts = submitTokenTexts;
            this.libraryTokenNames = libraryTokenNames;
            this.libraryTokenTexts = libraryTokenTexts;
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