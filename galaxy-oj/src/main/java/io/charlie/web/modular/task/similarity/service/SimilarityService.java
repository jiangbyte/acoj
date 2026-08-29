package io.charlie.web.modular.task.similarity.service;

import io.charlie.web.modular.data.library.param.BatchLibraryQueryParam;

/**
 * @author ZhangJiangHu
 * @version v1.0
 * @date 26/09/2025
 * @description 题目相似度
 */
public interface SimilarityService {
    String batch(BatchLibraryQueryParam batchSimilarityParam);
}
