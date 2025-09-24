package io.charlie.web.oj.modular.data.problem.utils;

import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.core.collection.CollectionUtil;
import io.charlie.galaxy.utils.ranking.RankingUtil;
import io.charlie.web.oj.modular.data.problem.entity.DataProblem;
import io.charlie.web.oj.modular.data.ranking.service.SetUserRankingService;
import io.charlie.web.oj.modular.data.ranking.service.UserRankingService;
import io.charlie.web.oj.modular.data.relation.tag.service.DataProblemTagService;
import io.charlie.web.oj.modular.data.solved.mapper.DataSolvedMapper;
import lombok.RequiredArgsConstructor;
import org.dromara.trans.service.impl.TransService;
import org.springframework.stereotype.Component;

import java.util.List;

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
    private final RankingUtil rankingUtil;
    private final UserRankingService userRankingService;
    private final SetUserRankingService setUserRankingService;
    private final DataProblemTagService dataProblemTagService;
    private final TransService transService;

    public void buildProblems(List<DataProblem> dataProblems) {
        if (CollectionUtil.isEmpty(dataProblems)) {
            return;
        }
        dataProblems.forEach(this::buildProblem);
    }

    public void buildProblem(DataProblem dataProblem) {
        // 是否解决
        try {
            String loginIdAsString = StpUtil.getLoginIdAsString();
            Boolean solved = userRankingService.isSolved(loginIdAsString, dataProblem.getId());
            dataProblem.setCurrentUserSolved(solved);
        } catch (Exception e) {
            dataProblem.setCurrentUserSolved(false);
        }

        // 标签设置
        dataProblem.setTagIds(dataProblemTagService.getTagIdsById(dataProblem.getId()));
        dataProblem.setTagNames(dataProblemTagService.getTagNamesById(dataProblem.getId()));
    }

    public void buildSetProblems(String setId, List<DataProblem> dataProblems) {
        if (CollectionUtil.isEmpty(dataProblems)) {
            return;
        }
        dataProblems.forEach(dataProblem -> buildSetProblem(setId, dataProblem));
    }

    public void buildSetProblem(String setId, DataProblem dataProblem) {
        // 是否解决
        try {
            String loginIdAsString = StpUtil.getLoginIdAsString();
            Boolean solved = setUserRankingService.isSolved(setId, loginIdAsString, dataProblem.getId());
            dataProblem.setCurrentUserSolved(solved);
        } catch (Exception e) {
            dataProblem.setCurrentUserSolved(false);
        }

        // 标签设置
        dataProblem.setTagIds(dataProblemTagService.getTagIdsById(dataProblem.getId()));
        dataProblem.setTagNames(dataProblemTagService.getTagNamesById(dataProblem.getId()));
    }
}
