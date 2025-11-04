package io.charlie.web.oj.utils.similarity.utils;

import io.charlie.web.oj.utils.similarity.factory.LanguageStrategy;
import io.charlie.web.oj.utils.similarity.factory.LanguageStrategyFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * @author ZhangJiangHu
 * @version v1.0
 * @date 30/10/2025
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class CodeTokenUtil {
    private final LanguageStrategyFactory languageStrategyFactory;

    private LanguageStrategy languageStrategy;

    public List<String> getCodeTokensNames(String language, String code) {
        languageStrategy = languageStrategyFactory.languageStrategy(language);
        return languageStrategy.getTokenNames(languageStrategy.getTokenInfo(code));
    }

    public List<String> getCodeTokensTexts(String language, String code) {
        languageStrategy = languageStrategyFactory.languageStrategy(language);
        return languageStrategy.getTokenTexts(languageStrategy.getTokenInfo(code), code);
    }

    public List<Integer> getCodeTokens(String language, String code) {
        languageStrategy = languageStrategyFactory.languageStrategy(language);
        return languageStrategy.getTokenInfo(code);
    }

    public TokenDetail getCodeTokensDetail(String language, String code) {
        languageStrategy = languageStrategyFactory.languageStrategy(language);
        TokenDetail tokenDetail = new TokenDetail();
        tokenDetail.setTokens(languageStrategy.getTokenInfo(code));
        tokenDetail.setTokenNames(languageStrategy.getTokenNames(tokenDetail.getTokens()));
        tokenDetail.setTokenTexts(languageStrategy.getTokenTexts(tokenDetail.getTokens(), code));
        return tokenDetail;
    }
}

