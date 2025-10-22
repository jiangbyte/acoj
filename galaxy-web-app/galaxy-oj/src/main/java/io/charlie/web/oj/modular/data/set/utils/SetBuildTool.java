package io.charlie.web.oj.modular.data.set.utils;

import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.core.collection.CollectionUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import io.charlie.web.oj.modular.data.problem.entity.DataProblem;
import io.charlie.web.oj.modular.data.ranking.service.ProblemCacheService;
import io.charlie.web.oj.modular.data.ranking.service.ProblemSetCacheService;
import io.charlie.web.oj.modular.data.ranking.service.UserCacheService;
import io.charlie.web.oj.modular.data.relation.set.service.DataSetProblemService;
import io.charlie.web.oj.modular.data.set.entity.DataSet;
import io.charlie.web.oj.modular.data.solved.entity.DataSolved;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * @author ZhangJiangHu
 * @version v1.0
 * @date 01/10/2025
 * @description TODO
 */
@Component
@RequiredArgsConstructor
public class SetBuildTool {
    private final ProblemSetCacheService problemSetCacheService;
    private final DataSetProblemService dataSetProblemService;

    public void buildSets(List<DataSet> dataSets) {
        if (CollectionUtil.isEmpty(dataSets)) {
            return;
        }
        buildSetsBatch(dataSets);
    }

    public void buildSet(DataSet dataSet) {
        buildSetsBatch(Collections.singletonList(dataSet));
    }

    public void buildSetsBatch(List<DataSet> dataSets) {
        if (CollectionUtil.isEmpty(dataSets)) {
            return;
        }

        // 1. 批量获取当前题集的所有题目ID
        List<String> setIds = dataSets.stream()
                .map(DataSet::getId)
                .distinct()
                .toList();
        Map<String, List<String>> problemIdsMap = dataSetProblemService.getProblemIdsBySetIds(setIds);

        Map<String, Long> totalSubmitCountMap = problemSetCacheService.getBatchProblemSetTotalSubmitCount(setIds);
        Map<String, Double> avgAcceptanceMap = problemSetCacheService.getBatchProblemSetAverageAcceptRate(setIds);
        Map<String, Long> participantUserCountMap = problemSetCacheService.getBatchProblemSetParticipantCount(setIds);


        for (DataSet dataSet : dataSets) {
            String id = dataSet.getId();
            dataSet.setProblemIds(problemIdsMap.getOrDefault(id, Collections.emptyList()));

            // 提交数
            dataSet.setSubmitCount(totalSubmitCountMap.getOrDefault(id, 0L));
            // 通过率
            dataSet.setAvgAcceptance(avgAcceptanceMap.getOrDefault(id, 0D));
            // 参与人数
            dataSet.setParticipantUserCount(participantUserCountMap.getOrDefault(id, 0L));
        }
    }

}
