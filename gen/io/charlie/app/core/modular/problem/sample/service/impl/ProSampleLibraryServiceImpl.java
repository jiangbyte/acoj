package io.charlie.problem.service.base.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollStreamUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.charlie.app.core.modular.problem.sample.entity.ProSampleLibrary;
import io.charlie.app.core.modular.problem.sample.param.ProSampleLibraryAddParam;
import io.charlie.app.core.modular.problem.sample.param.ProSampleLibraryEditParam;
import io.charlie.app.core.modular.problem.sample.param.ProSampleLibraryIdParam;
import io.charlie.app.core.modular.problem.sample.param.ProSampleLibraryPageParam;
import io.charlie.app.core.modular.problem.sample.mapper.ProSampleLibraryMapper;
import io.charlie.app.core.modular.problem.sample.service.ProSampleLibraryService;
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
* @description 题目提交样本库 服务实现类
*/
@Slf4j
@Service
@RequiredArgsConstructor
public class ProSampleLibraryServiceImpl extends ServiceImpl<ProSampleLibraryMapper, ProSampleLibrary> implements ProSampleLibraryService {

    @Override
    public Page<ProSampleLibrary> page(ProSampleLibraryPageParam proSampleLibraryPageParam) {
        QueryWrapper<ProSampleLibrary> queryWrapper = new QueryWrapper<ProSampleLibrary>().checkSqlInjection();
        if (ObjectUtil.isAllNotEmpty(proSampleLibraryPageParam.getSortField(), proSampleLibraryPageParam.getSortOrder()) && ISortOrderEnum.isValid(proSampleLibraryPageParam.getSortOrder())) {
            queryWrapper.orderBy(
                    true,
                    proSampleLibraryPageParam.getSortOrder().equals(ISortOrderEnum.ASCEND.getValue()),
                    StrUtil.toUnderlineCase(proSampleLibraryPageParam.getSortField()));
        }

        return this.page(CommonPageRequest.Page(
                        Optional.ofNullable(proSampleLibraryPageParam.getCurrent()).orElse(1),
                        Optional.ofNullable(proSampleLibraryPageParam.getSize()).orElse(20),
                null
                ),
                queryWrapper);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void add(ProSampleLibraryAddParam proSampleLibraryAddParam) {
        ProSampleLibrary bean = BeanUtil.toBean(proSampleLibraryAddParam, ProSampleLibrary.class);
        this.save(bean);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void edit(ProSampleLibraryEditParam proSampleLibraryEditParam) {
        if (!this.exists(new LambdaQueryWrapper<ProSampleLibrary>().eq(ProSampleLibrary::getId, proSampleLibraryEditParam.getId()))) {
            throw new BusinessException(ResultCode.PARAM_ERROR);
        }
        ProSampleLibrary bean = BeanUtil.toBean(proSampleLibraryEditParam, ProSampleLibrary.class);
        BeanUtil.copyProperties(proSampleLibraryEditParam, bean);
        this.updateById(bean);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void delete(List<ProSampleLibraryIdParam> proSampleLibraryIdParamList) {
        if (ObjectUtil.isEmpty(proSampleLibraryIdParamList)) {
            throw new BusinessException(ResultCode.PARAM_ERROR);
        }
        this.removeByIds(CollStreamUtil.toList(proSampleLibraryIdParamList, ProSampleLibraryIdParam::getId));
    }

    @Override
    public ProSampleLibrary detail(ProSampleLibraryIdParam proSampleLibraryIdParam) {
        ProSampleLibrary proSampleLibrary = this.getById(proSampleLibraryIdParam.getId());
        if (ObjectUtil.isEmpty(proSampleLibrary)) {
            throw new BusinessException(ResultCode.PARAM_ERROR);
        }
        return proSampleLibrary;
    }

}