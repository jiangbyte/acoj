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

import java.util.List;

/**
 * @author ZhangJiangHu
 * @version v1.0
 * @date 01/10/2025
 * @description TODO
 */@Component
@RequiredArgsConstructor
public class SetBuildTool {
    private final UserCacheService userCacheService;
    private final ProblemSetCacheService problemSetCacheService;
    private final ProblemCacheService problemCacheService;
    private final DataSetProblemService dataSetProblemService;

    public void buildSets(List<DataSet> dataSets) {
        if (CollectionUtil.isEmpty(dataSets)) {
            return;
        }
        dataSets.forEach(this::buildSet);
    }

    public void  buildSet(DataSet dataSet) {
        dataSet.setProblemIds(dataSetProblemService.getProblemIdsBySetId(dataSet.getId()));

        // 提交数
        dataSet.setSubmitCount(problemSetCacheService.getProblemSetTotalSubmitCount(dataSet.getId()));
        // 通过率
        dataSet.setAvgAcceptance(problemSetCacheService.getProblemSetAverageAcceptRate(dataSet.getId()));
        // 参与人数
        dataSet.setParticipantUserCount(problemSetCacheService.getProblemSetParticipantCount(dataSet.getId()));
    }
}
