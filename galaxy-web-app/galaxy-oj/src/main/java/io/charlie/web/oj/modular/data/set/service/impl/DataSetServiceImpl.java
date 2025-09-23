package io.charlie.web.oj.modular.data.set.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollStreamUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.charlie.web.oj.modular.data.set.entity.DataSet;
import io.charlie.web.oj.modular.data.set.param.DataSetAddParam;
import io.charlie.web.oj.modular.data.set.param.DataSetEditParam;
import io.charlie.web.oj.modular.data.set.param.DataSetIdParam;
import io.charlie.web.oj.modular.data.set.param.DataSetPageParam;
import io.charlie.web.oj.modular.data.set.mapper.DataSetMapper;
import io.charlie.web.oj.modular.data.set.service.DataSetService;
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
 * @description 题集 服务实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DataSetServiceImpl extends ServiceImpl<DataSetMapper, DataSet> implements DataSetService {

    @Override
    public Page<DataSet> page(DataSetPageParam dataSetPageParam) {
        QueryWrapper<DataSet> queryWrapper = new QueryWrapper<DataSet>().checkSqlInjection();
        // 关键字
        if (ObjectUtil.isNotEmpty(dataSetPageParam.getKeyword())) {
            queryWrapper.lambda().like(DataSet::getTitle, dataSetPageParam.getKeyword());
        }
        if (ObjectUtil.isAllNotEmpty(dataSetPageParam.getSortField(), dataSetPageParam.getSortOrder()) && ISortOrderEnum.isValid(dataSetPageParam.getSortOrder())) {
            queryWrapper.orderBy(
                    true,
                    dataSetPageParam.getSortOrder().equals(ISortOrderEnum.ASCEND.getValue()),
                    StrUtil.toUnderlineCase(dataSetPageParam.getSortField()));
        }

        return this.page(CommonPageRequest.Page(
                        Optional.ofNullable(dataSetPageParam.getCurrent()).orElse(1),
                        Optional.ofNullable(dataSetPageParam.getSize()).orElse(20),
                        null
                ),
                queryWrapper);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void add(DataSetAddParam dataSetAddParam) {
        DataSet bean = BeanUtil.toBean(dataSetAddParam, DataSet.class);
        this.save(bean);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void edit(DataSetEditParam dataSetEditParam) {
        if (!this.exists(new LambdaQueryWrapper<DataSet>().eq(DataSet::getId, dataSetEditParam.getId()))) {
            throw new BusinessException(ResultCode.PARAM_ERROR);
        }
        DataSet bean = BeanUtil.toBean(dataSetEditParam, DataSet.class);
        BeanUtil.copyProperties(dataSetEditParam, bean);
        this.updateById(bean);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void delete(List<DataSetIdParam> dataSetIdParamList) {
        if (ObjectUtil.isEmpty(dataSetIdParamList)) {
            throw new BusinessException(ResultCode.PARAM_ERROR);
        }
        this.removeByIds(CollStreamUtil.toList(dataSetIdParamList, DataSetIdParam::getId));
    }

    @Override
    public DataSet detail(DataSetIdParam dataSetIdParam) {
        DataSet dataSet = this.getById(dataSetIdParam.getId());
        if (ObjectUtil.isEmpty(dataSet)) {
            throw new BusinessException(ResultCode.PARAM_ERROR);
        }
        return dataSet;
    }

    @Override
    public List<DataSet> latestN(int n) {
        List<DataSet> list = this.list(new QueryWrapper<DataSet>().checkSqlInjection()
                .lambda()
                .eq(DataSet::getIsVisible, true)
                .orderByAsc(DataSet::getCreateTime)
                .last("LIMIT " + n)
        );
        return list;
    }

}