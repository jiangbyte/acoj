package io.charlie.app.core.modular.set.ranking.service.impl;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import io.charlie.app.core.modular.problem.ranking.entity.ProblemRanking;
import io.charlie.app.core.modular.set.ranking.entity.SetRanking;
import io.charlie.app.core.modular.set.ranking.entity.SetUserRanking;
import io.charlie.app.core.modular.set.ranking.mapper.SetRankingMapper;
import io.charlie.app.core.modular.set.ranking.mapper.SetUserRankingMapper;
import io.charlie.app.core.modular.set.ranking.param.SetRankingPageParam;
import io.charlie.app.core.modular.set.ranking.service.SetRankingService;
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
 * @date 03/08/2025
 * @description TODO
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SetRankingServiceImpl extends ServiceImpl<SetRankingMapper, SetRanking> implements SetRankingService {
    @Override
    public Page<SetRanking> rankingPage(SetRankingPageParam setRankingPageParam) {
        QueryWrapper<SetRanking> queryWrapper = new QueryWrapper<SetRanking>().checkSqlInjection();
        // 关键字
        if (ObjectUtil.isNotEmpty(setRankingPageParam.getKeyword())) {
            queryWrapper.lambda().like(SetRanking::getTitle, setRankingPageParam.getKeyword());
        }

        if (ObjectUtil.isAllNotEmpty(setRankingPageParam.getSortField(), setRankingPageParam.getSortOrder()) && ISortOrderEnum.isValid(setRankingPageParam.getSortOrder())) {
            queryWrapper.orderBy(
                    true,
                    setRankingPageParam.getSortOrder().equals(ISortOrderEnum.ASCEND.getValue()),
                    StrUtil.toUnderlineCase(setRankingPageParam.getSortField()));
        }

        return (Page<SetRanking>) this.baseMapper.rankingPage(CommonPageRequest.Page(
                Optional.ofNullable(setRankingPageParam.getCurrent()).orElse(1),
                Optional.ofNullable(setRankingPageParam.getSize()).orElse(20),
                null
        ), queryWrapper);
    }

    @Override
    public List<SetRanking> topN(Integer n) {
        return this.baseMapper.selectTopN(n);
    }
}
