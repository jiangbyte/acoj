package io.charlie.app.core.modular.problem.ranking.service.impl;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import io.charlie.app.core.modular.problem.ranking.entity.ProblemUserRanking;
import io.charlie.app.core.modular.problem.ranking.mapper.ProblemUserRankingMapper;
import io.charlie.app.core.modular.problem.ranking.param.ProblemUserRankingPageParam;
import io.charlie.app.core.modular.problem.ranking.service.ProblemUserRankingService;
import io.charlie.galaxy.enums.ISortOrderEnum;
import io.charlie.galaxy.pojo.CommonPageRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

/**
 * @author Charlie Zhang
 * @version v1.0
 * @date 28/07/2025
 * @description 提交排行榜服务实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ProblemUserRankingServiceImpl extends ServiceImpl<ProblemUserRankingMapper, ProblemUserRanking> implements ProblemUserRankingService {
    @Override
    public Page<ProblemUserRanking> totalRankingPage(ProblemUserRankingPageParam problemRankingPageParam) {
        QueryWrapper<ProblemUserRanking> queryWrapper = new QueryWrapper<ProblemUserRanking>().checkSqlInjection();
        // 关键字
        if (ObjectUtil.isNotEmpty(problemRankingPageParam.getKeyword())) {
            queryWrapper.lambda().like(ProblemUserRanking::getNickname, problemRankingPageParam.getKeyword());
        }

        if (ObjectUtil.isAllNotEmpty(problemRankingPageParam.getSortField(), problemRankingPageParam.getSortOrder()) && ISortOrderEnum.isValid(problemRankingPageParam.getSortOrder())) {
            queryWrapper.orderBy(
                    true,
                    problemRankingPageParam.getSortOrder().equals(ISortOrderEnum.ASCEND.getValue()),
                    StrUtil.toUnderlineCase(problemRankingPageParam.getSortField()));
        }

        return (Page<ProblemUserRanking>) this.baseMapper.selectTotalRankingPage(CommonPageRequest.Page(
                Optional.ofNullable(problemRankingPageParam.getCurrent()).orElse(1),
                Optional.ofNullable(problemRankingPageParam.getSize()).orElse(20),
                null
        ), queryWrapper);
    }

    @Override
    public Page<ProblemUserRanking> monthlyRankingPage(ProblemUserRankingPageParam problemRankingPageParam) {
        QueryWrapper<ProblemUserRanking> queryWrapper = new QueryWrapper<ProblemUserRanking>().checkSqlInjection();
        // 关键字
        if (ObjectUtil.isNotEmpty(problemRankingPageParam.getKeyword())) {
            queryWrapper.lambda().like(ProblemUserRanking::getNickname, problemRankingPageParam.getKeyword());
        }

        if (ObjectUtil.isAllNotEmpty(problemRankingPageParam.getSortField(), problemRankingPageParam.getSortOrder()) && ISortOrderEnum.isValid(problemRankingPageParam.getSortOrder())) {
            queryWrapper.orderBy(
                    true,
                    problemRankingPageParam.getSortOrder().equals(ISortOrderEnum.ASCEND.getValue()),
                    StrUtil.toUnderlineCase(problemRankingPageParam.getSortField()));
        }

        return (Page<ProblemUserRanking>) this.baseMapper.selectMonthlyRankingPage(CommonPageRequest.Page(
                Optional.ofNullable(problemRankingPageParam.getCurrent()).orElse(1),
                Optional.ofNullable(problemRankingPageParam.getSize()).orElse(20),
                null
        ), queryWrapper);
    }

    @Override
    public Page<ProblemUserRanking> weeklyRankingPage(ProblemUserRankingPageParam problemRankingPageParam) {
        QueryWrapper<ProblemUserRanking> queryWrapper = new QueryWrapper<ProblemUserRanking>().checkSqlInjection();
        // 关键字
        if (ObjectUtil.isNotEmpty(problemRankingPageParam.getKeyword())) {
            queryWrapper.lambda().like(ProblemUserRanking::getNickname, problemRankingPageParam.getKeyword());
        }

        if (ObjectUtil.isAllNotEmpty(problemRankingPageParam.getSortField(), problemRankingPageParam.getSortOrder()) && ISortOrderEnum.isValid(problemRankingPageParam.getSortOrder())) {
            queryWrapper.orderBy(
                    true,
                    problemRankingPageParam.getSortOrder().equals(ISortOrderEnum.ASCEND.getValue()),
                    StrUtil.toUnderlineCase(problemRankingPageParam.getSortField()));
        }

        return (Page<ProblemUserRanking>) this.baseMapper.selectWeeklyRankingPage(CommonPageRequest.Page(
                Optional.ofNullable(problemRankingPageParam.getCurrent()).orElse(1),
                Optional.ofNullable(problemRankingPageParam.getSize()).orElse(20),
                null
        ), queryWrapper);
    }

    @Override
    public Page<ProblemUserRanking> dailyRankingPage(ProblemUserRankingPageParam problemRankingPageParam) {
        QueryWrapper<ProblemUserRanking> queryWrapper = new QueryWrapper<ProblemUserRanking>().checkSqlInjection();
        // 关键字
        if (ObjectUtil.isNotEmpty(problemRankingPageParam.getKeyword())) {
            queryWrapper.lambda().like(ProblemUserRanking::getNickname, problemRankingPageParam.getKeyword());
        }

        if (ObjectUtil.isAllNotEmpty(problemRankingPageParam.getSortField(), problemRankingPageParam.getSortOrder()) && ISortOrderEnum.isValid(problemRankingPageParam.getSortOrder())) {
            queryWrapper.orderBy(
                    true,
                    problemRankingPageParam.getSortOrder().equals(ISortOrderEnum.ASCEND.getValue()),
                    StrUtil.toUnderlineCase(problemRankingPageParam.getSortField()));
        }

        return (Page<ProblemUserRanking>) this.baseMapper.selectDailyRankingPage(CommonPageRequest.Page(
                Optional.ofNullable(problemRankingPageParam.getCurrent()).orElse(1),
                Optional.ofNullable(problemRankingPageParam.getSize()).orElse(20),
                null
        ), queryWrapper);
    }

    @Override
    public ProblemUserRanking totalRankingByUserId(String userId) {
        return this.baseMapper.selectTotalRankingByUserId(userId);
    }

    @Override
    public ProblemUserRanking monthlyRankingByUserId(String userId) {
        return this.baseMapper.selectMonthlyRankingByUserId(userId);
    }

    @Override
    public ProblemUserRanking weeklyRankingByUserId(String userId) {
        return this.baseMapper.selectWeeklyRankingByUserId(userId);
    }

    @Override
    public ProblemUserRanking dailyRankingByUserId(String userId) {
        return this.baseMapper.selectDailyRankingByUserId(userId);
    }

    @Override
    public List<ProblemUserRanking> topN(Integer n) {
        return this.baseMapper.selectTopN(n);
    }
}