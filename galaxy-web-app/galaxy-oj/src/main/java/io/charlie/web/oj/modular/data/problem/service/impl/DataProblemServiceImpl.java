package io.charlie.web.oj.modular.data.problem.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollStreamUtil;
import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.charlie.galaxy.utils.ranking.RankingInfo;
import io.charlie.galaxy.utils.ranking.RankingUtil;
import io.charlie.web.oj.modular.data.problem.entity.DataProblem;
import io.charlie.web.oj.modular.data.problem.entity.DataProblemCount;
import io.charlie.web.oj.modular.data.problem.param.DataProblemAddParam;
import io.charlie.web.oj.modular.data.problem.param.DataProblemEditParam;
import io.charlie.web.oj.modular.data.problem.param.DataProblemIdParam;
import io.charlie.web.oj.modular.data.problem.param.DataProblemPageParam;
import io.charlie.web.oj.modular.data.problem.mapper.DataProblemMapper;
import io.charlie.web.oj.modular.data.problem.service.DataProblemService;
import io.charlie.galaxy.enums.ISortOrderEnum;
import io.charlie.galaxy.exception.BusinessException;
import io.charlie.galaxy.pojo.CommonPageRequest;
import io.charlie.galaxy.result.ResultCode;
import io.charlie.web.oj.modular.data.ranking.enums.RankingEnums;
import io.charlie.web.oj.modular.data.ranking.service.UserRankingService;
import io.charlie.web.oj.modular.data.relation.tag.service.DataProblemTagService;
import io.charlie.web.oj.modular.data.solved.mapper.DataSolvedMapper;
import org.springframework.stereotype.Service;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.*;

/**
 * @author Charlie Zhang
 * @version v1.0
 * @date 2025-06-23
 * @description 题目表 服务实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DataProblemServiceImpl extends ServiceImpl<DataProblemMapper, DataProblem> implements DataProblemService {
    private final DataSolvedMapper dataSolvedMapper;
    private final RankingUtil rankingUtil;
    private final UserRankingService userRankingService;
    private final DataProblemTagService dataProblemTagService;

    private void buildProblems(List<DataProblem> dataProblems) {
        if (CollectionUtil.isEmpty(dataProblems)) {
            return;
        }

        dataProblems.forEach(dataProblem -> {
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
        });
    }

    @Override
    public Page<DataProblem> page(DataProblemPageParam dataProblemPageParam) {
        QueryWrapper<DataProblem> queryWrapper = new QueryWrapper<DataProblem>().checkSqlInjection();
        // 关键字
        if (ObjectUtil.isNotEmpty(dataProblemPageParam.getKeyword())) {
            queryWrapper.lambda().like(DataProblem::getTitle, dataProblemPageParam.getKeyword());
        }
        if (ObjectUtil.isAllNotEmpty(dataProblemPageParam.getSortField(), dataProblemPageParam.getSortOrder()) && ISortOrderEnum.isValid(dataProblemPageParam.getSortOrder())) {
            queryWrapper.orderBy(
                    true,
                    dataProblemPageParam.getSortOrder().equals(ISortOrderEnum.ASCEND.getValue()),
                    StrUtil.toUnderlineCase(dataProblemPageParam.getSortField()));
        }

        Page<DataProblem> page = this.page(CommonPageRequest.Page(
                        Optional.ofNullable(dataProblemPageParam.getCurrent()).orElse(1),
                        Optional.ofNullable(dataProblemPageParam.getSize()).orElse(20),
                        null
                ),
                queryWrapper);
        buildProblems(page.getRecords());
        return page;
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void add(DataProblemAddParam dataProblemAddParam) {
        DataProblem bean = BeanUtil.toBean(dataProblemAddParam, DataProblem.class);
        this.save(bean);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void edit(DataProblemEditParam dataProblemEditParam) {
        if (!this.exists(new LambdaQueryWrapper<DataProblem>().eq(DataProblem::getId, dataProblemEditParam.getId()))) {
            throw new BusinessException(ResultCode.PARAM_ERROR);
        }
        DataProblem bean = BeanUtil.toBean(dataProblemEditParam, DataProblem.class);
        BeanUtil.copyProperties(dataProblemEditParam, bean);
        this.updateById(bean);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void delete(List<DataProblemIdParam> dataProblemIdParamList) {
        if (ObjectUtil.isEmpty(dataProblemIdParamList)) {
            throw new BusinessException(ResultCode.PARAM_ERROR);
        }
        this.removeByIds(CollStreamUtil.toList(dataProblemIdParamList, DataProblemIdParam::getId));
    }

    @Override
    public DataProblem detail(DataProblemIdParam dataProblemIdParam) {
        DataProblem dataProblem = this.getById(dataProblemIdParam.getId());
        if (ObjectUtil.isEmpty(dataProblem)) {
            throw new BusinessException(ResultCode.PARAM_ERROR);
        }
        return dataProblem;
    }

    @Override
    public List<DataProblem> latestN(int n) {
        List<DataProblem> list = this.list(new QueryWrapper<DataProblem>().checkSqlInjection()
                .lambda()
                .eq(DataProblem::getIsVisible, true)
                .orderByDesc(DataProblem::getCreateTime)
                .last("LIMIT " + n));
        return list;
    }

    @Override
    public DataProblemCount getProblemCount() {
        DataProblemCount dataProblemCount = new DataProblemCount();

        long count = this.count(new LambdaQueryWrapper<DataProblem>()
                .eq(DataProblem::getIsVisible, true)
                .eq(DataProblem::getIsPublic, true)
        );
        dataProblemCount.setTotal(count);

        long monthAdd = this.count(new LambdaQueryWrapper<DataProblem>()
                .eq(DataProblem::getIsVisible, true)
                .eq(DataProblem::getIsPublic, true)
                .ge(DataProblem::getCreateTime, Date.from(new Date().toInstant().minusSeconds(30 * 24 * 60 * 60)))
        );
        dataProblemCount.setMonthAdd(monthAdd);

        dataProblemCount.setLastAddTime(this.getOne(new LambdaQueryWrapper<DataProblem>()
                .eq(DataProblem::getIsVisible, true)
                .eq(DataProblem::getIsPublic, true)
                .orderByDesc(DataProblem::getCreateTime)
                .last("LIMIT 1")
        ).getCreateTime());

        dataProblemCount.setGrowthRate(BigDecimal.valueOf(monthAdd / (double) count));

        try {
            String userId = StpUtil.getLoginIdAsString();
        } catch (Exception e) {
            // 未登录
        }

        return dataProblemCount;
    }

    @Override
    public List<DataProblem> getHotN(int n) {
        List<RankingInfo> topNRanking = rankingUtil.getTopNRanking(RankingEnums.HOT_PROBLEM.getValue(), n);
        List<DataProblem> dataProblems = new ArrayList<>();
        for (RankingInfo rankingInfo : topNRanking) {
            DataProblem dataProblem = this.getById(rankingInfo.getEntityId());
            dataProblem.setRank(rankingInfo.getRank());
            dataProblems.add(dataProblem);
        }
        return dataProblems;
    }

}