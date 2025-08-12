package io.charlie.app.core.modular.similarity.factory;

import java.util.List;

/**
 * @author ZhangJiangHu
 * @version v1.0
 * @date 11/08/2025
 * @description 语言策略
 */
public interface LanguageStrategy {
    List<Integer> getTokenInfo(String code);

    List<String> getTokenNames(List<Integer> tokenValues);

    List<String> getTokenTexts(List<Integer> tokenValues, String code);
}
