package io.charlie.web.oj.modular.data.contest.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollStreamUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.charlie.web.oj.modular.data.contest.entity.DataContestProblem;
import io.charlie.web.oj.modular.data.contest.param.DataContestProblemAddParam;
import io.charlie.web.oj.modular.data.contest.param.DataContestProblemEditParam;
import io.charlie.web.oj.modular.data.contest.param.DataContestProblemIdParam;
import io.charlie.web.oj.modular.data.contest.param.DataContestProblemPageParam;
import io.charlie.web.oj.modular.data.contest.mapper.DataContestProblemMapper;
import io.charlie.web.oj.modular.data.contest.service.DataContestProblemService;
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
* @description 竞赛题目表 服务实现类
*/
@Slf4j
@Service
@RequiredArgsConstructor
public class DataContestProblemServiceImpl extends ServiceImpl<DataContestProblemMapper, DataContestProblem> implements DataContestProblemService {

    @Override
    public Page<DataContestProblem> page(DataContestProblemPageParam dataContestProblemPageParam) {
        QueryWrapper<DataContestProblem> queryWrapper = new QueryWrapper<DataContestProblem>().checkSqlInjection();
        // 关键字
//        if (ObjectUtil.isNotEmpty(dataContestProblemPageParam.getKeyword())) {
//            queryWrapper.lambda().like(DataContestProblem::getTitle, dataContestProblemPageParam.getKeyword());
//        }
        if (ObjectUtil.isAllNotEmpty(dataContestProblemPageParam.getSortField(), dataContestProblemPageParam.getSortOrder()) && ISortOrderEnum.isValid(dataContestProblemPageParam.getSortOrder())) {
            queryWrapper.orderBy(
                    true,
                    dataContestProblemPageParam.getSortOrder().equals(ISortOrderEnum.ASCEND.getValue()),
                    StrUtil.toUnderlineCase(dataContestProblemPageParam.getSortField()));
        } else {
            queryWrapper.lambda().orderByAsc(DataContestProblem::getSort);
        }

        return this.page(CommonPageRequest.Page(
                        Optional.ofNullable(dataContestProblemPageParam.getCurrent()).orElse(1),
                        Optional.ofNullable(dataContestProblemPageParam.getSize()).orElse(20),
                null
                ),
                queryWrapper);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void add(DataContestProblemAddParam dataContestProblemAddParam) {
        DataContestProblem bean = BeanUtil.toBean(dataContestProblemAddParam, DataContestProblem.class);
        this.save(bean);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void edit(DataContestProblemEditParam dataContestProblemEditParam) {
        if (!this.exists(new LambdaQueryWrapper<DataContestProblem>().eq(DataContestProblem::getId, dataContestProblemEditParam.getId()))) {
            throw new BusinessException(ResultCode.PARAM_ERROR);
        }
        DataContestProblem bean = BeanUtil.toBean(dataContestProblemEditParam, DataContestProblem.class);
        BeanUtil.copyProperties(dataContestProblemEditParam, bean);
        this.updateById(bean);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void delete(List<DataContestProblemIdParam> dataContestProblemIdParamList) {
        if (ObjectUtil.isEmpty(dataContestProblemIdParamList)) {
            throw new BusinessException(ResultCode.PARAM_ERROR);
        }
        this.removeByIds(CollStreamUtil.toList(dataContestProblemIdParamList, DataContestProblemIdParam::getId));
    }

    @Override
    public DataContestProblem detail(DataContestProblemIdParam dataContestProblemIdParam) {
        DataContestProblem dataContestProblem = this.getById(dataContestProblemIdParam.getId());
        if (ObjectUtil.isEmpty(dataContestProblem)) {
            throw new BusinessException(ResultCode.PARAM_ERROR);
        }
        return dataContestProblem;
    }

}