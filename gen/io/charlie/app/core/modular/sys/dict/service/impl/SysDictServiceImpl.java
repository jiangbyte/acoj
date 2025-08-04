package io.charlie.problem.service.base.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollStreamUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.charlie.app.core.modular.sys.dict.entity.SysDict;
import io.charlie.app.core.modular.sys.dict.param.SysDictAddParam;
import io.charlie.app.core.modular.sys.dict.param.SysDictEditParam;
import io.charlie.app.core.modular.sys.dict.param.SysDictIdParam;
import io.charlie.app.core.modular.sys.dict.param.SysDictPageParam;
import io.charlie.app.core.modular.sys.dict.mapper.SysDictMapper;
import io.charlie.app.core.modular.sys.dict.service.SysDictService;
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
* @description 系统字典表 服务实现类
*/
@Slf4j
@Service
@RequiredArgsConstructor
public class SysDictServiceImpl extends ServiceImpl<SysDictMapper, SysDict> implements SysDictService {

    @Override
    public Page<SysDict> page(SysDictPageParam sysDictPageParam) {
        QueryWrapper<SysDict> queryWrapper = new QueryWrapper<SysDict>().checkSqlInjection();
        if (ObjectUtil.isAllNotEmpty(sysDictPageParam.getSortField(), sysDictPageParam.getSortOrder()) && ISortOrderEnum.isValid(sysDictPageParam.getSortOrder())) {
            queryWrapper.orderBy(
                    true,
                    sysDictPageParam.getSortOrder().equals(ISortOrderEnum.ASCEND.getValue()),
                    StrUtil.toUnderlineCase(sysDictPageParam.getSortField()));
        }

        return this.page(CommonPageRequest.Page(
                        Optional.ofNullable(sysDictPageParam.getCurrent()).orElse(1),
                        Optional.ofNullable(sysDictPageParam.getSize()).orElse(20),
                null
                ),
                queryWrapper);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void add(SysDictAddParam sysDictAddParam) {
        SysDict bean = BeanUtil.toBean(sysDictAddParam, SysDict.class);
        this.save(bean);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void edit(SysDictEditParam sysDictEditParam) {
        if (!this.exists(new LambdaQueryWrapper<SysDict>().eq(SysDict::getId, sysDictEditParam.getId()))) {
            throw new BusinessException(ResultCode.PARAM_ERROR);
        }
        SysDict bean = BeanUtil.toBean(sysDictEditParam, SysDict.class);
        BeanUtil.copyProperties(sysDictEditParam, bean);
        this.updateById(bean);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void delete(List<SysDictIdParam> sysDictIdParamList) {
        if (ObjectUtil.isEmpty(sysDictIdParamList)) {
            throw new BusinessException(ResultCode.PARAM_ERROR);
        }
        this.removeByIds(CollStreamUtil.toList(sysDictIdParamList, SysDictIdParam::getId));
    }

    @Override
    public SysDict detail(SysDictIdParam sysDictIdParam) {
        SysDict sysDict = this.getById(sysDictIdParam.getId());
        if (ObjectUtil.isEmpty(sysDict)) {
            throw new BusinessException(ResultCode.PARAM_ERROR);
        }
        return sysDict;
    }

}