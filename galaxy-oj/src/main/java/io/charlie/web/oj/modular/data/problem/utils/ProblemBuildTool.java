package io.charlie.web.oj.modular.data.problem.utils;

import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.core.collection.CollectionUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import io.charlie.web.oj.modular.data.problem.entity.DataProblem;
import io.charlie.web.oj.modular.data.relation.tag.service.DataProblemTagService;
import io.charlie.web.oj.modular.data.solved.entity.DataSolved;
import io.charlie.web.oj.modular.data.solved.entity.ProblemStatistics;
import io.charlie.web.oj.modular.data.solved.mapper.DataSolvedMapper;
import io.charlie.web.oj.modular.data.solved.service.DataSolvedService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author ZhangJiangHu
 * @version v1.0
 * @date 24/09/2025
 * @description 构建题目工具
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ProblemBuildTool {
    private final DataSolvedMapper dataSolvedMapper;
    private final DataProblemTagService dataProblemTagService;

    private final DataSolvedService dataSolvedService;

    public void buildProblems(List<DataProblem> dataProblems) {
        if (CollectionUtil.isEmpty(dataProblems)) {
            return;
        }
        buildProblemsBatch(dataProblems);
    }

    // 保留单个构建方法作为兼容
    public void buildProblem(DataProblem dataProblem) {
        buildProblems(Collections.singletonList(dataProblem));
    }

    private void buildProblemsBatch(List<DataProblem> dataProblems) {
        if (CollectionUtil.isEmpty(dataProblems)) {
            return;
        }

        List<String> problemIds = dataProblems.stream()
                .map(DataProblem::getId)
                .distinct()
                .collect(Collectors.toList());

        // 1. 批量获取当前用户解题状态
        Map<String, Boolean> userSolvedMap = buildUserSolvedStatus(problemIds);

        // 2. 批量获取标签信息
        Map<String, List<String>> tagIdsMap = dataProblemTagService.batchGetTagIdsByIds(problemIds);
        Map<String, List<String>> tagNamesMap = dataProblemTagService.batchGetTagNamesByIds(problemIds);

        // 3. 批量获取题目统计信息
        Map<String, ProblemStatistics> stringProblemStatisticsMap = dataSolvedService.getBatchProblemStatistics(problemIds);

        // 批量设置属性
        for (DataProblem dataProblem : dataProblems) {
            String problemId = dataProblem.getId();

            dataProblem.setCurrentUserSolved(userSolvedMap.getOrDefault(problemId, Boolean.FALSE));
            dataProblem.setTagIds(tagIdsMap.getOrDefault(problemId, Collections.emptyList()));
            dataProblem.setTagNames(tagNamesMap.getOrDefault(problemId, Collections.emptyList()));

            ProblemStatistics statistics = stringProblemStatisticsMap.get(problemId);
            if (statistics != null) {
                dataProblem.setAcceptance(statistics.getAcceptanceRate());
                dataProblem.setParticipantUserCount((long) statistics.getTotalParticipants());
                dataProblem.setSolved((long) statistics.getAcceptedParticipants());
            } else {
                // 设置默认值
                dataProblem.setAcceptance(BigDecimal.ZERO);
                dataProblem.setParticipantUserCount(0L);
                dataProblem.setSolved(0L);
            }
        }
    }

    private Map<String, Boolean> buildUserSolvedStatus(List<String> problemIds) {
        try {
            String loginIdAsString = StpUtil.getLoginIdAsString();

            // 批量查询用户解题状态
            List<DataSolved> solvedList = dataSolvedMapper.selectList(new LambdaQueryWrapper<DataSolved>()
                    .eq(DataSolved::getUserId, loginIdAsString)
                    .in(DataSolved::getProblemId, problemIds)
                    .eq(DataSolved::getModuleType, "PROBLEM")
                    .eq(DataSolved::getSolved, Boolean.TRUE)
            );

            return solvedList.stream()
                    .collect(Collectors.toMap(
                            DataSolved::getProblemId,
                            solved -> true,
                            (existing, replacement) -> existing
                    ));
        } catch (Exception e) {
            return Collections.emptyMap();
        }
    }


    private Map<String, Boolean> buildUserSolvedSetStatus(String setId, List<String> problemIds) {
        try {
            String loginIdAsString = StpUtil.getLoginIdAsString();

            // 批量查询用户解题状态
            List<DataSolved> solvedList = dataSolvedMapper.selectList(new LambdaQueryWrapper<DataSolved>()
                    .eq(DataSolved::getUserId, loginIdAsString)
                    .in(DataSolved::getProblemId, problemIds)
                    .eq(DataSolved::getModuleId, setId)
                    .eq(DataSolved::getModuleType, "PROBLEM")
                    .eq(DataSolved::getSolved, Boolean.TRUE)
            );

            return solvedList.stream()
                    .collect(Collectors.toMap(
                            DataSolved::getProblemId,
                            solved -> true,
                            (existing, replacement) -> existing
                    ));
        } catch (Exception e) {
            return Collections.emptyMap();
        }
    }


    public void buildSetProblems(String setId, List<DataProblem> dataProblems) {
        if (CollectionUtil.isEmpty(dataProblems)) {
            return;
        }
        buildSetProblemsBatch(setId, dataProblems);
    }

    // 保留单个构建方法作为兼容
    public void buildSetProblem(String setId, DataProblem dataProblem) {
        buildSetProblems(setId, Collections.singletonList(dataProblem));
    }

    private void buildSetProblemsBatch(String setId, List<DataProblem> dataProblems) {
        if (CollectionUtil.isEmpty(dataProblems)) {
            return;
        }

        List<String> problemIds = dataProblems.stream()
                .map(DataProblem::getId)
                .distinct()
                .collect(Collectors.toList());

        // 1. 批量获取当前用户解题状态
        Map<String, Boolean> userSolvedMap = buildUserSolvedSetStatus(setId, problemIds);

        // 2. 批量获取标签信息
        Map<String, List<String>> tagIdsMap = dataProblemTagService.batchGetTagIdsByIds(problemIds);
        Map<String, List<String>> tagNamesMap = dataProblemTagService.batchGetTagNamesByIds(problemIds);

        // 3. 批量获取题目统计信息
        Map<String, ProblemStatistics> stringProblemStatisticsMap = dataSolvedService.getBatchSetProblemStatistics(setId, problemIds);

//        Map<String, Double> acceptanceMap = problemSetCacheService.batchGetAcceptRate(setId, problemIds);
//        Map<String, Long> solvedMap = problemSetCacheService.batchGetAcceptCount(setId, problemIds);
//        Map<String, Long> submitUserCountMap = problemSetCacheService.batchGetSubmitCount(setId, problemIds);
//        Map<String, Long> participantCountMap = problemSetCacheService.batchGetParticipantCount(setId, problemIds);

        // 批量设置属性
        for (DataProblem dataProblem : dataProblems) {
            String problemId = dataProblem.getId();

            dataProblem.setCurrentUserSolved(userSolvedMap.getOrDefault(problemId, false));
            dataProblem.setTagIds(tagIdsMap.getOrDefault(problemId, Collections.emptyList()));
            dataProblem.setTagNames(tagNamesMap.getOrDefault(problemId, Collections.emptyList()));

            ProblemStatistics statistics = stringProblemStatisticsMap.get(problemId);
            if (statistics != null) {
                dataProblem.setAcceptance(statistics.getAcceptanceRate());
                dataProblem.setParticipantUserCount((long) statistics.getTotalParticipants());
                dataProblem.setSolved((long) statistics.getAcceptedParticipants());
            } else {
                // 设置默认值
                dataProblem.setAcceptance(BigDecimal.ZERO);
                dataProblem.setParticipantUserCount(0L);
                dataProblem.setSolved(0L);
            }
        }
    }
}
