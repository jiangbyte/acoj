package io.charlie.app.core.modular.sys.banner.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollStreamUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.charlie.app.core.modular.sys.banner.entity.SysBanner;
import io.charlie.app.core.modular.sys.banner.param.SysBannerAddParam;
import io.charlie.app.core.modular.sys.banner.param.SysBannerEditParam;
import io.charlie.app.core.modular.sys.banner.param.SysBannerIdParam;
import io.charlie.app.core.modular.sys.banner.param.SysBannerPageParam;
import io.charlie.app.core.modular.sys.banner.mapper.SysBannerMapper;
import io.charlie.app.core.modular.sys.banner.service.SysBannerService;
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
* @description 横幅表 服务实现类
*/
@Slf4j
@Service
@RequiredArgsConstructor
public class SysBannerServiceImpl extends ServiceImpl<SysBannerMapper, SysBanner> implements SysBannerService {

    @Override
    public Page<SysBanner> page(SysBannerPageParam sysBannerPageParam) {
        QueryWrapper<SysBanner> queryWrapper = new QueryWrapper<SysBanner>().checkSqlInjection();
        // 关键字
        if (ObjectUtil.isNotEmpty(sysBannerPageParam.getKeyword())) {
            queryWrapper.lambda().like(SysBanner::getTitle, sysBannerPageParam.getKeyword());
        }
        if (ObjectUtil.isAllNotEmpty(sysBannerPageParam.getSortField(), sysBannerPageParam.getSortOrder()) && ISortOrderEnum.isValid(sysBannerPageParam.getSortOrder())) {
            queryWrapper.orderBy(
                    true,
                    sysBannerPageParam.getSortOrder().equals(ISortOrderEnum.ASCEND.getValue()),
                    StrUtil.toUnderlineCase(sysBannerPageParam.getSortField()));
        } else {
            queryWrapper.lambda().orderByAsc(SysBanner::getSort);
        }

        return this.page(CommonPageRequest.Page(
                        Optional.ofNullable(sysBannerPageParam.getCurrent()).orElse(1),
                        Optional.ofNullable(sysBannerPageParam.getSize()).orElse(20),
                null
                ),
                queryWrapper);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void add(SysBannerAddParam sysBannerAddParam) {
        SysBanner bean = BeanUtil.toBean(sysBannerAddParam, SysBanner.class);
        this.save(bean);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void edit(SysBannerEditParam sysBannerEditParam) {
        if (!this.exists(new LambdaQueryWrapper<SysBanner>().eq(SysBanner::getId, sysBannerEditParam.getId()))) {
            throw new BusinessException(ResultCode.PARAM_ERROR);
        }
        SysBanner bean = BeanUtil.toBean(sysBannerEditParam, SysBanner.class);
        BeanUtil.copyProperties(sysBannerEditParam, bean);
        this.updateById(bean);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void delete(List<SysBannerIdParam> sysBannerIdParamList) {
        if (ObjectUtil.isEmpty(sysBannerIdParamList)) {
            throw new BusinessException(ResultCode.PARAM_ERROR);
        }
        this.removeByIds(CollStreamUtil.toList(sysBannerIdParamList, SysBannerIdParam::getId));
    }

    @Override
    public SysBanner detail(SysBannerIdParam sysBannerIdParam) {
        SysBanner sysBanner = this.getById(sysBannerIdParam.getId());
        if (ObjectUtil.isEmpty(sysBanner)) {
            throw new BusinessException(ResultCode.PARAM_ERROR);
        }
        return sysBanner;
    }

}