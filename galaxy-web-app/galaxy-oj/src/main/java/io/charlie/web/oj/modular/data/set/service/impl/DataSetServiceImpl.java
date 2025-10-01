package io.charlie.web.oj.modular.data.set.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollStreamUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.charlie.galaxy.utils.str.GaStringUtil;
import io.charlie.web.oj.modular.data.problem.entity.DataProblem;
import io.charlie.web.oj.modular.data.problem.mapper.DataProblemMapper;
import io.charlie.web.oj.modular.data.problem.utils.ProblemBuildTool;
import io.charlie.web.oj.modular.data.ranking.data.RankItem;
import io.charlie.web.oj.modular.data.ranking.service.ProblemCacheService;
import io.charlie.web.oj.modular.data.ranking.service.ProblemSetCacheService;
import io.charlie.web.oj.modular.data.ranking.service.UserCacheService;
import io.charlie.web.oj.modular.data.relation.set.service.DataSetProblemService;
import io.charlie.web.oj.modular.data.set.entity.DataSet;
import io.charlie.web.oj.modular.data.set.param.*;
import io.charlie.web.oj.modular.data.set.mapper.DataSetMapper;
import io.charlie.web.oj.modular.data.set.service.DataSetService;
import io.charlie.galaxy.enums.ISortOrderEnum;
import io.charlie.galaxy.exception.BusinessException;
import io.charlie.galaxy.pojo.CommonPageRequest;
import io.charlie.galaxy.result.ResultCode;
import io.charlie.web.oj.modular.data.set.utils.SetBuildTool;
import org.dromara.trans.service.impl.TransService;
import org.springframework.stereotype.Service;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

/**
 * @author Charlie Zhang
 * @version v1.0
 * @date 2025-06-23
 * @description 题集 服务实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DataSetServiceImpl extends ServiceImpl<DataSetMapper, DataSet> implements DataSetService {
    private final DataSetProblemService dataSetProblemService;
    private final TransService transService;
    private final DataProblemMapper dataProblemMapper;
    private final ProblemBuildTool problemBuildTool;

    private final UserCacheService userCacheService;
    private final ProblemSetCacheService problemSetCacheService;
    private final ProblemCacheService problemCacheService;

    private final SetBuildTool setBuildTool;

    @Override
    public Page<DataSet> page(DataSetPageParam dataSetPageParam) {
        QueryWrapper<DataSet> queryWrapper = new QueryWrapper<DataSet>().checkSqlInjection();
        // 关键字
        if (ObjectUtil.isNotEmpty(dataSetPageParam.getKeyword())) {
            queryWrapper.lambda().like(DataSet::getTitle, dataSetPageParam.getKeyword());
        }
        if (GaStringUtil.isNotEmpty(dataSetPageParam.getCategoryId())) {
            queryWrapper.lambda().eq(DataSet::getCategoryId, dataSetPageParam.getCategoryId());
        }
        if (GaStringUtil.isNotEmpty(dataSetPageParam.getDifficulty())) {
            queryWrapper.lambda().eq(DataSet::getDifficulty, dataSetPageParam.getDifficulty());
        }
        if (GaStringUtil.isNotEmpty(dataSetPageParam.getSetType())) {
            queryWrapper.lambda().eq(DataSet::getSetType, dataSetPageParam.getSetType());
        }
        if (ObjectUtil.isAllNotEmpty(dataSetPageParam.getSortField(), dataSetPageParam.getSortOrder()) && ISortOrderEnum.isValid(dataSetPageParam.getSortOrder())) {
            queryWrapper.orderBy(
                    true,
                    dataSetPageParam.getSortOrder().equals(ISortOrderEnum.ASCEND.getValue()),
                    StrUtil.toUnderlineCase(dataSetPageParam.getSortField()));
        }

        Page<DataSet> page = this.page(CommonPageRequest.Page(
                        Optional.ofNullable(dataSetPageParam.getCurrent()).orElse(1),
                        Optional.ofNullable(dataSetPageParam.getSize()).orElse(20),
                        null
                ),
                queryWrapper);
        setBuildTool.buildSets(page.getRecords());
        return page;
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void add(DataSetAddParam dataSetAddParam) {
        DataSet bean = BeanUtil.toBean(dataSetAddParam, DataSet.class);
        this.save(bean);

        dataSetProblemService.addOrUpdate(bean.getId(), dataSetAddParam.getProblemIds());
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void edit(DataSetEditParam dataSetEditParam) {
        if (!this.exists(new LambdaQueryWrapper<DataSet>().eq(DataSet::getId, dataSetEditParam.getId()))) {
            throw new BusinessException(ResultCode.PARAM_ERROR);
        }
        DataSet bean = BeanUtil.toBean(dataSetEditParam, DataSet.class);
        BeanUtil.copyProperties(dataSetEditParam, bean);
        this.updateById(bean);

        dataSetProblemService.addOrUpdate(bean.getId(), dataSetEditParam.getProblemIds());
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void delete(List<DataSetIdParam> dataSetIdParamList) {
        if (ObjectUtil.isEmpty(dataSetIdParamList)) {
            throw new BusinessException(ResultCode.PARAM_ERROR);
        }
        this.removeByIds(CollStreamUtil.toList(dataSetIdParamList, DataSetIdParam::getId));
    }

    @Override
    public DataSet detail(DataSetIdParam dataSetIdParam) {
        DataSet dataSet = this.getById(dataSetIdParam.getId());
        if (ObjectUtil.isEmpty(dataSet)) {
            throw new BusinessException(ResultCode.PARAM_ERROR);
        }
        setBuildTool.buildSet(dataSet);
        return dataSet;
    }

    @Override
    public List<DataSet> latestN(int n) {
        List<DataSet> list = this.list(new QueryWrapper<DataSet>().checkSqlInjection()
                .lambda()
                .eq(DataSet::getIsVisible, true)
                .orderByAsc(DataSet::getCreateTime)
                .last("LIMIT " + n)
        );
        setBuildTool.buildSets(list);
        return list;
    }

    @Override
    public List<DataProblem> getSetProblem(DataSetProblemParam dataSetProblemParam) {
        List<String> problemIdsBySetId = dataSetProblemService.getProblemIdsBySetId(dataSetProblemParam.getId());
        if (ObjectUtil.isEmpty(problemIdsBySetId)) {
            return List.of();
        }
        List<DataProblem> dataProblems = dataProblemMapper.selectByIds(problemIdsBySetId);
        transService.transBatch(dataProblems);
        problemBuildTool.buildSetProblems(dataSetProblemParam.getId(), dataProblems);
        return dataProblems;
    }

    @Override
    public DataProblem getSetProblemDetail(DataSetProblemDetailParam dataSetProblemDetailParam) {
        List<String> problemIdsBySetId = dataSetProblemService.getProblemIdsBySetId(dataSetProblemDetailParam.getId());
        if (ObjectUtil.isEmpty(problemIdsBySetId)) {
            throw new BusinessException("该题集没有题目");
        }

        // 判断problemIdsBySetId 中是否存在dataSetProblemDetailParam.getProblemId
        if (!problemIdsBySetId.contains(dataSetProblemDetailParam.getProblemId())) {
            throw new BusinessException("该题集没有该题目");
        }

        DataProblem dataProblem = dataProblemMapper.selectById(dataSetProblemDetailParam.getProblemId());
        transService.transOne(dataProblem);
        problemBuildTool.buildSetProblem(dataSetProblemDetailParam.getId(), dataProblem);
        return dataProblem;
    }

    @Override
    public List<DataSet> getHotN(int n) {
        List<RankItem> problemSetRankTopN = problemSetCacheService.getProblemSetRankTopN(n);
        List<DataSet> dataSets = new ArrayList<>();
        for (RankItem rankingInfo : problemSetRankTopN) {
            DataSet dataSet = this.getById(rankingInfo.getId());
            dataSet.setRank(rankingInfo.getRank());
            dataSets.add(dataSet);
        }
        setBuildTool.buildSets(dataSets);
        return dataSets;
    }

}