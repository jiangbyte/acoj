package io.charlie.app.core.modular.tag.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollStreamUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.charlie.app.core.modular.category.entity.ProCategory;
import io.charlie.app.core.modular.problem.relation.service.ProProblemTagService;
import io.charlie.app.core.modular.tag.entity.ProTag;
import io.charlie.app.core.modular.tag.param.*;
import io.charlie.app.core.modular.tag.mapper.ProTagMapper;
import io.charlie.app.core.modular.tag.service.ProTagService;
import io.charlie.galaxy.enums.ISortOrderEnum;
import io.charlie.galaxy.exception.BusinessException;
import io.charlie.galaxy.option.LabelOption;
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
 * @description 标签表 服务实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ProTagServiceImpl extends ServiceImpl<ProTagMapper, ProTag> implements ProTagService {
    private final ProProblemTagService proProblemTagService;

    @Override
    public Page<ProTag> page(ProTagPageParam proTagPageParam) {
        QueryWrapper<ProTag> queryWrapper = new QueryWrapper<ProTag>().checkSqlInjection();
        // 关键字
        if (ObjectUtil.isNotEmpty(proTagPageParam.getKeyword())) {
            queryWrapper.lambda().like(ProTag::getName, proTagPageParam.getKeyword());
        }
        if (ObjectUtil.isAllNotEmpty(proTagPageParam.getSortField(), proTagPageParam.getSortOrder()) && ISortOrderEnum.isValid(proTagPageParam.getSortOrder())) {
            queryWrapper.orderBy(
                    true,
                    proTagPageParam.getSortOrder().equals(ISortOrderEnum.ASCEND.getValue()),
                    StrUtil.toUnderlineCase(proTagPageParam.getSortField()));
        }

        return this.page(CommonPageRequest.Page(
                        Optional.ofNullable(proTagPageParam.getCurrent()).orElse(1),
                        Optional.ofNullable(proTagPageParam.getSize()).orElse(20),
                        null
                ),
                queryWrapper);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void add(ProTagAddParam proTagAddParam) {
        ProTag bean = BeanUtil.toBean(proTagAddParam, ProTag.class);
        this.save(bean);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void edit(ProTagEditParam proTagEditParam) {
        if (!this.exists(new LambdaQueryWrapper<ProTag>().eq(ProTag::getId, proTagEditParam.getId()))) {
            throw new BusinessException(ResultCode.PARAM_ERROR);
        }
        ProTag bean = BeanUtil.toBean(proTagEditParam, ProTag.class);
        BeanUtil.copyProperties(proTagEditParam, bean);
        this.updateById(bean);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void delete(List<ProTagIdParam> proTagIdParamList) {
        if (ObjectUtil.isEmpty(proTagIdParamList)) {
            throw new BusinessException(ResultCode.PARAM_ERROR);
        }
        proTagIdParamList.forEach(proTagIdParam -> proProblemTagService.deleteByTagId(proTagIdParam.getId()));
        this.removeByIds(CollStreamUtil.toList(proTagIdParamList, ProTagIdParam::getId));
    }

    @Override
    public ProTag detail(ProTagIdParam proTagIdParam) {
        ProTag proTag = this.getById(proTagIdParam.getId());
        if (ObjectUtil.isEmpty(proTag)) {
            throw new BusinessException(ResultCode.PARAM_ERROR);
        }
        return proTag;
    }

    @Override
    public List<LabelOption<String>> options(ProTagOptionParam proTagOptionParam) {
        QueryWrapper<ProTag> queryWrapper = new QueryWrapper<ProTag>().checkSqlInjection();
        // 关键字
        if (ObjectUtil.isNotEmpty(proTagOptionParam.getKeyword())) {
            queryWrapper.lambda().like(ProTag::getName, proTagOptionParam.getKeyword());
        }
        return this.list(queryWrapper).stream().map(proCategory -> new LabelOption<>(proCategory.getId(), proCategory.getName())).toList();
    }

}