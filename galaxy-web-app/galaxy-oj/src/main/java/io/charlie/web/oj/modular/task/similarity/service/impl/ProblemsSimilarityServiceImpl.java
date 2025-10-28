package io.charlie.web.oj.modular.task.similarity.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.ObjectUtil;
import io.charlie.galaxy.exception.BusinessException;
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
    public String batch(BatchSimilarityParam batchSimilarityParam) {
        if (ObjectUtil.isNotEmpty(batchSimilarityParam.getUserIds())) {
            if (batchSimilarityParam.getUserIds().size() <= 1) {
                throw new BusinessException("用户数量不能小于2");
            }
        }

        String taskId = IdUtil.objectId();
        log.info("开始处理任务: {}", taskId);
        BatchSimilaritySubmitDto batchSimilaritySubmitDto = BeanUtil.toBean(batchSimilarityParam, BatchSimilaritySubmitDto.class);
        batchSimilaritySubmitDto.setTaskType(Boolean.TRUE);
        batchSimilaritySubmitDto.setTaskId(taskId);
        batchSimilarityHandleMessage.sendSimilarity(batchSimilaritySubmitDto);
        return taskId;
    }
}
