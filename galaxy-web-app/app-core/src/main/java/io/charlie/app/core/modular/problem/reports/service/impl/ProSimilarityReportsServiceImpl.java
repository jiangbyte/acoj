package io.charlie.app.core.modular.problem.reports.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollStreamUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.charlie.app.core.modular.problem.reports.entity.ProSimilarityReports;
import io.charlie.app.core.modular.problem.reports.param.ProSimilarityReportsAddParam;
import io.charlie.app.core.modular.problem.reports.param.ProSimilarityReportsEditParam;
import io.charlie.app.core.modular.problem.reports.param.ProSimilarityReportsIdParam;
import io.charlie.app.core.modular.problem.reports.param.ProSimilarityReportsPageParam;
import io.charlie.app.core.modular.problem.reports.mapper.ProSimilarityReportsMapper;
import io.charlie.app.core.modular.problem.reports.service.ProSimilarityReportsService;
import io.charlie.galaxy.enums.ISortOrderEnum;
import io.charlie.galaxy.exception.BusinessException;
import io.charlie.galaxy.pojo.CommonPageRequest;
import io.charlie.galaxy.result.ResultCode;
import org.springframework.stereotype.Service;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

/**
* @author Charlie Zhang
* @version v1.0
* @date 2025-06-23
* @description 题目报告库表 服务实现类
*/
@Slf4j
@Service
@RequiredArgsConstructor
public class ProSimilarityReportsServiceImpl extends ServiceImpl<ProSimilarityReportsMapper, ProSimilarityReports> implements ProSimilarityReportsService {

    @Override
    public Page<ProSimilarityReports> page(ProSimilarityReportsPageParam proSimilarityReportsPageParam) {
        QueryWrapper<ProSimilarityReports> queryWrapper = new QueryWrapper<ProSimilarityReports>().checkSqlInjection();
        if (ObjectUtil.isAllNotEmpty(proSimilarityReportsPageParam.getSortField(), proSimilarityReportsPageParam.getSortOrder()) && ISortOrderEnum.isValid(proSimilarityReportsPageParam.getSortOrder())) {
            queryWrapper.orderBy(
                    true,
                    proSimilarityReportsPageParam.getSortOrder().equals(ISortOrderEnum.ASCEND.getValue()),
                    StrUtil.toUnderlineCase(proSimilarityReportsPageParam.getSortField()));
        }

        return this.page(CommonPageRequest.Page(
                        Optional.ofNullable(proSimilarityReportsPageParam.getCurrent()).orElse(1),
                        Optional.ofNullable(proSimilarityReportsPageParam.getSize()).orElse(20),
                null
                ),
                queryWrapper);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void add(ProSimilarityReportsAddParam proSimilarityReportsAddParam) {
        ProSimilarityReports bean = BeanUtil.toBean(proSimilarityReportsAddParam, ProSimilarityReports.class);
        this.save(bean);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void edit(ProSimilarityReportsEditParam proSimilarityReportsEditParam) {
        if (!this.exists(new LambdaQueryWrapper<ProSimilarityReports>().eq(ProSimilarityReports::getId, proSimilarityReportsEditParam.getId()))) {
            throw new BusinessException(ResultCode.PARAM_ERROR);
        }
        ProSimilarityReports bean = BeanUtil.toBean(proSimilarityReportsEditParam, ProSimilarityReports.class);
        BeanUtil.copyProperties(proSimilarityReportsEditParam, bean);
        this.updateById(bean);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void delete(List<ProSimilarityReportsIdParam> proSimilarityReportsIdParamList) {
        if (ObjectUtil.isEmpty(proSimilarityReportsIdParamList)) {
            throw new BusinessException(ResultCode.PARAM_ERROR);
        }
        this.removeByIds(CollStreamUtil.toList(proSimilarityReportsIdParamList, ProSimilarityReportsIdParam::getId));
    }

    @Override
    public ProSimilarityReports detail(ProSimilarityReportsIdParam proSimilarityReportsIdParam) {
        ProSimilarityReports proSimilarityReports = this.getById(proSimilarityReportsIdParam.getId());
        if (ObjectUtil.isEmpty(proSimilarityReports)) {
            throw new BusinessException(ResultCode.PARAM_ERROR);
        }
        return proSimilarityReports;
    }

}