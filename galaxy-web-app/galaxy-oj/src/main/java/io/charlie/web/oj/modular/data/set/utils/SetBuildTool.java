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
import io.charlie.web.oj.modular.data.solved.entity.ProblemStatistics;
import io.charlie.web.oj.modular.data.solved.entity.SetStatistics;
import io.charlie.web.oj.modular.data.solved.service.DataSolvedService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.Date;
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

    private final DataSolvedService dataSolvedService;

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

        Map<String, SetStatistics> batchSetStatistics = dataSolvedService.getBatchSetStatistics(setIds);

//        Map<String, Long> totalSubmitCountMap = problemSetCacheService.getBatchProblemSetTotalSubmitCount(setIds);
//        Map<String, Double> avgAcceptanceMap = problemSetCacheService.getBatchProblemSetAverageAcceptRate(setIds);
//        Map<String, Long> participantUserCountMap = problemSetCacheService.getBatchProblemSetParticipantCount(setIds);


        for (DataSet dataSet : dataSets) {
            String setId = dataSet.getId();
            dataSet.setProblemIds(problemIdsMap.getOrDefault(setId, Collections.emptyList()));

            SetStatistics statistics = batchSetStatistics.get(setId);
            if (statistics != null) {
                dataSet.setAvgAcceptance(statistics.getAcceptanceRate());
                dataSet.setParticipantUserCount((long) statistics.getTotalParticipants());
//                dataSet.setSolved((long) statistics.getAcceptedParticipants());
                dataSet.setSubmitCount((long) statistics.getSubmitCount());
            } else {
                // 设置默认值
                dataSet.setAvgAcceptance(BigDecimal.ZERO);
                dataSet.setParticipantUserCount(0L);
//                dataProblem.setSolved(0L);
                dataSet.setSubmitCount(0L);
            }

            // 限时题集
            if (dataSet.getSetType().equals(2)) {
                // 时间状态计算
                Date now = new Date();
                // 只有当开始时间和结束时间都存在时才计算
                if (dataSet.getStartTime() != null && dataSet.getEndTime() != null) {
                    if (now.before(dataSet.getStartTime())) {
                        dataSet.setTimeStatus(1);  // 未开始
                    } else if (now.after(dataSet.getEndTime())) {
                        dataSet.setTimeStatus(3);  // 已结束
                    } else {
                        dataSet.setTimeStatus(2); // 进行中
                    }
                } else {
                    // 如果时间不完整，可以设置一个默认状态或保持null
                    dataSet.setTimeStatus(0); // 或 null，表示时间状态未知
                }
            }

            // 提交数
//            dataSet.setSubmitCount(totalSubmitCountMap.getOrDefault(setId, 0L));
//            // 通过率
//            dataSet.setAvgAcceptance(avgAcceptanceMap.getOrDefault(setId, 0D));
//            // 参与人数
//            dataSet.setParticipantUserCount(participantUserCountMap.getOrDefault(setId, 0L));
        }
    }

}
