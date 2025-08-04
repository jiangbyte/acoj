package io.charlie.app.core.modular.set.submit.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollStreamUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.charlie.app.core.modular.set.submit.entity.ProSetSubmit;
import io.charlie.app.core.modular.set.submit.param.*;
import io.charlie.app.core.modular.set.submit.mapper.ProSetSubmitMapper;
import io.charlie.app.core.modular.set.submit.service.ProSetSubmitService;
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
 * @description 题单提交表 服务实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ProSetSubmitServiceImpl extends ServiceImpl<ProSetSubmitMapper, ProSetSubmit> implements ProSetSubmitService {

    @Override
    public Page<ProSetSubmit> page(ProSetSubmitPageParam proSetSubmitPageParam) {
        QueryWrapper<ProSetSubmit> queryWrapper = new QueryWrapper<ProSetSubmit>().checkSqlInjection();
        if (ObjectUtil.isAllNotEmpty(proSetSubmitPageParam.getSortField(), proSetSubmitPageParam.getSortOrder()) && ISortOrderEnum.isValid(proSetSubmitPageParam.getSortOrder())) {
            queryWrapper.orderBy(
                    true,
                    proSetSubmitPageParam.getSortOrder().equals(ISortOrderEnum.ASCEND.getValue()),
                    StrUtil.toUnderlineCase(proSetSubmitPageParam.getSortField()));
        }

        return this.page(CommonPageRequest.Page(
                        Optional.ofNullable(proSetSubmitPageParam.getCurrent()).orElse(1),
                        Optional.ofNullable(proSetSubmitPageParam.getSize()).orElse(20),
                        null
                ),
                queryWrapper);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void add(ProSetSubmitAddParam proSetSubmitAddParam) {
        ProSetSubmit bean = BeanUtil.toBean(proSetSubmitAddParam, ProSetSubmit.class);
        this.save(bean);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void edit(ProSetSubmitEditParam proSetSubmitEditParam) {
        if (!this.exists(new LambdaQueryWrapper<ProSetSubmit>().eq(ProSetSubmit::getId, proSetSubmitEditParam.getId()))) {
            throw new BusinessException(ResultCode.PARAM_ERROR);
        }
        ProSetSubmit bean = BeanUtil.toBean(proSetSubmitEditParam, ProSetSubmit.class);
        BeanUtil.copyProperties(proSetSubmitEditParam, bean);
        this.updateById(bean);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void delete(List<ProSetSubmitIdParam> proSetSubmitIdParamList) {
        if (ObjectUtil.isEmpty(proSetSubmitIdParamList)) {
            throw new BusinessException(ResultCode.PARAM_ERROR);
        }
        this.removeByIds(CollStreamUtil.toList(proSetSubmitIdParamList, ProSetSubmitIdParam::getId));
    }

    @Override
    public ProSetSubmit detail(ProSetSubmitIdParam proSetSubmitIdParam) {
        ProSetSubmit proSetSubmit = this.getById(proSetSubmitIdParam.getId());
        if (ObjectUtil.isEmpty(proSetSubmit)) {
            throw new BusinessException(ResultCode.PARAM_ERROR);
        }
        return proSetSubmit;
    }

    @Override
    public String execute(ProSetSubmitExecuteParam proSetSubmitExecuteParam) {
        ProSetSubmit proSetSubmit = BeanUtil.toBean(proSetSubmitExecuteParam, ProSetSubmit.class);
        BeanUtil.copyProperties(proSetSubmitExecuteParam, proSetSubmit);
        this.save(proSetSubmit);
        String loginIdAsString = StpUtil.getLoginIdAsString();
        proSetSubmit.setUserId(loginIdAsString);
        return proSetSubmit.getId();
    }

}