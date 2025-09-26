package io.charlie.web.oj.modular.task.similarity.service.impl;

import cn.hutool.core.bean.BeanUtil;
import io.charlie.web.oj.modular.task.similarity.dto.BatchSimilaritySubmitDto;
import io.charlie.web.oj.modular.task.similarity.enums.ReportTypeEnum;
import io.charlie.web.oj.modular.task.similarity.handle.BatchSimilarityHandleMessage;
import io.charlie.web.oj.modular.task.similarity.param.BatchSimilarityParam;
import io.charlie.web.oj.modular.task.similarity.service.ProblemsSimilarityService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * @author ZhangJiangHu
 * @version v1.0
 * @date 26/09/2025
 * @description 题目相似度
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ProblemsSimilarityServiceImpl implements ProblemsSimilarityService {
    private final BatchSimilarityHandleMessage batchSimilarityHandleMessage;

    @Override
    public void batch(BatchSimilarityParam batchSimilarityParam) {
        BatchSimilaritySubmitDto bean = BeanUtil.toBean(batchSimilarityParam, BatchSimilaritySubmitDto.class);
        bean.setTaskType(Boolean.TRUE);
        batchSimilarityHandleMessage.sendSimilarity(bean);
    }
}
