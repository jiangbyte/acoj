package io.charlie.problem.service.base.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollStreamUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.charlie.app.core.modular.set.sample.entity.ProSetSampleLibrary;
import io.charlie.app.core.modular.set.sample.param.ProSetSampleLibraryAddParam;
import io.charlie.app.core.modular.set.sample.param.ProSetSampleLibraryEditParam;
import io.charlie.app.core.modular.set.sample.param.ProSetSampleLibraryIdParam;
import io.charlie.app.core.modular.set.sample.param.ProSetSampleLibraryPageParam;
import io.charlie.app.core.modular.set.sample.mapper.ProSetSampleLibraryMapper;
import io.charlie.app.core.modular.set.sample.service.ProSetSampleLibraryService;
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
* @description 题目题集提交样本库 服务实现类
*/
@Slf4j
@Service
@RequiredArgsConstructor
public class ProSetSampleLibraryServiceImpl extends ServiceImpl<ProSetSampleLibraryMapper, ProSetSampleLibrary> implements ProSetSampleLibraryService {

    @Override
    public Page<ProSetSampleLibrary> page(ProSetSampleLibraryPageParam proSetSampleLibraryPageParam) {
        QueryWrapper<ProSetSampleLibrary> queryWrapper = new QueryWrapper<ProSetSampleLibrary>().checkSqlInjection();
        if (ObjectUtil.isAllNotEmpty(proSetSampleLibraryPageParam.getSortField(), proSetSampleLibraryPageParam.getSortOrder()) && ISortOrderEnum.isValid(proSetSampleLibraryPageParam.getSortOrder())) {
            queryWrapper.orderBy(
                    true,
                    proSetSampleLibraryPageParam.getSortOrder().equals(ISortOrderEnum.ASCEND.getValue()),
                    StrUtil.toUnderlineCase(proSetSampleLibraryPageParam.getSortField()));
        }

        return this.page(CommonPageRequest.Page(
                        Optional.ofNullable(proSetSampleLibraryPageParam.getCurrent()).orElse(1),
                        Optional.ofNullable(proSetSampleLibraryPageParam.getSize()).orElse(20),
                null
                ),
                queryWrapper);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void add(ProSetSampleLibraryAddParam proSetSampleLibraryAddParam) {
        ProSetSampleLibrary bean = BeanUtil.toBean(proSetSampleLibraryAddParam, ProSetSampleLibrary.class);
        this.save(bean);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void edit(ProSetSampleLibraryEditParam proSetSampleLibraryEditParam) {
        if (!this.exists(new LambdaQueryWrapper<ProSetSampleLibrary>().eq(ProSetSampleLibrary::getId, proSetSampleLibraryEditParam.getId()))) {
            throw new BusinessException(ResultCode.PARAM_ERROR);
        }
        ProSetSampleLibrary bean = BeanUtil.toBean(proSetSampleLibraryEditParam, ProSetSampleLibrary.class);
        BeanUtil.copyProperties(proSetSampleLibraryEditParam, bean);
        this.updateById(bean);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void delete(List<ProSetSampleLibraryIdParam> proSetSampleLibraryIdParamList) {
        if (ObjectUtil.isEmpty(proSetSampleLibraryIdParamList)) {
            throw new BusinessException(ResultCode.PARAM_ERROR);
        }
        this.removeByIds(CollStreamUtil.toList(proSetSampleLibraryIdParamList, ProSetSampleLibraryIdParam::getId));
    }

    @Override
    public ProSetSampleLibrary detail(ProSetSampleLibraryIdParam proSetSampleLibraryIdParam) {
        ProSetSampleLibrary proSetSampleLibrary = this.getById(proSetSampleLibraryIdParam.getId());
        if (ObjectUtil.isEmpty(proSetSampleLibrary)) {
            throw new BusinessException(ResultCode.PARAM_ERROR);
        }
        return proSetSampleLibrary;
    }

}