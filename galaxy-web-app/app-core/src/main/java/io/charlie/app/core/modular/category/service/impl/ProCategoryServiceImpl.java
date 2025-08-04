package io.charlie.app.core.modular.category.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollStreamUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.charlie.app.core.modular.category.entity.ProCategory;
import io.charlie.app.core.modular.category.param.*;
import io.charlie.app.core.modular.category.mapper.ProCategoryMapper;
import io.charlie.app.core.modular.category.service.ProCategoryService;
import io.charlie.app.core.modular.sys.user.entity.SysUser;
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
 * @description 分类表 服务实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ProCategoryServiceImpl extends ServiceImpl<ProCategoryMapper, ProCategory> implements ProCategoryService {

    @Override
    public Page<ProCategory> page(ProCategoryPageParam proCategoryPageParam) {
        QueryWrapper<ProCategory> queryWrapper = new QueryWrapper<ProCategory>().checkSqlInjection();
        // 关键字
        if (ObjectUtil.isNotEmpty(proCategoryPageParam.getKeyword())) {
            queryWrapper.lambda().like(ProCategory::getName, proCategoryPageParam.getKeyword());
        }
        if (ObjectUtil.isAllNotEmpty(proCategoryPageParam.getSortField(), proCategoryPageParam.getSortOrder()) && ISortOrderEnum.isValid(proCategoryPageParam.getSortOrder())) {
            queryWrapper.orderBy(
                    true,
                    proCategoryPageParam.getSortOrder().equals(ISortOrderEnum.ASCEND.getValue()),
                    StrUtil.toUnderlineCase(proCategoryPageParam.getSortField()));
        }

        return this.page(CommonPageRequest.Page(
                        Optional.ofNullable(proCategoryPageParam.getCurrent()).orElse(1),
                        Optional.ofNullable(proCategoryPageParam.getSize()).orElse(20),
                        null
                ),
                queryWrapper);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void add(ProCategoryAddParam proCategoryAddParam) {
        ProCategory bean = BeanUtil.toBean(proCategoryAddParam, ProCategory.class);
        this.save(bean);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void edit(ProCategoryEditParam proCategoryEditParam) {
        if (!this.exists(new LambdaQueryWrapper<ProCategory>().eq(ProCategory::getId, proCategoryEditParam.getId()))) {
            throw new BusinessException(ResultCode.PARAM_ERROR);
        }
        ProCategory bean = BeanUtil.toBean(proCategoryEditParam, ProCategory.class);
        BeanUtil.copyProperties(proCategoryEditParam, bean);
        this.updateById(bean);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void delete(List<ProCategoryIdParam> proCategoryIdParamList) {
        if (ObjectUtil.isEmpty(proCategoryIdParamList)) {
            throw new BusinessException(ResultCode.PARAM_ERROR);
        }
        this.removeByIds(CollStreamUtil.toList(proCategoryIdParamList, ProCategoryIdParam::getId));
    }

    @Override
    public ProCategory detail(ProCategoryIdParam proCategoryIdParam) {
        ProCategory proCategory = this.getById(proCategoryIdParam.getId());
        if (ObjectUtil.isEmpty(proCategory)) {
            throw new BusinessException(ResultCode.PARAM_ERROR);
        }
        return proCategory;
    }

    @Override
    public List<LabelOption<String>> options(ProCategoryOptionParam proCategoryOptionParam) {
        QueryWrapper<ProCategory> queryWrapper = new QueryWrapper<ProCategory>().checkSqlInjection();
        // 关键字
        if (ObjectUtil.isNotEmpty(proCategoryOptionParam.getKeyword())) {
            queryWrapper.lambda().like(ProCategory::getName, proCategoryOptionParam.getKeyword());
        }
        return this.list(queryWrapper).stream().map(proCategory -> new LabelOption<>(proCategory.getId(), proCategory.getName())).toList();
    }

}