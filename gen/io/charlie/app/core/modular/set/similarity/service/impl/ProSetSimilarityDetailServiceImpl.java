package io.charlie.problem.service.base.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollStreamUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.charlie.app.core.modular.set.similarity.entity.ProSetSimilarityDetail;
import io.charlie.app.core.modular.set.similarity.param.ProSetSimilarityDetailAddParam;
import io.charlie.app.core.modular.set.similarity.param.ProSetSimilarityDetailEditParam;
import io.charlie.app.core.modular.set.similarity.param.ProSetSimilarityDetailIdParam;
import io.charlie.app.core.modular.set.similarity.param.ProSetSimilarityDetailPageParam;
import io.charlie.app.core.modular.set.similarity.mapper.ProSetSimilarityDetailMapper;
import io.charlie.app.core.modular.set.similarity.service.ProSetSimilarityDetailService;
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
* @description 题集题目检测结果任务库 服务实现类
*/
@Slf4j
@Service
@RequiredArgsConstructor
public class ProSetSimilarityDetailServiceImpl extends ServiceImpl<ProSetSimilarityDetailMapper, ProSetSimilarityDetail> implements ProSetSimilarityDetailService {

    @Override
    public Page<ProSetSimilarityDetail> page(ProSetSimilarityDetailPageParam proSetSimilarityDetailPageParam) {
        QueryWrapper<ProSetSimilarityDetail> queryWrapper = new QueryWrapper<ProSetSimilarityDetail>().checkSqlInjection();
        if (ObjectUtil.isAllNotEmpty(proSetSimilarityDetailPageParam.getSortField(), proSetSimilarityDetailPageParam.getSortOrder()) && ISortOrderEnum.isValid(proSetSimilarityDetailPageParam.getSortOrder())) {
            queryWrapper.orderBy(
                    true,
                    proSetSimilarityDetailPageParam.getSortOrder().equals(ISortOrderEnum.ASCEND.getValue()),
                    StrUtil.toUnderlineCase(proSetSimilarityDetailPageParam.getSortField()));
        }

        return this.page(CommonPageRequest.Page(
                        Optional.ofNullable(proSetSimilarityDetailPageParam.getCurrent()).orElse(1),
                        Optional.ofNullable(proSetSimilarityDetailPageParam.getSize()).orElse(20),
                null
                ),
                queryWrapper);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void add(ProSetSimilarityDetailAddParam proSetSimilarityDetailAddParam) {
        ProSetSimilarityDetail bean = BeanUtil.toBean(proSetSimilarityDetailAddParam, ProSetSimilarityDetail.class);
        this.save(bean);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void edit(ProSetSimilarityDetailEditParam proSetSimilarityDetailEditParam) {
        if (!this.exists(new LambdaQueryWrapper<ProSetSimilarityDetail>().eq(ProSetSimilarityDetail::getId, proSetSimilarityDetailEditParam.getId()))) {
            throw new BusinessException(ResultCode.PARAM_ERROR);
        }
        ProSetSimilarityDetail bean = BeanUtil.toBean(proSetSimilarityDetailEditParam, ProSetSimilarityDetail.class);
        BeanUtil.copyProperties(proSetSimilarityDetailEditParam, bean);
        this.updateById(bean);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void delete(List<ProSetSimilarityDetailIdParam> proSetSimilarityDetailIdParamList) {
        if (ObjectUtil.isEmpty(proSetSimilarityDetailIdParamList)) {
            throw new BusinessException(ResultCode.PARAM_ERROR);
        }
        this.removeByIds(CollStreamUtil.toList(proSetSimilarityDetailIdParamList, ProSetSimilarityDetailIdParam::getId));
    }

    @Override
    public ProSetSimilarityDetail detail(ProSetSimilarityDetailIdParam proSetSimilarityDetailIdParam) {
        ProSetSimilarityDetail proSetSimilarityDetail = this.getById(proSetSimilarityDetailIdParam.getId());
        if (ObjectUtil.isEmpty(proSetSimilarityDetail)) {
            throw new BusinessException(ResultCode.PARAM_ERROR);
        }
        return proSetSimilarityDetail;
    }

}