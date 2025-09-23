package io.charlie.web.oj.modular.data.solved.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollStreamUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.charlie.web.oj.modular.data.solved.entity.DataSolved;
import io.charlie.web.oj.modular.data.solved.param.DataSolvedAddParam;
import io.charlie.web.oj.modular.data.solved.param.DataSolvedEditParam;
import io.charlie.web.oj.modular.data.solved.param.DataSolvedIdParam;
import io.charlie.web.oj.modular.data.solved.param.DataSolvedPageParam;
import io.charlie.web.oj.modular.data.solved.mapper.DataSolvedMapper;
import io.charlie.web.oj.modular.data.solved.service.DataSolvedService;
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
* @description 用户解决表 服务实现类
*/
@Slf4j
@Service
@RequiredArgsConstructor
public class DataSolvedServiceImpl extends ServiceImpl<DataSolvedMapper, DataSolved> implements DataSolvedService {

    @Override
    public Page<DataSolved> page(DataSolvedPageParam dataSolvedPageParam) {
        QueryWrapper<DataSolved> queryWrapper = new QueryWrapper<DataSolved>().checkSqlInjection();
        if (ObjectUtil.isAllNotEmpty(dataSolvedPageParam.getSortField(), dataSolvedPageParam.getSortOrder()) && ISortOrderEnum.isValid(dataSolvedPageParam.getSortOrder())) {
            queryWrapper.orderBy(
                    true,
                    dataSolvedPageParam.getSortOrder().equals(ISortOrderEnum.ASCEND.getValue()),
                    StrUtil.toUnderlineCase(dataSolvedPageParam.getSortField()));
        }

        return this.page(CommonPageRequest.Page(
                        Optional.ofNullable(dataSolvedPageParam.getCurrent()).orElse(1),
                        Optional.ofNullable(dataSolvedPageParam.getSize()).orElse(20),
                null
                ),
                queryWrapper);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void add(DataSolvedAddParam dataSolvedAddParam) {
        DataSolved bean = BeanUtil.toBean(dataSolvedAddParam, DataSolved.class);
        this.save(bean);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void edit(DataSolvedEditParam dataSolvedEditParam) {
        if (!this.exists(new LambdaQueryWrapper<DataSolved>().eq(DataSolved::getId, dataSolvedEditParam.getId()))) {
            throw new BusinessException(ResultCode.PARAM_ERROR);
        }
        DataSolved bean = BeanUtil.toBean(dataSolvedEditParam, DataSolved.class);
        BeanUtil.copyProperties(dataSolvedEditParam, bean);
        this.updateById(bean);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void delete(List<DataSolvedIdParam> dataSolvedIdParamList) {
        if (ObjectUtil.isEmpty(dataSolvedIdParamList)) {
            throw new BusinessException(ResultCode.PARAM_ERROR);
        }
        this.removeByIds(CollStreamUtil.toList(dataSolvedIdParamList, DataSolvedIdParam::getId));
    }

    @Override
    public DataSolved detail(DataSolvedIdParam dataSolvedIdParam) {
        DataSolved dataSolved = this.getById(dataSolvedIdParam.getId());
        if (ObjectUtil.isEmpty(dataSolved)) {
            throw new BusinessException(ResultCode.PARAM_ERROR);
        }
        return dataSolved;
    }

}