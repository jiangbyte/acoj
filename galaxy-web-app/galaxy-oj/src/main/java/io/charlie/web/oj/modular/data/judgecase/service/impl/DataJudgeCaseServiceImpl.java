package io.charlie.web.oj.modular.data.judgecase.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollStreamUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.charlie.web.oj.modular.data.judgecase.entity.DataJudgeCase;
import io.charlie.web.oj.modular.data.judgecase.param.DataJudgeCaseAddParam;
import io.charlie.web.oj.modular.data.judgecase.param.DataJudgeCaseEditParam;
import io.charlie.web.oj.modular.data.judgecase.param.DataJudgeCaseIdParam;
import io.charlie.web.oj.modular.data.judgecase.param.DataJudgeCasePageParam;
import io.charlie.web.oj.modular.data.judgecase.mapper.DataJudgeCaseMapper;
import io.charlie.web.oj.modular.data.judgecase.service.DataJudgeCaseService;
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
* @description 判题结果用例表 服务实现类
*/
@Slf4j
@Service
@RequiredArgsConstructor
public class DataJudgeCaseServiceImpl extends ServiceImpl<DataJudgeCaseMapper, DataJudgeCase> implements DataJudgeCaseService {

    @Override
    public Page<DataJudgeCase> page(DataJudgeCasePageParam dataJudgeCasePageParam) {
        QueryWrapper<DataJudgeCase> queryWrapper = new QueryWrapper<DataJudgeCase>().checkSqlInjection();

        if (ObjectUtil.isNotEmpty(dataJudgeCasePageParam.getSubmitId())) {
            queryWrapper.lambda().eq(DataJudgeCase::getSubmitId, dataJudgeCasePageParam.getSubmitId());
        } else {
            return new Page<>();
        }

        if (ObjectUtil.isAllNotEmpty(dataJudgeCasePageParam.getSortField(), dataJudgeCasePageParam.getSortOrder()) && ISortOrderEnum.isValid(dataJudgeCasePageParam.getSortOrder())) {
            queryWrapper.orderBy(
                    true,
                    dataJudgeCasePageParam.getSortOrder().equals(ISortOrderEnum.ASCEND.getValue()),
                    StrUtil.toUnderlineCase(dataJudgeCasePageParam.getSortField()));
        }

        return this.page(CommonPageRequest.Page(
                        Optional.ofNullable(dataJudgeCasePageParam.getCurrent()).orElse(1),
                        Optional.ofNullable(dataJudgeCasePageParam.getSize()).orElse(20),
                null
                ),
                queryWrapper);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void add(DataJudgeCaseAddParam dataJudgeCaseAddParam) {
        DataJudgeCase bean = BeanUtil.toBean(dataJudgeCaseAddParam, DataJudgeCase.class);
        this.save(bean);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void edit(DataJudgeCaseEditParam dataJudgeCaseEditParam) {
        if (!this.exists(new LambdaQueryWrapper<DataJudgeCase>().eq(DataJudgeCase::getId, dataJudgeCaseEditParam.getId()))) {
            throw new BusinessException(ResultCode.PARAM_ERROR);
        }
        DataJudgeCase bean = BeanUtil.toBean(dataJudgeCaseEditParam, DataJudgeCase.class);
        BeanUtil.copyProperties(dataJudgeCaseEditParam, bean);
        this.updateById(bean);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void delete(List<DataJudgeCaseIdParam> dataJudgeCaseIdParamList) {
        if (ObjectUtil.isEmpty(dataJudgeCaseIdParamList)) {
            throw new BusinessException(ResultCode.PARAM_ERROR);
        }
        this.removeByIds(CollStreamUtil.toList(dataJudgeCaseIdParamList, DataJudgeCaseIdParam::getId));
    }

    @Override
    public DataJudgeCase detail(DataJudgeCaseIdParam dataJudgeCaseIdParam) {
        DataJudgeCase dataJudgeCase = this.getById(dataJudgeCaseIdParam.getId());
        if (ObjectUtil.isEmpty(dataJudgeCase)) {
            throw new BusinessException(ResultCode.PARAM_ERROR);
        }
        return dataJudgeCase;
    }

}