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
    private final DataProblemTagService dataProblemTagService;
    private final TransService transService;

    private final UserCacheService userCacheService;
    private final ProblemSetCacheService problemSetCacheService;
    private final ProblemCacheService problemCacheService;

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
            boolean exists = dataSolvedMapper.exists(new LambdaQueryWrapper<DataSolved>()
                    .eq(DataSolved::getUserId, loginIdAsString)
                    .eq(DataSolved::getProblemId, dataProblem.getId())
                    .eq(DataSolved::getIsSet, Boolean.FALSE)
                    .eq(DataSolved::getSolved, Boolean.TRUE)
            );
            dataProblem.setCurrentUserSolved(exists);
        } catch (Exception e) {
            dataProblem.setCurrentUserSolved(false);
        }

        // 标签设置
        dataProblem.setTagIds(dataProblemTagService.getTagIdsById(dataProblem.getId()));
        dataProblem.setTagNames(dataProblemTagService.getTagNamesById(dataProblem.getId()));

        // 通过率
        dataProblem.setAcceptance(problemCacheService.getAcceptRate(dataProblem.getId()));
        // 通过人数
        dataProblem.setSolved(problemCacheService.getAcceptCount(dataProblem.getId()));
        // 提交人数
        dataProblem.setSubmitUserCount(problemCacheService.getSubmitCount(dataProblem.getId()));
        // 参与人数
        dataProblem.setParticipantUserCount(problemCacheService.getParticipantCount(dataProblem.getId()));
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
            boolean exists = dataSolvedMapper.exists(new LambdaQueryWrapper<DataSolved>()
                    .eq(DataSolved::getUserId, loginIdAsString)
                    .eq(DataSolved::getProblemId, dataProblem.getId())
                    .eq(DataSolved::getIsSet, Boolean.TRUE)
                    .eq(DataSolved::getSolved, Boolean.TRUE)
            );
            dataProblem.setCurrentUserSolved(exists);
        } catch (Exception e) {
            dataProblem.setCurrentUserSolved(false);
        }

        // 标签设置
        dataProblem.setTagIds(dataProblemTagService.getTagIdsById(dataProblem.getId()));
        dataProblem.setTagNames(dataProblemTagService.getTagNamesById(dataProblem.getId()));

        ProblemSetProblemStats problemSetProblemStats = problemSetCacheService.getProblemSetProblemStats(dataProblem.getId(), dataProblem.getId());
        dataProblem.setAcceptance(problemSetProblemStats.getAcceptRate());
        dataProblem.setSolved(problemSetProblemStats.getAcceptCount());
        // 提交人数
        dataProblem.setSubmitUserCount(problemSetProblemStats.getSubmitCount());
        // 参与人数
        dataProblem.setParticipantUserCount(problemSetProblemStats.getParticipantCount());
    }
}
