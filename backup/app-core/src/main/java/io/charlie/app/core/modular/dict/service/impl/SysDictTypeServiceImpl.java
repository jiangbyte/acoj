package io.charlie.app.core.modular.dict.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollStreamUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.charlie.app.core.modular.dict.entity.SysDictType;
import io.charlie.app.core.modular.dict.param.SysDictTypeAddParam;
import io.charlie.app.core.modular.dict.param.SysDictTypeEditParam;
import io.charlie.app.core.modular.dict.param.SysDictTypeIdParam;
import io.charlie.app.core.modular.dict.param.SysDictTypePageParam;
import io.charlie.app.core.modular.dict.mapper.SysDictTypeMapper;
import io.charlie.app.core.modular.dict.service.SysDictTypeService;
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
* @description 字典类型表 服务实现类
*/
@Slf4j
@Service
@RequiredArgsConstructor
public class SysDictTypeServiceImpl extends ServiceImpl<SysDictTypeMapper, SysDictType> implements SysDictTypeService {

    @Override
    public Page<SysDictType> page(SysDictTypePageParam sysDictTypePageParam) {
        QueryWrapper<SysDictType> queryWrapper = new QueryWrapper<SysDictType>().checkSqlInjection();
        // 关键字
        if (ObjectUtil.isNotEmpty(sysDictTypePageParam.getKeyword())) {
            queryWrapper.lambda().like(SysDictType::getTitle, sysDictTypePageParam.getKeyword());
        }
        if (ObjectUtil.isAllNotEmpty(sysDictTypePageParam.getSortField(), sysDictTypePageParam.getSortOrder()) && ISortOrderEnum.isValid(sysDictTypePageParam.getSortOrder())) {
            queryWrapper.orderBy(
                    true,
                    sysDictTypePageParam.getSortOrder().equals(ISortOrderEnum.ASCEND.getValue()),
                    StrUtil.toUnderlineCase(sysDictTypePageParam.getSortField()));
        } else {
            queryWrapper.lambda().orderByAsc(SysDictType::getSort);
        }

        return this.page(CommonPageRequest.Page(
                        Optional.ofNullable(sysDictTypePageParam.getCurrent()).orElse(1),
                        Optional.ofNullable(sysDictTypePageParam.getSize()).orElse(20),
                null
                ),
                queryWrapper);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void add(SysDictTypeAddParam sysDictTypeAddParam) {
        SysDictType bean = BeanUtil.toBean(sysDictTypeAddParam, SysDictType.class);
        this.save(bean);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void edit(SysDictTypeEditParam sysDictTypeEditParam) {
        if (!this.exists(new LambdaQueryWrapper<SysDictType>().eq(SysDictType::getId, sysDictTypeEditParam.getId()))) {
            throw new BusinessException(ResultCode.PARAM_ERROR);
        }
        SysDictType bean = BeanUtil.toBean(sysDictTypeEditParam, SysDictType.class);
        BeanUtil.copyProperties(sysDictTypeEditParam, bean);
        this.updateById(bean);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void delete(List<SysDictTypeIdParam> sysDictTypeIdParamList) {
        if (ObjectUtil.isEmpty(sysDictTypeIdParamList)) {
            throw new BusinessException(ResultCode.PARAM_ERROR);
        }
        this.removeByIds(CollStreamUtil.toList(sysDictTypeIdParamList, SysDictTypeIdParam::getId));
    }

    @Override
    public SysDictType detail(SysDictTypeIdParam sysDictTypeIdParam) {
        SysDictType sysDictType = this.getById(sysDictTypeIdParam.getId());
        if (ObjectUtil.isEmpty(sysDictType)) {
            throw new BusinessException(ResultCode.PARAM_ERROR);
        }
        return sysDictType;
    }

}