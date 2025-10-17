package io.charlie.web.oj.modular.data.problem.utils;

import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.core.collection.CollectionUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import io.charlie.web.oj.modular.data.ranking.data.ProblemSetProblemStats;
import io.charlie.web.oj.modular.data.ranking.service.ProblemCacheService;
import io.charlie.web.oj.modular.data.ranking.service.ProblemSetCacheService;
import io.charlie.web.oj.modular.data.ranking.service.UserCacheService;
import io.charlie.web.oj.modular.data.problem.entity.DataProblem;
import io.charlie.web.oj.modular.data.relation.tag.service.DataProblemTagService;
import io.charlie.web.oj.modular.data.solved.entity.DataSolved;
import io.charlie.web.oj.modular.data.solved.mapper.DataSolvedMapper;
import lombok.RequiredArgsConstructor;
import org.dromara.trans.service.impl.TransService;
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
@Component
@RequiredArgsConstructor
public class ProblemBuildTool {
    private final DataSolvedMapper dataSolvedMapper;
    private final DataProblemTagService dataProblemTagService;
    private final TransService transService;

    private final UserCacheService userCacheService;
    private final ProblemSetCacheService problemSetCacheService;
    private final ProblemCacheService problemCacheService;

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
        Map<String, Double> acceptanceMap = problemCacheService.batchGetAcceptRate(problemIds);
        Map<String, Long> solvedMap = problemCacheService.batchGetAcceptCount(problemIds);
        Map<String, Long> submitUserCountMap = problemCacheService.batchGetSubmitCount(problemIds);
        Map<String, Long> participantCountMap = problemCacheService.batchGetParticipantCount(problemIds);

        // 批量设置属性
        for (DataProblem dataProblem : dataProblems) {
            String problemId = dataProblem.getId();

            dataProblem.setCurrentUserSolved(userSolvedMap.getOrDefault(problemId, false));
            dataProblem.setTagIds(tagIdsMap.getOrDefault(problemId, Collections.emptyList()));
            dataProblem.setTagNames(tagNamesMap.getOrDefault(problemId, Collections.emptyList()));
            dataProblem.setAcceptance(BigDecimal.valueOf(acceptanceMap.getOrDefault(problemId, 0.0)));
            dataProblem.setSolved(solvedMap.getOrDefault(problemId, 0L));
            dataProblem.setSubmitUserCount(submitUserCountMap.getOrDefault(problemId, 0L));
            dataProblem.setParticipantUserCount(participantCountMap.getOrDefault(problemId, 0L));
        }
    }

    private Map<String, Boolean> buildUserSolvedStatus(List<String> problemIds) {
        try {
            String loginIdAsString = StpUtil.getLoginIdAsString();

            // 批量查询用户解题状态
            List<DataSolved> solvedList = dataSolvedMapper.selectList(new LambdaQueryWrapper<DataSolved>()
                    .eq(DataSolved::getUserId, loginIdAsString)
                    .in(DataSolved::getProblemId, problemIds)
                    .eq(DataSolved::getIsSet, Boolean.FALSE)
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
                    .eq(DataSolved::getSetId, setId)
                    .eq(DataSolved::getIsSet, Boolean.TRUE)
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

//    public void buildSetProblem(String setId, DataProblem dataProblem) {
//        // 是否解决
//        try {
//            String loginIdAsString = StpUtil.getLoginIdAsString();
//            boolean exists = dataSolvedMapper.exists(new LambdaQueryWrapper<DataSolved>()
//                    .eq(DataSolved::getUserId, loginIdAsString)
//                    .eq(DataSolved::getProblemId, dataProblem.getId())
//                    .eq(DataSolved::getIsSet, Boolean.TRUE)
//                    .eq(DataSolved::getSolved, Boolean.TRUE)
//            );
//            dataProblem.setCurrentUserSolved(exists);
//        } catch (Exception e) {
//            dataProblem.setCurrentUserSolved(false);
//        }
//
//        // 标签设置
//        dataProblem.setTagIds(dataProblemTagService.getTagIdsById(dataProblem.getId()));
//        dataProblem.setTagNames(dataProblemTagService.getTagNamesById(dataProblem.getId()));
//
//        // 通过率
//        dataProblem.setAcceptance(BigDecimal.valueOf(problemSetCacheService.getProblemAcceptRate(setId, dataProblem.getId())));
//        dataProblem.setSolved(problemSetCacheService.getProblemAcceptCount(setId, dataProblem.getId()));
//
//        ProblemSetProblemStats problemSetProblemStats = problemSetCacheService.getProblemSetProblemStats(dataProblem.getId(), dataProblem.getId());
//        // 提交人数
//        dataProblem.setSubmitUserCount(problemSetProblemStats.getSubmitCount());
//        // 参与人数
//        dataProblem.setParticipantUserCount(problemSetProblemStats.getParticipantCount());
//    }

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
        Map<String, Double> acceptanceMap = problemSetCacheService.batchGetAcceptRate(setId, problemIds);
        Map<String, Long> solvedMap = problemSetCacheService.batchGetAcceptCount(setId, problemIds);
        Map<String, Long> submitUserCountMap = problemSetCacheService.batchGetSubmitCount(setId, problemIds);
        Map<String, Long> participantCountMap = problemSetCacheService.batchGetParticipantCount(setId, problemIds);

        // 批量设置属性
        for (DataProblem dataProblem : dataProblems) {
            String problemId = dataProblem.getId();

            dataProblem.setCurrentUserSolved(userSolvedMap.getOrDefault(problemId, false));
            dataProblem.setTagIds(tagIdsMap.getOrDefault(problemId, Collections.emptyList()));
            dataProblem.setTagNames(tagNamesMap.getOrDefault(problemId, Collections.emptyList()));

            dataProblem.setAcceptance(BigDecimal.valueOf(acceptanceMap.getOrDefault(problemId, 0.0)));
            dataProblem.setSolved(solvedMap.getOrDefault(problemId, 0L));
            dataProblem.setSubmitUserCount(submitUserCountMap.getOrDefault(problemId, 0L));
            dataProblem.setParticipantUserCount(participantCountMap.getOrDefault(problemId, 0L));
        }
    }
}
