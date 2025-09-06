package io.charlie.app.core.modular.problem.similarity.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollStreamUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.charlie.app.core.modular.problem.similarity.entity.ProSimilarityDetail;
import io.charlie.app.core.modular.problem.similarity.param.*;
import io.charlie.app.core.modular.problem.similarity.mapper.ProSimilarityDetailMapper;
import io.charlie.app.core.modular.problem.similarity.service.ProSimilarityDetailService;
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
* @description 题目检测结果任务库 服务实现类
*/
@Slf4j
@Service
@RequiredArgsConstructor
public class ProSimilarityDetailServiceImpl extends ServiceImpl<ProSimilarityDetailMapper, ProSimilarityDetail> implements ProSimilarityDetailService {

    @Override
    public Page<ProSimilarityDetail> page(ProSimilarityDetailPageParam proSimilarityDetailPageParam) {
        QueryWrapper<ProSimilarityDetail> queryWrapper = new QueryWrapper<ProSimilarityDetail>().checkSqlInjection();
        if (ObjectUtil.isAllNotEmpty(proSimilarityDetailPageParam.getSortField(), proSimilarityDetailPageParam.getSortOrder()) && ISortOrderEnum.isValid(proSimilarityDetailPageParam.getSortOrder())) {
            queryWrapper.orderBy(
                    true,
                    proSimilarityDetailPageParam.getSortOrder().equals(ISortOrderEnum.ASCEND.getValue()),
                    StrUtil.toUnderlineCase(proSimilarityDetailPageParam.getSortField()));
        }

        return this.page(CommonPageRequest.Page(
                        Optional.ofNullable(proSimilarityDetailPageParam.getCurrent()).orElse(1),
                        Optional.ofNullable(proSimilarityDetailPageParam.getSize()).orElse(20),
                null
                ),
                queryWrapper);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void add(ProSimilarityDetailAddParam proSimilarityDetailAddParam) {
        ProSimilarityDetail bean = BeanUtil.toBean(proSimilarityDetailAddParam, ProSimilarityDetail.class);
        this.save(bean);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void edit(ProSimilarityDetailEditParam proSimilarityDetailEditParam) {
        if (!this.exists(new LambdaQueryWrapper<ProSimilarityDetail>().eq(ProSimilarityDetail::getId, proSimilarityDetailEditParam.getId()))) {
            throw new BusinessException(ResultCode.PARAM_ERROR);
        }
        ProSimilarityDetail bean = BeanUtil.toBean(proSimilarityDetailEditParam, ProSimilarityDetail.class);
        BeanUtil.copyProperties(proSimilarityDetailEditParam, bean);
        this.updateById(bean);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void delete(List<ProSimilarityDetailIdParam> proSimilarityDetailIdParamList) {
        if (ObjectUtil.isEmpty(proSimilarityDetailIdParamList)) {
            throw new BusinessException(ResultCode.PARAM_ERROR);
        }
        this.removeByIds(CollStreamUtil.toList(proSimilarityDetailIdParamList, ProSimilarityDetailIdParam::getId));
    }

    @Override
    public ProSimilarityDetail detail(ProSimilarityDetailIdParam proSimilarityDetailIdParam) {
        ProSimilarityDetail proSimilarityDetail = this.getById(proSimilarityDetailIdParam.getId());
        if (ObjectUtil.isEmpty(proSimilarityDetail)) {
            throw new BusinessException(ResultCode.PARAM_ERROR);
        }
        return proSimilarityDetail;
    }

    @Override
    public void problemSimilarityReport(ProblemReportConfig config) {

    }

}