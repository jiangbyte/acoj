package io.charlie.web.modular.task.similarity.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import io.charlie.cores.exception.BusinessException;
import io.charlie.web.modular.data.library.entity.DataLibrary;
import io.charlie.web.modular.data.library.mapper.DataLibraryMapper;
import io.charlie.web.modular.data.library.param.BatchLibraryQueryParam;
import io.charlie.web.modular.data.library.service.DataLibraryService;
import io.charlie.web.modular.data.problem.entity.DataProblem;
import io.charlie.web.modular.data.problem.mapper.DataProblemMapper;
import io.charlie.web.modular.data.reports.entity.TaskReports;
import io.charlie.web.modular.data.reports.mapper.TaskReportsMapper;
import io.charlie.web.modular.task.similarity.dto.SimilaritySubmitDto;
import io.charlie.web.modular.task.similarity.handle.SimilarityHandleMessage;
import io.charlie.web.modular.task.similarity.service.SimilarityService;
import io.charlie.web.utils.similarity.utils.CodeTokenUtil;
import io.charlie.web.utils.similarity.utils.TokenDetail;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

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
    private final DataLibraryMapper dataLibraryMapper;
    private final DataProblemMapper dataProblemMapper;
    private final CodeTokenUtil codeTokenUtil;

    private final TaskReportsMapper taskReportsMapper;

    @Override
    public String batch(BatchLibraryQueryParam batchSimilarityParam) {
        List<String> list = dataLibraryService.libraryIds(batchSimilarityParam);
        if (ObjectUtil.isEmpty(list)) {
            throw new BusinessException("参数错误");
        }

        DataProblem dataProblem = dataProblemMapper.selectById(batchSimilarityParam.getProblemId());

        String taskId = IdUtil.objectId();

        // 1. 固定 GST 参数唯一来源：请求 minMatchLength，缺省对齐 admin UI 默认 8
        // 2. 写入 task_reports 供详情回显
        Integer minMatchLength = batchSimilarityParam.getMinMatchLength();
        if (minMatchLength == null || minMatchLength < 1) {
            minMatchLength = 8;
        }

        TaskReports taskReports = new TaskReports();
        taskReports.setTaskId(taskId);
        taskReports.setModuleId(batchSimilarityParam.getModuleId());
        taskReports.setProblemId(batchSimilarityParam.getProblemId());
        taskReports.setSampleCount(Math.toIntExact(list.size()));
        taskReports.setModuleType("SET");
        taskReports.setThreshold(dataProblem.getThreshold());
        taskReports.setMinMatchLength(minMatchLength);

        taskReportsMapper.insert(taskReports);

        // 3. 用当前分词规则重写样本 token。库里的旧序列仍含注释换行，不重算的话比对结果不会变
        refreshLibraryTokens(list);

        log.info("开始处理任务: {}, minMatchLength={}", taskId, minMatchLength);

        SimilaritySubmitDto batchSimilaritySubmitDto = BeanUtil.toBean(batchSimilarityParam, SimilaritySubmitDto.class);
        batchSimilaritySubmitDto.setReportId(taskReports.getId());
        batchSimilaritySubmitDto.setTaskId(taskId);
        batchSimilaritySubmitDto.setLibIds(list);
        batchSimilaritySubmitDto.setMinMatchLength(minMatchLength);
        batchSimilaritySubmitDto.setThreshold(dataProblem.getThreshold());
        batchSimilaritySubmitDto.setTaskType(Boolean.TRUE);

        batchSimilarityHandleMessage.sendSimilarity(batchSimilaritySubmitDto);

        return taskReports.getId();
    }

    /**
     * 比对前按当前规则重算样本 token，并只更新有变化的行。
     */
    private void refreshLibraryTokens(List<String> libIds) {
        // 1. 没有样本就不查库
        if (ObjectUtil.isEmpty(libIds)) {
            return;
        }
        List<DataLibrary> libraries = dataLibraryService.listByIds(libIds);
        if (ObjectUtil.isEmpty(libraries)) {
            return;
        }
        // 2. 逐条分词；分词失败或结果为空时保留原 token，避免把可比对样本写成空序列
        for (DataLibrary library : libraries) {
            if (library == null || StrUtil.isBlank(library.getId())
                    || StrUtil.isBlank(library.getLanguage()) || library.getCode() == null) {
                continue;
            }
            try {
                TokenDetail detail = codeTokenUtil.getCodeTokensDetail(
                        library.getLanguage().toLowerCase(), library.getCode());
                if (detail == null || ObjectUtil.isEmpty(detail.getTokens())) {
                    if (StrUtil.isNotBlank(library.getCode())) {
                        log.warn("样本分词结果为空，保留原 token, libId={}", library.getId());
                    }
                    continue;
                }
                if (Objects.equals(library.getCodeToken(), detail.getTokens())
                        && Objects.equals(library.getCodeTokenName(), detail.getTokenNames())
                        && Objects.equals(library.getCodeTokenTexts(), detail.getTokenTexts())) {
                    continue;
                }
                // 3. 只更新 token 三列，沿用实体更新以走 JSON TypeHandler
                DataLibrary patch = new DataLibrary();
                patch.setCodeToken(detail.getTokens());
                patch.setCodeTokenName(detail.getTokenNames());
                patch.setCodeTokenTexts(detail.getTokenTexts());
                dataLibraryMapper.update(patch, new LambdaUpdateWrapper<DataLibrary>()
                        .eq(DataLibrary::getId, library.getId()));
            } catch (Exception e) {
                log.error("重算样本 token 失败, libId={}", library.getId(), e);
            }
        }
    }
}
