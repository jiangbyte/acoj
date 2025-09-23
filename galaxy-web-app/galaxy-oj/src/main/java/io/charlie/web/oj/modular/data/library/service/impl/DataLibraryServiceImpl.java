package io.charlie.web.oj.modular.data.library.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollStreamUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.charlie.web.oj.modular.data.library.entity.DataLibrary;
import io.charlie.web.oj.modular.data.library.param.DataLibraryAddParam;
import io.charlie.web.oj.modular.data.library.param.DataLibraryEditParam;
import io.charlie.web.oj.modular.data.library.param.DataLibraryIdParam;
import io.charlie.web.oj.modular.data.library.param.DataLibraryPageParam;
import io.charlie.web.oj.modular.data.library.mapper.DataLibraryMapper;
import io.charlie.web.oj.modular.data.library.service.DataLibraryService;
import io.charlie.galaxy.enums.ISortOrderEnum;
import io.charlie.galaxy.exception.BusinessException;
import io.charlie.galaxy.pojo.CommonPageRequest;
import io.charlie.galaxy.result.ResultCode;
import io.charlie.web.oj.modular.data.submit.entity.DataSubmit;
import org.springframework.stereotype.Service;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

/**
* @author Charlie Zhang
* @version v1.0
* @date 2025-06-23
* @description 提交样本库 服务实现类
*/
@Slf4j
@Service
@RequiredArgsConstructor
public class DataLibraryServiceImpl extends ServiceImpl<DataLibraryMapper, DataLibrary> implements DataLibraryService {

    @Override
    public Page<DataLibrary> page(DataLibraryPageParam dataLibraryPageParam) {
        QueryWrapper<DataLibrary> queryWrapper = new QueryWrapper<DataLibrary>().checkSqlInjection();
        if (ObjectUtil.isAllNotEmpty(dataLibraryPageParam.getSortField(), dataLibraryPageParam.getSortOrder()) && ISortOrderEnum.isValid(dataLibraryPageParam.getSortOrder())) {
            queryWrapper.orderBy(
                    true,
                    dataLibraryPageParam.getSortOrder().equals(ISortOrderEnum.ASCEND.getValue()),
                    StrUtil.toUnderlineCase(dataLibraryPageParam.getSortField()));
        }

        return this.page(CommonPageRequest.Page(
                        Optional.ofNullable(dataLibraryPageParam.getCurrent()).orElse(1),
                        Optional.ofNullable(dataLibraryPageParam.getSize()).orElse(20),
                null
                ),
                queryWrapper);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void add(DataLibraryAddParam dataLibraryAddParam) {
        DataLibrary bean = BeanUtil.toBean(dataLibraryAddParam, DataLibrary.class);
        this.save(bean);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void edit(DataLibraryEditParam dataLibraryEditParam) {
        if (!this.exists(new LambdaQueryWrapper<DataLibrary>().eq(DataLibrary::getId, dataLibraryEditParam.getId()))) {
            throw new BusinessException(ResultCode.PARAM_ERROR);
        }
        DataLibrary bean = BeanUtil.toBean(dataLibraryEditParam, DataLibrary.class);
        BeanUtil.copyProperties(dataLibraryEditParam, bean);
        this.updateById(bean);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void delete(List<DataLibraryIdParam> dataLibraryIdParamList) {
        if (ObjectUtil.isEmpty(dataLibraryIdParamList)) {
            throw new BusinessException(ResultCode.PARAM_ERROR);
        }
        this.removeByIds(CollStreamUtil.toList(dataLibraryIdParamList, DataLibraryIdParam::getId));
    }

    @Override
    public DataLibrary detail(DataLibraryIdParam dataLibraryIdParam) {
        DataLibrary dataLibrary = this.getById(dataLibraryIdParam.getId());
        if (ObjectUtil.isEmpty(dataLibrary)) {
            throw new BusinessException(ResultCode.PARAM_ERROR);
        }
        return dataLibrary;
    }

    @Override
    public void addLibrary(DataSubmit submit) {

    }

}