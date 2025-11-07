package io.charlie.web.oj.utils.similarity.factory;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * @author ZhangJiangHu
 * @version v1.0
 * @date 11/08/2025
 * @description 语言策略工厂
 */
@Component
@RequiredArgsConstructor
public class LanguageStrategyFactory {
    private final Map<String, LanguageStrategy> languageStrategyMap;
    public LanguageStrategy languageStrategy(String language) {
        String strategyKey = language.toLowerCase() + "LanguageStrategy";
        if (!languageStrategyMap.containsKey(strategyKey)) {
            throw new IllegalArgumentException("No language strategy found for: " + language);
        }
        return languageStrategyMap.get(strategyKey);
    }
}
