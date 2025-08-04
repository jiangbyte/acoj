package io.charlie.app.core.modular.problem.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollStreamUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.charlie.app.core.modular.problem.entity.ProProblem;
import io.charlie.app.core.modular.problem.param.ProProblemAddParam;
import io.charlie.app.core.modular.problem.param.ProProblemEditParam;
import io.charlie.app.core.modular.problem.param.ProProblemIdParam;
import io.charlie.app.core.modular.problem.param.ProProblemPageParam;
import io.charlie.app.core.modular.problem.mapper.ProProblemMapper;
import io.charlie.app.core.modular.problem.service.ProProblemService;
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
* @description 题目表 服务实现类
*/
@Slf4j
@Service
@RequiredArgsConstructor
public class ProProblemServiceImpl extends ServiceImpl<ProProblemMapper, ProProblem> implements ProProblemService {

    @Override
    public Page<ProProblem> page(ProProblemPageParam proProblemPageParam) {
        QueryWrapper<ProProblem> queryWrapper = new QueryWrapper<ProProblem>().checkSqlInjection();
        // 关键字
        if (ObjectUtil.isNotEmpty(proProblemPageParam.getKeyword())) {
            queryWrapper.lambda().like(ProProblem::getTitle, proProblemPageParam.getKeyword());
        }
        if (ObjectUtil.isAllNotEmpty(proProblemPageParam.getSortField(), proProblemPageParam.getSortOrder()) && ISortOrderEnum.isValid(proProblemPageParam.getSortOrder())) {
            queryWrapper.orderBy(
                    true,
                    proProblemPageParam.getSortOrder().equals(ISortOrderEnum.ASCEND.getValue()),
                    StrUtil.toUnderlineCase(proProblemPageParam.getSortField()));
        }

        return this.page(CommonPageRequest.Page(
                        Optional.ofNullable(proProblemPageParam.getCurrent()).orElse(1),
                        Optional.ofNullable(proProblemPageParam.getSize()).orElse(20),
                null
                ),
                queryWrapper);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void add(ProProblemAddParam proProblemAddParam) {
        ProProblem bean = BeanUtil.toBean(proProblemAddParam, ProProblem.class);
        this.save(bean);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void edit(ProProblemEditParam proProblemEditParam) {
        if (!this.exists(new LambdaQueryWrapper<ProProblem>().eq(ProProblem::getId, proProblemEditParam.getId()))) {
            throw new BusinessException(ResultCode.PARAM_ERROR);
        }
        ProProblem bean = BeanUtil.toBean(proProblemEditParam, ProProblem.class);
        BeanUtil.copyProperties(proProblemEditParam, bean);
        this.updateById(bean);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void delete(List<ProProblemIdParam> proProblemIdParamList) {
        if (ObjectUtil.isEmpty(proProblemIdParamList)) {
            throw new BusinessException(ResultCode.PARAM_ERROR);
        }
        this.removeByIds(CollStreamUtil.toList(proProblemIdParamList, ProProblemIdParam::getId));
    }

    @Override
    public ProProblem detail(ProProblemIdParam proProblemIdParam) {
        return this.queryEntity(proProblemIdParam.getId());
    }

    @Override
    public ProProblem queryEntity(String id) {
        ProProblem proProblem = this.getById(id);
        if (ObjectUtil.isEmpty(proProblem)) {
            throw new BusinessException(ResultCode.PARAM_ERROR);
        }
        return proProblem;
    }
}