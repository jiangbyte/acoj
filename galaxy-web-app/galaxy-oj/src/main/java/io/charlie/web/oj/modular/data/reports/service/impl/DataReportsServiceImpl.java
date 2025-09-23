package io.charlie.web.oj.modular.data.reports.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollStreamUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.charlie.web.oj.modular.data.reports.entity.DataReports;
import io.charlie.web.oj.modular.data.reports.param.DataReportsAddParam;
import io.charlie.web.oj.modular.data.reports.param.DataReportsEditParam;
import io.charlie.web.oj.modular.data.reports.param.DataReportsIdParam;
import io.charlie.web.oj.modular.data.reports.param.DataReportsPageParam;
import io.charlie.web.oj.modular.data.reports.mapper.DataReportsMapper;
import io.charlie.web.oj.modular.data.reports.service.DataReportsService;
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
* @description 报告库表 服务实现类
*/
@Slf4j
@Service
@RequiredArgsConstructor
public class DataReportsServiceImpl extends ServiceImpl<DataReportsMapper, DataReports> implements DataReportsService {

    @Override
    public Page<DataReports> page(DataReportsPageParam dataReportsPageParam) {
        QueryWrapper<DataReports> queryWrapper = new QueryWrapper<DataReports>().checkSqlInjection();
        if (ObjectUtil.isAllNotEmpty(dataReportsPageParam.getSortField(), dataReportsPageParam.getSortOrder()) && ISortOrderEnum.isValid(dataReportsPageParam.getSortOrder())) {
            queryWrapper.orderBy(
                    true,
                    dataReportsPageParam.getSortOrder().equals(ISortOrderEnum.ASCEND.getValue()),
                    StrUtil.toUnderlineCase(dataReportsPageParam.getSortField()));
        }

        return this.page(CommonPageRequest.Page(
                        Optional.ofNullable(dataReportsPageParam.getCurrent()).orElse(1),
                        Optional.ofNullable(dataReportsPageParam.getSize()).orElse(20),
                null
                ),
                queryWrapper);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void add(DataReportsAddParam dataReportsAddParam) {
        DataReports bean = BeanUtil.toBean(dataReportsAddParam, DataReports.class);
        this.save(bean);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void edit(DataReportsEditParam dataReportsEditParam) {
        if (!this.exists(new LambdaQueryWrapper<DataReports>().eq(DataReports::getId, dataReportsEditParam.getId()))) {
            throw new BusinessException(ResultCode.PARAM_ERROR);
        }
        DataReports bean = BeanUtil.toBean(dataReportsEditParam, DataReports.class);
        BeanUtil.copyProperties(dataReportsEditParam, bean);
        this.updateById(bean);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void delete(List<DataReportsIdParam> dataReportsIdParamList) {
        if (ObjectUtil.isEmpty(dataReportsIdParamList)) {
            throw new BusinessException(ResultCode.PARAM_ERROR);
        }
        this.removeByIds(CollStreamUtil.toList(dataReportsIdParamList, DataReportsIdParam::getId));
    }

    @Override
    public DataReports detail(DataReportsIdParam dataReportsIdParam) {
        DataReports dataReports = this.getById(dataReportsIdParam.getId());
        if (ObjectUtil.isEmpty(dataReports)) {
            throw new BusinessException(ResultCode.PARAM_ERROR);
        }
        return dataReports;
    }

}