package io.charlie.app.core.modular.similarity.factory;

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
    private final Map<String, LanguageStrategy> strategyMap;

    public LanguageStrategy languageStrategy(String language) {
        String strategyKey = language.toLowerCase() + "LanguageStrategy";
        if (!strategyMap.containsKey(strategyKey)) {
            throw new IllegalArgumentException("No language strategy found for: " + language);
        }
        return strategyMap.get(strategyKey);
    }

    public boolean hasLanguageStrategy(String language) {
        String strategyKey = language.toLowerCase() + "LanguageStrategy";
        return strategyMap.containsKey(strategyKey);
    }

}
