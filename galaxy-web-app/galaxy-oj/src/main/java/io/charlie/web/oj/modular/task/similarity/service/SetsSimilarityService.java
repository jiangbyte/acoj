package io.charlie.web.oj.modular.task.similarity.service;

import io.charlie.web.oj.modular.task.similarity.param.BatchSimilarityParam;

/**
 * @author ZhangJiangHu
 * @version v1.0
 * @date 26/09/2025
 * @description 题集相似度
 */
public interface SetsSimilarityService {
    void batch(BatchSimilarityParam batchSimilarityParam);
}
