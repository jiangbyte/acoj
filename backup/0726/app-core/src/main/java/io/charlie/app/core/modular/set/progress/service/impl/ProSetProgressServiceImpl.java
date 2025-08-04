package io.charlie.app.core.modular.set.progress.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollStreamUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.charlie.app.core.modular.set.progress.entity.ProSetProgress;
import io.charlie.app.core.modular.set.progress.param.ProSetProgressAddParam;
import io.charlie.app.core.modular.set.progress.param.ProSetProgressEditParam;
import io.charlie.app.core.modular.set.progress.param.ProSetProgressIdParam;
import io.charlie.app.core.modular.set.progress.param.ProSetProgressPageParam;
import io.charlie.app.core.modular.set.progress.mapper.ProSetProgressMapper;
import io.charlie.app.core.modular.set.progress.service.ProSetProgressService;
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
* @description 题集进度表 服务实现类
*/
@Slf4j
@Service
@RequiredArgsConstructor
public class ProSetProgressServiceImpl extends ServiceImpl<ProSetProgressMapper, ProSetProgress> implements ProSetProgressService {

    @Override
    public Page<ProSetProgress> page(ProSetProgressPageParam proSetProgressPageParam) {
        QueryWrapper<ProSetProgress> queryWrapper = new QueryWrapper<ProSetProgress>().checkSqlInjection();
        if (ObjectUtil.isAllNotEmpty(proSetProgressPageParam.getSortField(), proSetProgressPageParam.getSortOrder()) && ISortOrderEnum.isValid(proSetProgressPageParam.getSortOrder())) {
            queryWrapper.orderBy(
                    true,
                    proSetProgressPageParam.getSortOrder().equals(ISortOrderEnum.ASCEND.getValue()),
                    StrUtil.toUnderlineCase(proSetProgressPageParam.getSortField()));
        }

        return this.page(CommonPageRequest.Page(
                        Optional.ofNullable(proSetProgressPageParam.getCurrent()).orElse(1),
                        Optional.ofNullable(proSetProgressPageParam.getSize()).orElse(20),
                null
                ),
                queryWrapper);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void add(ProSetProgressAddParam proSetProgressAddParam) {
        ProSetProgress bean = BeanUtil.toBean(proSetProgressAddParam, ProSetProgress.class);
        this.save(bean);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void edit(ProSetProgressEditParam proSetProgressEditParam) {
        if (!this.exists(new LambdaQueryWrapper<ProSetProgress>().eq(ProSetProgress::getId, proSetProgressEditParam.getId()))) {
            throw new BusinessException(ResultCode.PARAM_ERROR);
        }
        ProSetProgress bean = BeanUtil.toBean(proSetProgressEditParam, ProSetProgress.class);
        BeanUtil.copyProperties(proSetProgressEditParam, bean);
        this.updateById(bean);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void delete(List<ProSetProgressIdParam> proSetProgressIdParamList) {
        if (ObjectUtil.isEmpty(proSetProgressIdParamList)) {
            throw new BusinessException(ResultCode.PARAM_ERROR);
        }
        this.removeByIds(CollStreamUtil.toList(proSetProgressIdParamList, ProSetProgressIdParam::getId));
    }

    @Override
    public ProSetProgress detail(ProSetProgressIdParam proSetProgressIdParam) {
        ProSetProgress proSetProgress = this.getById(proSetProgressIdParam.getId());
        if (ObjectUtil.isEmpty(proSetProgress)) {
            throw new BusinessException(ResultCode.PARAM_ERROR);
        }
        return proSetProgress;
    }

}