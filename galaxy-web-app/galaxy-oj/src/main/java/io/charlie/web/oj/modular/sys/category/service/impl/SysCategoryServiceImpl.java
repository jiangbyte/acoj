package io.charlie.web.oj.modular.sys.category.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollStreamUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.charlie.galaxy.option.LabelOption;
import io.charlie.web.oj.modular.sys.category.entity.SysCategory;
import io.charlie.web.oj.modular.sys.category.param.*;
import io.charlie.web.oj.modular.sys.category.mapper.SysCategoryMapper;
import io.charlie.web.oj.modular.sys.category.service.SysCategoryService;
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
* @description 分类表 服务实现类
*/
@Slf4j
@Service
@RequiredArgsConstructor
public class SysCategoryServiceImpl extends ServiceImpl<SysCategoryMapper, SysCategory> implements SysCategoryService {

    @Override
    public Page<SysCategory> page(SysCategoryPageParam sysCategoryPageParam) {
        QueryWrapper<SysCategory> queryWrapper = new QueryWrapper<SysCategory>().checkSqlInjection();
        // 关键字
        if (ObjectUtil.isNotEmpty(sysCategoryPageParam.getKeyword())) {
            queryWrapper.lambda().like(SysCategory::getName, sysCategoryPageParam.getKeyword());
        }
        if (ObjectUtil.isAllNotEmpty(sysCategoryPageParam.getSortField(), sysCategoryPageParam.getSortOrder()) && ISortOrderEnum.isValid(sysCategoryPageParam.getSortOrder())) {
            queryWrapper.orderBy(
                    true,
                    sysCategoryPageParam.getSortOrder().equals(ISortOrderEnum.ASCEND.getValue()),
                    StrUtil.toUnderlineCase(sysCategoryPageParam.getSortField()));
        }

        return this.page(CommonPageRequest.Page(
                        Optional.ofNullable(sysCategoryPageParam.getCurrent()).orElse(1),
                        Optional.ofNullable(sysCategoryPageParam.getSize()).orElse(20),
                null
                ),
                queryWrapper);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void add(SysCategoryAddParam sysCategoryAddParam) {
        SysCategory bean = BeanUtil.toBean(sysCategoryAddParam, SysCategory.class);
        this.save(bean);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void edit(SysCategoryEditParam sysCategoryEditParam) {
        if (!this.exists(new LambdaQueryWrapper<SysCategory>().eq(SysCategory::getId, sysCategoryEditParam.getId()))) {
            throw new BusinessException(ResultCode.PARAM_ERROR);
        }
        SysCategory bean = BeanUtil.toBean(sysCategoryEditParam, SysCategory.class);
        BeanUtil.copyProperties(sysCategoryEditParam, bean);
        this.updateById(bean);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void delete(List<SysCategoryIdParam> sysCategoryIdParamList) {
        if (ObjectUtil.isEmpty(sysCategoryIdParamList)) {
            throw new BusinessException(ResultCode.PARAM_ERROR);
        }
        this.removeByIds(CollStreamUtil.toList(sysCategoryIdParamList, SysCategoryIdParam::getId));
    }

    @Override
    public SysCategory detail(SysCategoryIdParam sysCategoryIdParam) {
        SysCategory sysCategory = this.getById(sysCategoryIdParam.getId());
        if (ObjectUtil.isEmpty(sysCategory)) {
            throw new BusinessException(ResultCode.PARAM_ERROR);
        }
        return sysCategory;
    }

    @Override
    public List<LabelOption<String>> options(SysCategoryOptionParam sysCategoryOptionParam) {
        QueryWrapper<SysCategory> queryWrapper = new QueryWrapper<SysCategory>().checkSqlInjection();
        // 关键字
        if (ObjectUtil.isNotEmpty(sysCategoryOptionParam.getKeyword())) {
            queryWrapper.lambda().like(SysCategory::getName, sysCategoryOptionParam.getKeyword());
        }
        return this.list(queryWrapper).stream().map(proCategory -> new LabelOption<>(proCategory.getId(), proCategory.getName())).toList();
    }

}