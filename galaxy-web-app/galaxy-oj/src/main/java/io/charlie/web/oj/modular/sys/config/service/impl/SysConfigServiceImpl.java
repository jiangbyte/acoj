package io.charlie.web.oj.modular.sys.config.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollStreamUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.charlie.web.oj.modular.sys.config.entity.SysConfig;
import io.charlie.web.oj.modular.sys.config.param.*;
import io.charlie.web.oj.modular.sys.config.mapper.SysConfigMapper;
import io.charlie.web.oj.modular.sys.config.service.SysConfigService;
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
* @description 系统配置表 服务实现类
*/
@Slf4j
@Service
@RequiredArgsConstructor
public class SysConfigServiceImpl extends ServiceImpl<SysConfigMapper, SysConfig> implements SysConfigService {

    @Override
    public Page<SysConfig> page(SysConfigPageParam sysConfigPageParam) {
        QueryWrapper<SysConfig> queryWrapper = new QueryWrapper<SysConfig>().checkSqlInjection();
        // 关键字
        if (ObjectUtil.isNotEmpty(sysConfigPageParam.getKeyword())) {
            queryWrapper.lambda().like(SysConfig::getName, sysConfigPageParam.getKeyword());
        }
        // 先按 configType 排序，然后按 name 排序
        queryWrapper.lambda()
                .orderByAsc(SysConfig::getConfigType)
                .orderByAsc(SysConfig::getName);

        if (ObjectUtil.isAllNotEmpty(sysConfigPageParam.getSortField(), sysConfigPageParam.getSortOrder()) && ISortOrderEnum.isValid(sysConfigPageParam.getSortOrder())) {
            queryWrapper.orderBy(
                    true,
                    sysConfigPageParam.getSortOrder().equals(ISortOrderEnum.ASCEND.getValue()),
                    StrUtil.toUnderlineCase(sysConfigPageParam.getSortField()));
        }

        return this.page(CommonPageRequest.Page(
                        Optional.ofNullable(sysConfigPageParam.getCurrent()).orElse(1),
                        Optional.ofNullable(sysConfigPageParam.getSize()).orElse(20),
                null
                ),
                queryWrapper);
    }

    @Override
    public List<SysConfig> listAll(SysConfigListParam sysConfigListParam) {
        QueryWrapper<SysConfig> queryWrapper = new QueryWrapper<SysConfig>().checkSqlInjection();
        // 关键字
        if (ObjectUtil.isNotEmpty(sysConfigListParam.getKeyword())) {
            queryWrapper.lambda().like(SysConfig::getName, sysConfigListParam.getKeyword());
        }
        // 先按 configType 排序，然后按 name 排序
        queryWrapper.lambda()
                .orderByAsc(SysConfig::getConfigType)
                .orderByAsc(SysConfig::getName);
        return this.list(queryWrapper);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void add(SysConfigAddParam sysConfigAddParam) {
        SysConfig bean = BeanUtil.toBean(sysConfigAddParam, SysConfig.class);
        this.save(bean);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void edit(SysConfigEditParam sysConfigEditParam) {
        if (!this.exists(new LambdaQueryWrapper<SysConfig>().eq(SysConfig::getId, sysConfigEditParam.getId()))) {
            throw new BusinessException(ResultCode.PARAM_ERROR);
        }
        SysConfig bean = BeanUtil.toBean(sysConfigEditParam, SysConfig.class);
        BeanUtil.copyProperties(sysConfigEditParam, bean);
        this.updateById(bean);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void delete(List<SysConfigIdParam> sysConfigIdParamList) {
        if (ObjectUtil.isEmpty(sysConfigIdParamList)) {
            throw new BusinessException(ResultCode.PARAM_ERROR);
        }
        this.removeByIds(CollStreamUtil.toList(sysConfigIdParamList, SysConfigIdParam::getId));
    }

    @Override
    public SysConfig detail(SysConfigIdParam sysConfigIdParam) {
        SysConfig sysConfig = this.getById(sysConfigIdParam.getId());
        if (ObjectUtil.isEmpty(sysConfig)) {
            throw new BusinessException(ResultCode.PARAM_ERROR);
        }
        return sysConfig;
    }

    @Override
    public String getValueByCode(String code) {
        SysConfig sysConfig = this.getOne(new LambdaQueryWrapper<SysConfig>().eq(SysConfig::getCode, code));
        if (ObjectUtil.isNotEmpty(sysConfig)) {
            return sysConfig.getValue();
        }
        return "";
    }

}