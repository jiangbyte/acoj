package io.charlie.app.core.modular.set.ranking.service.impl;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import io.charlie.app.core.modular.set.ranking.entity.SetUserRanking;
import io.charlie.app.core.modular.set.ranking.mapper.SetUserRankingMapper;
import io.charlie.app.core.modular.set.ranking.param.SetUserRankingPageParam;
import io.charlie.app.core.modular.set.ranking.service.SetUserRankingService;
import io.charlie.galaxy.enums.ISortOrderEnum;
import io.charlie.galaxy.pojo.CommonPageRequest;
import io.charlie.galaxy.utils.GaStringUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Optional;

/**
 * @author Charlie Zhang
 * @version v1.0
 * @date 28/07/2025
 * @description Set提交排行榜服务实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SetUserRankingServiceImpl extends ServiceImpl<SetUserRankingMapper, SetUserRanking> implements SetUserRankingService {

    @Override
    public Page<SetUserRanking> totalRankingPage(SetUserRankingPageParam setRankingPageParam) {
        QueryWrapper<SetUserRanking> queryWrapper = new QueryWrapper<SetUserRanking>().checkSqlInjection();
        // 关键字
        if (ObjectUtil.isNotEmpty(setRankingPageParam.getKeyword())) {
            queryWrapper.lambda().like(SetUserRanking::getNickname, setRankingPageParam.getKeyword());
        }
        if (GaStringUtil.isNotEmpty(setRankingPageParam.getSetId())) {
            queryWrapper.lambda().eq(SetUserRanking::getSetId, setRankingPageParam.getSetId());
        }
        if (ObjectUtil.isAllNotEmpty(setRankingPageParam.getSortField(), setRankingPageParam.getSortOrder()) && ISortOrderEnum.isValid(setRankingPageParam.getSortOrder())) {
            queryWrapper.orderBy(
                    true,
                    setRankingPageParam.getSortOrder().equals(ISortOrderEnum.ASCEND.getValue()),
                    StrUtil.toUnderlineCase(setRankingPageParam.getSortField()));
        }

        return (Page<SetUserRanking>) this.baseMapper.selectTotalRankingPage(CommonPageRequest.Page(
                Optional.ofNullable(setRankingPageParam.getCurrent()).orElse(1),
                Optional.ofNullable(setRankingPageParam.getSize()).orElse(20),
                null
        ), queryWrapper);
    }

    @Override
    public Page<SetUserRanking> monthlyRankingPage(SetUserRankingPageParam setRankingPageParam) {
        QueryWrapper<SetUserRanking> queryWrapper = new QueryWrapper<SetUserRanking>().checkSqlInjection();
        // 关键字
        if (ObjectUtil.isNotEmpty(setRankingPageParam.getKeyword())) {
            queryWrapper.lambda().like(SetUserRanking::getNickname, setRankingPageParam.getKeyword());
        }

        if (GaStringUtil.isNotEmpty(setRankingPageParam.getSetId())) {
            queryWrapper.lambda().eq(SetUserRanking::getSetId, setRankingPageParam.getSetId());
        }
        if (ObjectUtil.isAllNotEmpty(setRankingPageParam.getSortField(), setRankingPageParam.getSortOrder()) && ISortOrderEnum.isValid(setRankingPageParam.getSortOrder())) {
            queryWrapper.orderBy(
                    true,
                    setRankingPageParam.getSortOrder().equals(ISortOrderEnum.ASCEND.getValue()),
                    StrUtil.toUnderlineCase(setRankingPageParam.getSortField()));
        }

        return (Page<SetUserRanking>) this.baseMapper.selectMonthlyRankingPage(CommonPageRequest.Page(
                Optional.ofNullable(setRankingPageParam.getCurrent()).orElse(1),
                Optional.ofNullable(setRankingPageParam.getSize()).orElse(20),
                null
        ), queryWrapper);
    }

    @Override
    public Page<SetUserRanking> weeklyRankingPage(SetUserRankingPageParam setRankingPageParam) {
        QueryWrapper<SetUserRanking> queryWrapper = new QueryWrapper<SetUserRanking>().checkSqlInjection();
        // 关键字
        if (ObjectUtil.isNotEmpty(setRankingPageParam.getKeyword())) {
            queryWrapper.lambda().like(SetUserRanking::getNickname, setRankingPageParam.getKeyword());
        }

        if (GaStringUtil.isNotEmpty(setRankingPageParam.getSetId())) {
            queryWrapper.lambda().eq(SetUserRanking::getSetId, setRankingPageParam.getSetId());
        }
        if (ObjectUtil.isAllNotEmpty(setRankingPageParam.getSortField(), setRankingPageParam.getSortOrder()) && ISortOrderEnum.isValid(setRankingPageParam.getSortOrder())) {
            queryWrapper.orderBy(
                    true,
                    setRankingPageParam.getSortOrder().equals(ISortOrderEnum.ASCEND.getValue()),
                    StrUtil.toUnderlineCase(setRankingPageParam.getSortField()));
        }

        return (Page<SetUserRanking>) this.baseMapper.selectWeeklyRankingPage(CommonPageRequest.Page(
                Optional.ofNullable(setRankingPageParam.getCurrent()).orElse(1),
                Optional.ofNullable(setRankingPageParam.getSize()).orElse(20),
                null
        ), queryWrapper);
    }

    @Override
    public Page<SetUserRanking> dailyRankingPage(SetUserRankingPageParam setRankingPageParam) {
        QueryWrapper<SetUserRanking> queryWrapper = new QueryWrapper<SetUserRanking>().checkSqlInjection();
        // 关键字
        if (ObjectUtil.isNotEmpty(setRankingPageParam.getKeyword())) {
            queryWrapper.lambda().like(SetUserRanking::getNickname, setRankingPageParam.getKeyword());
        }

        if (GaStringUtil.isNotEmpty(setRankingPageParam.getSetId())) {
            queryWrapper.lambda().eq(SetUserRanking::getSetId, setRankingPageParam.getSetId());
        }
        if (ObjectUtil.isAllNotEmpty(setRankingPageParam.getSortField(), setRankingPageParam.getSortOrder()) && ISortOrderEnum.isValid(setRankingPageParam.getSortOrder())) {
            queryWrapper.orderBy(
                    true,
                    setRankingPageParam.getSortOrder().equals(ISortOrderEnum.ASCEND.getValue()),
                    StrUtil.toUnderlineCase(setRankingPageParam.getSortField()));
        }

        return (Page<SetUserRanking>) this.baseMapper.selectDailyRankingPage(CommonPageRequest.Page(
                Optional.ofNullable(setRankingPageParam.getCurrent()).orElse(1),
                Optional.ofNullable(setRankingPageParam.getSize()).orElse(20),
                null
        ), queryWrapper);
    }

    @Override
    public SetUserRanking totalRankingByUserId(String setId, String userId) {
        return this.baseMapper.selectTotalRankingByUserId(setId, userId);
    }

    @Override
    public SetUserRanking monthlyRankingByUserId(String setId, String userId) {
        return this.baseMapper.selectMonthlyRankingByUserId(setId, userId);
    }

    @Override
    public SetUserRanking weeklyRankingByUserId(String setId, String userId) {
        return this.baseMapper.selectWeeklyRankingByUserId(setId, userId);
    }

    @Override
    public SetUserRanking dailyRankingByUserId(String setId, String userId) {
        return this.baseMapper.selectDailyRankingByUserId(setId, userId);
    }
}
