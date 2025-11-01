package io.charlie.web.oj.modular.data.testcase.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollStreamUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.dynamic.datasource.annotation.DS;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.charlie.web.oj.modular.data.testcase.entity.DataTestCase;
import io.charlie.web.oj.modular.data.testcase.mapper.DataTestCaseMapper;
import io.charlie.web.oj.modular.data.testcase.param.DataTestCaseAddParam;
import io.charlie.web.oj.modular.data.testcase.param.DataTestCaseEditParam;
import io.charlie.web.oj.modular.data.testcase.param.DataTestCaseIdParam;
import io.charlie.web.oj.modular.data.testcase.param.DataTestCasePageParam;
import io.charlie.web.oj.modular.data.testcase.service.DataTestCaseService;
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
* @description 题目测试用例表 服务实现类
*/
@Slf4j
@Service
@RequiredArgsConstructor
public class DataTestCaseServiceImpl extends ServiceImpl<DataTestCaseMapper, DataTestCase> implements DataTestCaseService {

    @Override
    @DS("slave")
    public Page<DataTestCase> page(DataTestCasePageParam dataTestCasePageParam) {
        QueryWrapper<DataTestCase> queryWrapper = new QueryWrapper<DataTestCase>().checkSqlInjection();

        if (ObjectUtil.isNotEmpty(dataTestCasePageParam.getProblemId())) {
            queryWrapper.lambda().eq(DataTestCase::getProblemId, dataTestCasePageParam.getProblemId());
        } else {
            return new Page<>();
        }

        // 关键字
        if (ObjectUtil.isNotEmpty(dataTestCasePageParam.getKeyword())) {
            queryWrapper.lambda().like(DataTestCase::getCaseSign, dataTestCasePageParam.getKeyword());
        }

        if (ObjectUtil.isAllNotEmpty(dataTestCasePageParam.getSortField(), dataTestCasePageParam.getSortOrder()) && ISortOrderEnum.isValid(dataTestCasePageParam.getSortOrder())) {
            queryWrapper.orderBy(
                    true,
                    dataTestCasePageParam.getSortOrder().equals(ISortOrderEnum.ASCEND.getValue()),
                    StrUtil.toUnderlineCase(dataTestCasePageParam.getSortField()));
        }

        return this.page(CommonPageRequest.Page(
                        Optional.ofNullable(dataTestCasePageParam.getCurrent()).orElse(1),
                        Optional.ofNullable(dataTestCasePageParam.getSize()).orElse(20),
                null
                ),
                queryWrapper);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void add(DataTestCaseAddParam dataTestCaseAddParam) {
        DataTestCase bean = BeanUtil.toBean(dataTestCaseAddParam, DataTestCase.class);
        bean.setCaseSign(IdUtil.objectId());
        this.save(bean);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void edit(DataTestCaseEditParam dataTestCaseEditParam) {
        if (!this.exists(new LambdaQueryWrapper<DataTestCase>().eq(DataTestCase::getId, dataTestCaseEditParam.getId()))) {
            throw new BusinessException(ResultCode.PARAM_ERROR);
        }
        DataTestCase bean = BeanUtil.toBean(dataTestCaseEditParam, DataTestCase.class);
        BeanUtil.copyProperties(dataTestCaseEditParam, bean);
        this.updateById(bean);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void delete(List<DataTestCaseIdParam> dataTestCaseIdParamList) {
        if (ObjectUtil.isEmpty(dataTestCaseIdParamList)) {
            throw new BusinessException(ResultCode.PARAM_ERROR);
        }
        this.removeByIds(CollStreamUtil.toList(dataTestCaseIdParamList, DataTestCaseIdParam::getId));
    }

    @Override
    @DS("slave")
    public DataTestCase detail(DataTestCaseIdParam dataTestCaseIdParam) {
        DataTestCase dataTestCase = this.getById(dataTestCaseIdParam.getId());
        if (ObjectUtil.isEmpty(dataTestCase)) {
            throw new BusinessException(ResultCode.PARAM_ERROR);
        }
        return dataTestCase;
    }

    @Override
    @DS("slave")
    public List<DataTestCase> getTestCaseByProblemId(String problemId) {
        return this.list(new LambdaQueryWrapper<DataTestCase>().eq(DataTestCase::getProblemId, problemId));
    }

}