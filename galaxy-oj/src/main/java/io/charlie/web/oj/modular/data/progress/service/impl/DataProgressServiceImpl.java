package io.charlie.web.oj.modular.data.progress.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollStreamUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.charlie.web.oj.modular.data.progress.entity.DataProgress;
import io.charlie.web.oj.modular.data.progress.param.DataProgressAddParam;
import io.charlie.web.oj.modular.data.progress.param.DataProgressEditParam;
import io.charlie.web.oj.modular.data.progress.param.DataProgressIdParam;
import io.charlie.web.oj.modular.data.progress.param.DataProgressPageParam;
import io.charlie.web.oj.modular.data.progress.mapper.DataProgressMapper;
import io.charlie.web.oj.modular.data.progress.service.DataProgressService;
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
public class DataProgressServiceImpl extends ServiceImpl<DataProgressMapper, DataProgress> implements DataProgressService {

    @Override
    public Page<DataProgress> page(DataProgressPageParam dataProgressPageParam) {
        QueryWrapper<DataProgress> queryWrapper = new QueryWrapper<DataProgress>().checkSqlInjection();
        if (ObjectUtil.isAllNotEmpty(dataProgressPageParam.getSortField(), dataProgressPageParam.getSortOrder()) && ISortOrderEnum.isValid(dataProgressPageParam.getSortOrder())) {
            queryWrapper.orderBy(
                    true,
                    dataProgressPageParam.getSortOrder().equals(ISortOrderEnum.ASCEND.getValue()),
                    StrUtil.toUnderlineCase(dataProgressPageParam.getSortField()));
        }

        return this.page(CommonPageRequest.Page(
                        Optional.ofNullable(dataProgressPageParam.getCurrent()).orElse(1),
                        Optional.ofNullable(dataProgressPageParam.getSize()).orElse(20),
                null
                ),
                queryWrapper);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void add(DataProgressAddParam dataProgressAddParam) {
        DataProgress bean = BeanUtil.toBean(dataProgressAddParam, DataProgress.class);
        this.save(bean);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void edit(DataProgressEditParam dataProgressEditParam) {
        if (!this.exists(new LambdaQueryWrapper<DataProgress>().eq(DataProgress::getId, dataProgressEditParam.getId()))) {
            throw new BusinessException(ResultCode.PARAM_ERROR);
        }
        DataProgress bean = BeanUtil.toBean(dataProgressEditParam, DataProgress.class);
        BeanUtil.copyProperties(dataProgressEditParam, bean);
        this.updateById(bean);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void delete(List<DataProgressIdParam> dataProgressIdParamList) {
        if (ObjectUtil.isEmpty(dataProgressIdParamList)) {
            throw new BusinessException(ResultCode.PARAM_ERROR);
        }
        this.removeByIds(CollStreamUtil.toList(dataProgressIdParamList, DataProgressIdParam::getId));
    }

    @Override
    public DataProgress detail(DataProgressIdParam dataProgressIdParam) {
        DataProgress dataProgress = this.getById(dataProgressIdParam.getId());
        if (ObjectUtil.isEmpty(dataProgress)) {
            throw new BusinessException(ResultCode.PARAM_ERROR);
        }
        return dataProgress;
    }

}