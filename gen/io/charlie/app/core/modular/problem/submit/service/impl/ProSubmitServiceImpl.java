package io.charlie.problem.service.base.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollStreamUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.charlie.app.core.modular.problem.submit.entity.ProSubmit;
import io.charlie.app.core.modular.problem.submit.param.ProSubmitAddParam;
import io.charlie.app.core.modular.problem.submit.param.ProSubmitEditParam;
import io.charlie.app.core.modular.problem.submit.param.ProSubmitIdParam;
import io.charlie.app.core.modular.problem.submit.param.ProSubmitPageParam;
import io.charlie.app.core.modular.problem.submit.mapper.ProSubmitMapper;
import io.charlie.app.core.modular.problem.submit.service.ProSubmitService;
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
* @description 提交表 服务实现类
*/
@Slf4j
@Service
@RequiredArgsConstructor
public class ProSubmitServiceImpl extends ServiceImpl<ProSubmitMapper, ProSubmit> implements ProSubmitService {

    @Override
    public Page<ProSubmit> page(ProSubmitPageParam proSubmitPageParam) {
        QueryWrapper<ProSubmit> queryWrapper = new QueryWrapper<ProSubmit>().checkSqlInjection();
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
        ProSubmit proSubmit = this.getById(proSubmitIdParam.getId());
        if (ObjectUtil.isEmpty(proSubmit)) {
            throw new BusinessException(ResultCode.PARAM_ERROR);
        }
        return proSubmit;
    }

}