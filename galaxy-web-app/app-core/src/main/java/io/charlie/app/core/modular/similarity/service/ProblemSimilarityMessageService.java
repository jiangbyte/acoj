package io.charlie.app.core.modular.similarity.service;

import io.charlie.app.core.modular.similarity.dto.SimilarityResultDto;
import io.charlie.app.core.modular.similarity.dto.SimilaritySubmitDto;

/**
 * @author ZhangJiangHu
 * @version v1.0
 * @date 11/08/2025
 * @description 克隆检测服务
 */
public interface ProblemSimilarityMessageService {
    /**
     * 发送克隆检测请求消息
     */
    void sendSimilarityRequest(SimilaritySubmitDto similaritySubmitDto);

    void handleSimilarityRequest(SimilaritySubmitDto similaritySubmitDto);

    void sendSimilarityResult(SimilarityResultDto similarityResultDto);

    /**
     * 处理克隆检测结果消息
     */
    void handleSimilarityResult(SimilarityResultDto similarityResultDto);
}
