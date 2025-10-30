package io.charlie.web.oj.utils.similarity.utils;

import cn.hutool.core.util.StrUtil;
import io.charlie.web.oj.utils.similarity.algorithm.GSTv1;
import io.charlie.web.oj.utils.similarity.algorithm.GSTv2;
import io.charlie.web.oj.utils.similarity.factory.LanguageStrategy;
import io.charlie.web.oj.utils.similarity.factory.LanguageStrategyFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.List;

/**
 * 代码相似度计算工具类
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class SimilarityCalculator {
    private final LanguageStrategyFactory languageStrategyFactory;

    private LanguageStrategy languageStrategy;

    /**
     * 计算两个代码的相似度
     *
     * @param submit         第一个代码
     * @param library        第二个代码
     * @param minMatchLength 控制匹配敏感度
     * @return 相似度百分比 (0.0 - 1.0)
     */
    public double calculate(String language, String submit, String library, int minMatchLength) {
        if (StrUtil.isBlank(submit) || StrUtil.isBlank(library)) return 0.0;

        languageStrategy = languageStrategyFactory.languageStrategy(language);

        List<Integer> submitTokens = languageStrategy.getTokenInfo(submit);
        List<Integer> libraryTokens = languageStrategy.getTokenInfo(library);

        if (submitTokens.isEmpty() || libraryTokens.isEmpty()) return 0.0;

        int matches = GSTv1.greedyStringTiling(submitTokens, libraryTokens, minMatchLength);

        double similarity = (double) (matches * 2) / (submitTokens.size() + libraryTokens.size());

        return Double.parseDouble(new DecimalFormat("0.00").format(similarity));
    }

    public BigDecimal calculateBigDecimal(List<Integer> submitTokens, List<Integer> libraryTokens, int minMatchLength) {
        int matches = GSTv1.greedyStringTiling(submitTokens, libraryTokens, minMatchLength);
        if (matches == 0) return BigDecimal.ZERO;

        BigDecimal matchesBig = BigDecimal.valueOf(matches * 2L);
        BigDecimal submitSize = BigDecimal.valueOf(submitTokens.size());
        BigDecimal librarySize = BigDecimal.valueOf(libraryTokens.size());

        BigDecimal denominator = submitSize.add(librarySize);
        BigDecimal similarity = matchesBig.divide(denominator, 4, RoundingMode.HALF_UP);
        return similarity.setScale(2, RoundingMode.HALF_UP);
    }

    public BigDecimal calculateBigDecimalFinal(List<Integer> submitTokens, List<Integer> libraryTokens, int minMatchLength) {
        int matches = GSTv2.greedyStringTilingFinal(submitTokens, libraryTokens, minMatchLength);
        if (matches == 0) return BigDecimal.ZERO;

        BigDecimal matchesBig = BigDecimal.valueOf(matches * 2L);
        BigDecimal submitSize = BigDecimal.valueOf(submitTokens.size());
        BigDecimal librarySize = BigDecimal.valueOf(libraryTokens.size());

        BigDecimal denominator = submitSize.add(librarySize);
        BigDecimal similarity = matchesBig.divide(denominator, 4, RoundingMode.HALF_UP);
        return similarity.setScale(2, RoundingMode.HALF_UP);
    }

    /**
     * 获取详细的Token匹配信息
     *
     * @param submit  第一个代码
     * @param library 第二个代码
     * @return 包含匹配详细信息的对象
     */
    public SimilarityDetail getCalculatorDetail(String language, String submit, String library, int minMatchLength) {
        languageStrategy = languageStrategyFactory.languageStrategy(language);

        List<Integer> submitTokens = languageStrategy.getTokenInfo(submit);
        List<String> submitNames = languageStrategy.getTokenNames(submitTokens);
        List<String> submitTexts = languageStrategy.getTokenTexts(submitTokens, submit);

        List<Integer> libraryTokens = languageStrategy.getTokenInfo(library);
        List<String> libraryNames = languageStrategy.getTokenNames(libraryTokens);
        List<String> libraryTexts = languageStrategy.getTokenTexts(libraryTokens, library);

        double similarity = calculate(language, submit, library, minMatchLength);

        return new SimilarityDetail(similarity, submitNames, libraryNames, submitTexts, libraryTexts);
    }
}