package io.charlie.app.core.modular.set.set.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollStreamUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.charlie.app.core.modular.set.set.entity.ProSet;
import io.charlie.app.core.modular.set.set.param.ProSetAddParam;
import io.charlie.app.core.modular.set.set.param.ProSetEditParam;
import io.charlie.app.core.modular.set.set.param.ProSetIdParam;
import io.charlie.app.core.modular.set.set.param.ProSetPageParam;
import io.charlie.app.core.modular.set.set.mapper.ProSetMapper;
import io.charlie.app.core.modular.set.set.service.ProSetService;
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
* @description 题集 服务实现类
*/
@Slf4j
@Service
@RequiredArgsConstructor
public class ProSetServiceImpl extends ServiceImpl<ProSetMapper, ProSet> implements ProSetService {

    @Override
    public Page<ProSet> page(ProSetPageParam proSetPageParam) {
        QueryWrapper<ProSet> queryWrapper = new QueryWrapper<ProSet>().checkSqlInjection();
        // 关键字
        if (ObjectUtil.isNotEmpty(proSetPageParam.getKeyword())) {
            queryWrapper.lambda().like(ProSet::getTitle, proSetPageParam.getKeyword());
        }
        if (ObjectUtil.isAllNotEmpty(proSetPageParam.getSortField(), proSetPageParam.getSortOrder()) && ISortOrderEnum.isValid(proSetPageParam.getSortOrder())) {
            queryWrapper.orderBy(
                    true,
                    proSetPageParam.getSortOrder().equals(ISortOrderEnum.ASCEND.getValue()),
                    StrUtil.toUnderlineCase(proSetPageParam.getSortField()));
        }

        return this.page(CommonPageRequest.Page(
                        Optional.ofNullable(proSetPageParam.getCurrent()).orElse(1),
                        Optional.ofNullable(proSetPageParam.getSize()).orElse(20),
                null
                ),
                queryWrapper);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void add(ProSetAddParam proSetAddParam) {
        ProSet bean = BeanUtil.toBean(proSetAddParam, ProSet.class);
        this.save(bean);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void edit(ProSetEditParam proSetEditParam) {
        if (!this.exists(new LambdaQueryWrapper<ProSet>().eq(ProSet::getId, proSetEditParam.getId()))) {
            throw new BusinessException(ResultCode.PARAM_ERROR);
        }
        ProSet bean = BeanUtil.toBean(proSetEditParam, ProSet.class);
        BeanUtil.copyProperties(proSetEditParam, bean);
        this.updateById(bean);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void delete(List<ProSetIdParam> proSetIdParamList) {
        if (ObjectUtil.isEmpty(proSetIdParamList)) {
            throw new BusinessException(ResultCode.PARAM_ERROR);
        }
        this.removeByIds(CollStreamUtil.toList(proSetIdParamList, ProSetIdParam::getId));
    }

    @Override
    public ProSet detail(ProSetIdParam proSetIdParam) {
        ProSet proSet = this.getById(proSetIdParam.getId());
        if (ObjectUtil.isEmpty(proSet)) {
            throw new BusinessException(ResultCode.PARAM_ERROR);
        }
        return proSet;
    }

}