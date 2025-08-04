package io.charlie.app.core.modular.sys.group.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollStreamUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.charlie.app.core.modular.sys.group.entity.SysGroup;
import io.charlie.app.core.modular.sys.group.param.*;
import io.charlie.app.core.modular.sys.group.mapper.SysGroupMapper;
import io.charlie.app.core.modular.sys.group.service.SysGroupService;
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
* @description 用户组表 服务实现类
*/
@Slf4j
@Service
@RequiredArgsConstructor
public class SysGroupServiceImpl extends ServiceImpl<SysGroupMapper, SysGroup> implements SysGroupService {

    @Override
    public Page<SysGroup> page(SysGroupPageParam sysGroupPageParam) {
        QueryWrapper<SysGroup> queryWrapper = new QueryWrapper<SysGroup>().checkSqlInjection();
        // 关键字
        if (ObjectUtil.isNotEmpty(sysGroupPageParam.getKeyword())) {
            queryWrapper.lambda().like(SysGroup::getName, sysGroupPageParam.getKeyword());
        }
        if (ObjectUtil.isAllNotEmpty(sysGroupPageParam.getSortField(), sysGroupPageParam.getSortOrder()) && ISortOrderEnum.isValid(sysGroupPageParam.getSortOrder())) {
            queryWrapper.orderBy(
                    true,
                    sysGroupPageParam.getSortOrder().equals(ISortOrderEnum.ASCEND.getValue()),
                    StrUtil.toUnderlineCase(sysGroupPageParam.getSortField()));
        } else {
            queryWrapper.lambda().orderByAsc(SysGroup::getSort);
        }

        return this.page(CommonPageRequest.Page(
                        Optional.ofNullable(sysGroupPageParam.getCurrent()).orElse(1),
                        Optional.ofNullable(sysGroupPageParam.getSize()).orElse(20),
                null
                ),
                queryWrapper);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void add(SysGroupAddParam sysGroupAddParam) {
        SysGroup bean = BeanUtil.toBean(sysGroupAddParam, SysGroup.class);
        this.save(bean);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void edit(SysGroupEditParam sysGroupEditParam) {
        if (!this.exists(new LambdaQueryWrapper<SysGroup>().eq(SysGroup::getId, sysGroupEditParam.getId()))) {
            throw new BusinessException(ResultCode.PARAM_ERROR);
        }
        SysGroup bean = BeanUtil.toBean(sysGroupEditParam, SysGroup.class);
        BeanUtil.copyProperties(sysGroupEditParam, bean);
        // 自己不能是自己的父级
        if (bean.getParentId().equals(bean.getId())) {
            throw new BusinessException("父级不能是自己");
        }
        this.updateById(bean);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void delete(List<SysGroupIdParam> sysGroupIdParamList) {
        if (ObjectUtil.isEmpty(sysGroupIdParamList)) {
            throw new BusinessException(ResultCode.PARAM_ERROR);
        }
        this.removeByIds(CollStreamUtil.toList(sysGroupIdParamList, SysGroupIdParam::getId));
    }

    @Override
    public SysGroup detail(SysGroupIdParam sysGroupIdParam) {
        SysGroup sysGroup = this.getById(sysGroupIdParam.getId());
        if (ObjectUtil.isEmpty(sysGroup)) {
            throw new BusinessException(ResultCode.PARAM_ERROR);
        }
        return sysGroup;
    }

    @Override
    public List<LabelOption<String>> options(SysGroupOptionParam sysGroupOptionParam) {
        QueryWrapper<SysGroup> queryWrapper = new QueryWrapper<SysGroup>().checkSqlInjection();
        // 关键字
        if (ObjectUtil.isNotEmpty(sysGroupOptionParam.getKeyword())) {
            queryWrapper.lambda().like(SysGroup::getName, sysGroupOptionParam.getKeyword());
        }
        return this.list(queryWrapper).stream().map(sysGroup -> new LabelOption<>(sysGroup.getId(), sysGroup.getName())).toList();
    }

}