package io.charlie.app.core.modular.set.reports.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollStreamUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.charlie.app.core.modular.set.reports.entity.ProSetSimilarityReports;
import io.charlie.app.core.modular.set.reports.param.ProSetSimilarityReportsAddParam;
import io.charlie.app.core.modular.set.reports.param.ProSetSimilarityReportsEditParam;
import io.charlie.app.core.modular.set.reports.param.ProSetSimilarityReportsIdParam;
import io.charlie.app.core.modular.set.reports.param.ProSetSimilarityReportsPageParam;
import io.charlie.app.core.modular.set.reports.mapper.ProSetSimilarityReportsMapper;
import io.charlie.app.core.modular.set.reports.service.ProSetSimilarityReportsService;
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
* @description 题库题目报告库表 服务实现类
*/
@Slf4j
@Service
@RequiredArgsConstructor
public class ProSetSimilarityReportsServiceImpl extends ServiceImpl<ProSetSimilarityReportsMapper, ProSetSimilarityReports> implements ProSetSimilarityReportsService {

    @Override
    public Page<ProSetSimilarityReports> page(ProSetSimilarityReportsPageParam proSetSimilarityReportsPageParam) {
        QueryWrapper<ProSetSimilarityReports> queryWrapper = new QueryWrapper<ProSetSimilarityReports>().checkSqlInjection();
        if (ObjectUtil.isAllNotEmpty(proSetSimilarityReportsPageParam.getSortField(), proSetSimilarityReportsPageParam.getSortOrder()) && ISortOrderEnum.isValid(proSetSimilarityReportsPageParam.getSortOrder())) {
            queryWrapper.orderBy(
                    true,
                    proSetSimilarityReportsPageParam.getSortOrder().equals(ISortOrderEnum.ASCEND.getValue()),
                    StrUtil.toUnderlineCase(proSetSimilarityReportsPageParam.getSortField()));
        }

        return this.page(CommonPageRequest.Page(
                        Optional.ofNullable(proSetSimilarityReportsPageParam.getCurrent()).orElse(1),
                        Optional.ofNullable(proSetSimilarityReportsPageParam.getSize()).orElse(20),
                null
                ),
                queryWrapper);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void add(ProSetSimilarityReportsAddParam proSetSimilarityReportsAddParam) {
        ProSetSimilarityReports bean = BeanUtil.toBean(proSetSimilarityReportsAddParam, ProSetSimilarityReports.class);
        this.save(bean);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void edit(ProSetSimilarityReportsEditParam proSetSimilarityReportsEditParam) {
        if (!this.exists(new LambdaQueryWrapper<ProSetSimilarityReports>().eq(ProSetSimilarityReports::getId, proSetSimilarityReportsEditParam.getId()))) {
            throw new BusinessException(ResultCode.PARAM_ERROR);
        }
        ProSetSimilarityReports bean = BeanUtil.toBean(proSetSimilarityReportsEditParam, ProSetSimilarityReports.class);
        BeanUtil.copyProperties(proSetSimilarityReportsEditParam, bean);
        this.updateById(bean);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void delete(List<ProSetSimilarityReportsIdParam> proSetSimilarityReportsIdParamList) {
        if (ObjectUtil.isEmpty(proSetSimilarityReportsIdParamList)) {
            throw new BusinessException(ResultCode.PARAM_ERROR);
        }
        this.removeByIds(CollStreamUtil.toList(proSetSimilarityReportsIdParamList, ProSetSimilarityReportsIdParam::getId));
    }

    @Override
    public ProSetSimilarityReports detail(ProSetSimilarityReportsIdParam proSetSimilarityReportsIdParam) {
        ProSetSimilarityReports proSetSimilarityReports = this.getById(proSetSimilarityReportsIdParam.getId());
        if (ObjectUtil.isEmpty(proSetSimilarityReports)) {
            throw new BusinessException(ResultCode.PARAM_ERROR);
        }
        return proSetSimilarityReports;
    }

}