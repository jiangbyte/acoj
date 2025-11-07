package io.charlie.web.oj.modular.task.similarity.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.ObjectUtil;
import io.charlie.galaxy.exception.BusinessException;
import io.charlie.web.oj.modular.data.library.param.BatchLibraryQueryParam;
import io.charlie.web.oj.modular.data.library.service.DataLibraryService;
import io.charlie.web.oj.modular.data.problem.entity.DataProblem;
import io.charlie.web.oj.modular.data.problem.mapper.DataProblemMapper;
import io.charlie.web.oj.modular.data.reports.entity.TaskReports;
import io.charlie.web.oj.modular.data.reports.mapper.TaskReportsMapper;
import io.charlie.web.oj.modular.task.similarity.dto.SimilaritySubmitDto;
import io.charlie.web.oj.modular.task.similarity.handle.SimilarityHandleMessage;
import io.charlie.web.oj.modular.task.similarity.service.SimilarityService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author ZhangJiangHu
 * @version v1.0
 * @date 26/09/2025
 * @description 题目相似度
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class SimilarityServiceImpl implements SimilarityService {
    private final SimilarityHandleMessage batchSimilarityHandleMessage;

    private final DataLibraryService dataLibraryService;
    private final DataProblemMapper dataProblemMapper;

    private final TaskReportsMapper taskReportsMapper;

    @Override
    public String batch(BatchLibraryQueryParam batchSimilarityParam) {
        List<String> list = dataLibraryService.libraryIds(batchSimilarityParam);
        if (ObjectUtil.isEmpty(list)) {
            throw new BusinessException("参数错误");
        }

        DataProblem dataProblem = dataProblemMapper.selectById(batchSimilarityParam.getProblemId());

        String taskId = IdUtil.objectId();

        TaskReports taskReports = new TaskReports();
        taskReports.setTaskId(taskId);
        taskReports.setModuleId(batchSimilarityParam.getModuleId());
        taskReports.setProblemId(batchSimilarityParam.getProblemId());
        taskReports.setSampleCount(Math.toIntExact(list.size()));
        taskReports.setModuleType("SET");
        taskReports.setThreshold(dataProblem.getThreshold());

        taskReportsMapper.insert(taskReports);

        log.info("开始处理任务: {}", taskId);

        SimilaritySubmitDto batchSimilaritySubmitDto = BeanUtil.toBean(batchSimilarityParam, SimilaritySubmitDto.class);
        batchSimilaritySubmitDto.setReportId(taskReports.getId());
        batchSimilaritySubmitDto.setTaskId(taskId);
        batchSimilaritySubmitDto.setLibIds(list);
        batchSimilaritySubmitDto.setMinMatchLength(batchSimilarityParam.getMinMatchLength());
        batchSimilaritySubmitDto.setThreshold(dataProblem.getThreshold());
        batchSimilaritySubmitDto.setTaskType(Boolean.TRUE);

        batchSimilarityHandleMessage.sendSimilarity(batchSimilaritySubmitDto);

        return taskReports.getId();
    }
}
