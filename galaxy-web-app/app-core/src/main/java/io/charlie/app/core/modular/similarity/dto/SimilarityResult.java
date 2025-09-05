package io.charlie.app.core.modular.similarity.dto;

import io.charlie.app.core.modular.similarity.utils.CodeSimilarityCalculator;
import lombok.Data;

import java.util.List;

/**
 * @author ZhangJiangHu
 * @version v1.0
 * @date 11/08/2025
 * @description 相似度结果类
 */
@Data
public class SimilarityResult {
    private List<CodeSimilarityCalculator.SimilarityDetail> similarityDetails;
    private CodeSimilarityCalculator.SimilarityDetail maxSimilarity;
}
