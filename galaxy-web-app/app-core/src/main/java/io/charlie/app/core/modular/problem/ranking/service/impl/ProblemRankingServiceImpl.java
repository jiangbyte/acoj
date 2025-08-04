package io.charlie.app.core.modular.problem.ranking.service.impl;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import io.charlie.app.core.modular.problem.ranking.entity.ProblemRanking;
import io.charlie.app.core.modular.problem.ranking.entity.ProblemUserRanking;
import io.charlie.app.core.modular.problem.ranking.mapper.ProblemRankingMapper;
import io.charlie.app.core.modular.problem.ranking.mapper.ProblemUserRankingMapper;
import io.charlie.app.core.modular.problem.ranking.param.ProblemRankingPageParam;
import io.charlie.app.core.modular.problem.ranking.service.ProblemRankingService;
import io.charlie.galaxy.enums.ISortOrderEnum;
import io.charlie.galaxy.pojo.CommonPageRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

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
public class ProblemRankingServiceImpl extends ServiceImpl<ProblemRankingMapper, ProblemRanking> implements ProblemRankingService {
    @Override
    public Page<ProblemRanking> rankingPage(ProblemRankingPageParam rankingPageParam) {
        QueryWrapper<ProblemRanking> queryWrapper = new QueryWrapper<ProblemRanking>().checkSqlInjection();
        // 关键字
        if (ObjectUtil.isNotEmpty(rankingPageParam.getKeyword())) {
            queryWrapper.lambda().like(ProblemRanking::getTitle, rankingPageParam.getKeyword());
        }

        if (ObjectUtil.isAllNotEmpty(rankingPageParam.getSortField(), rankingPageParam.getSortOrder()) && ISortOrderEnum.isValid(rankingPageParam.getSortOrder())) {
            queryWrapper.orderBy(
                    true,
                    rankingPageParam.getSortOrder().equals(ISortOrderEnum.ASCEND.getValue()),
                    StrUtil.toUnderlineCase(rankingPageParam.getSortField()));
        }

        return (Page<ProblemRanking>) this.baseMapper.rankingPage(CommonPageRequest.Page(
                Optional.ofNullable(rankingPageParam.getCurrent()).orElse(1),
                Optional.ofNullable(rankingPageParam.getSize()).orElse(20),
                null
        ), queryWrapper);
    }
}
