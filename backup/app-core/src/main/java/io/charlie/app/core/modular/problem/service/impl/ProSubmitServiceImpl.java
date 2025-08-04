package io.charlie.app.core.modular.problem.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollStreamUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import io.charlie.app.core.modular.problem.entity.ProProblem;
import io.charlie.app.core.modular.problem.entity.submit.ProSubmit;
import io.charlie.app.core.modular.problem.mapper.ProProblemMapper;
import io.charlie.app.core.modular.problem.mapper.ProSubmitMapper;
import io.charlie.app.core.modular.problem.param.submit.*;
import io.charlie.app.core.modular.problem.service.ProProblemService;
import io.charlie.app.core.modular.problem.service.ProSubmitService;
import io.charlie.galaxy.enums.ISortOrderEnum;
import io.charlie.galaxy.exception.BusinessException;
import io.charlie.galaxy.pojo.CommonPageRequest;
import io.charlie.galaxy.result.ResultCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * @author Charlie Zhang
 * @version v1.0
 * @date 2025-06-23
 * @description 提交表 服务实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ProSubmitServiceImpl extends ServiceImpl<ProSubmitMapper, ProSubmit> implements ProSubmitService {
    private final ProProblemMapper proProblemMapper;

    @Override
    public Page<ProSubmit> page(ProSubmitPageParam proSubmitPageParam) {
        QueryWrapper<ProSubmit> queryWrapper = new QueryWrapper<ProSubmit>().checkSqlInjection();
        // 关键字
        if (ObjectUtil.isNotEmpty(proSubmitPageParam.getKeyword())) {
            queryWrapper.lambda().like(ProSubmit::getId, proSubmitPageParam.getKeyword());
        }
        if (ObjectUtil.isAllNotEmpty(proSubmitPageParam.getSortField(), proSubmitPageParam.getSortOrder()) && ISortOrderEnum.isValid(proSubmitPageParam.getSortOrder())) {
            queryWrapper.orderBy(
                    true,
                    proSubmitPageParam.getSortOrder().equals(ISortOrderEnum.ASCEND.getValue()),
                    StrUtil.toUnderlineCase(proSubmitPageParam.getSortField()));
        }

        return this.page(CommonPageRequest.Page(
                        Optional.ofNullable(proSubmitPageParam.getCurrent()).orElse(1),
                        Optional.ofNullable(proSubmitPageParam.getSize()).orElse(20),
                        null
                ),
                queryWrapper);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void add(ProSubmitAddParam proSubmitAddParam) {
        ProSubmit bean = BeanUtil.toBean(proSubmitAddParam, ProSubmit.class);
        this.save(bean);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void edit(ProSubmitEditParam proSubmitEditParam) {
        if (!this.exists(new LambdaQueryWrapper<ProSubmit>().eq(ProSubmit::getId, proSubmitEditParam.getId()))) {
            throw new BusinessException(ResultCode.PARAM_ERROR);
        }
        ProSubmit bean = BeanUtil.toBean(proSubmitEditParam, ProSubmit.class);
        BeanUtil.copyProperties(proSubmitEditParam, bean);
        this.updateById(bean);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void delete(List<ProSubmitIdParam> proSubmitIdParamList) {
        if (ObjectUtil.isEmpty(proSubmitIdParamList)) {
            throw new BusinessException(ResultCode.PARAM_ERROR);
        }
        this.removeByIds(CollStreamUtil.toList(proSubmitIdParamList, ProSubmitIdParam::getId));
    }

    @Override
    public ProSubmit detail(ProSubmitIdParam proSubmitIdParam) {
        return this.queryEntity(proSubmitIdParam.getId());
    }

    @Override
    public ProSubmit queryEntity(String id) {
        ProSubmit proSubmit = this.getById(id);
        if (ObjectUtil.isEmpty(proSubmit)) {
            throw new BusinessException(ResultCode.PARAM_ERROR);
        }
        return proSubmit;
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public String executeCode(ProSubmitExecParam proSubmitExecParam) {
        if (!proProblemMapper.exists(new LambdaQueryWrapper<ProProblem>().eq(ProProblem::getId, proSubmitExecParam.getProblemId()))) {
            throw new BusinessException(ResultCode.PARAM_ERROR);
        }
        ProSubmit bean = BeanUtil.toBean(proSubmitExecParam, ProSubmit.class);
        String loginIdAsString = StpUtil.getLoginIdAsString();
        bean.setUserId(loginIdAsString);
        this.save(bean);

        ProProblem proProblem = proProblemMapper.selectById(proSubmitExecParam.getProblemId());
        return bean.getId();
    }
}