package io.charlie.app.core.modular.set.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollStreamUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.charlie.app.core.modular.set.entity.solved.ProSetSolved;
import io.charlie.app.core.modular.set.param.solved.ProSetSolvedAddParam;
import io.charlie.app.core.modular.set.param.solved.ProSetSolvedEditParam;
import io.charlie.app.core.modular.set.param.solved.ProSetSolvedIdParam;
import io.charlie.app.core.modular.set.param.solved.ProSetSolvedPageParam;
import io.charlie.app.core.modular.set.mapper.ProSetSolvedMapper;
import io.charlie.app.core.modular.set.service.ProSetSolvedService;
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
* @description 用户题集解决记录表 服务实现类
*/
@Slf4j
@Service
@RequiredArgsConstructor
public class ProSetSolvedServiceImpl extends ServiceImpl<ProSetSolvedMapper, ProSetSolved> implements ProSetSolvedService {

    @Override
    public Page<ProSetSolved> page(ProSetSolvedPageParam proSetSolvedPageParam) {
        QueryWrapper<ProSetSolved> queryWrapper = new QueryWrapper<ProSetSolved>().checkSqlInjection();
        // 关键字
//        if (ObjectUtil.isNotEmpty(proSetSolvedPageParam.getKeyword())) {
//            queryWrapper.lambda().like(ProSetSolved::getTitle, proSetSolvedPageParam.getKeyword());
//        }
        if (ObjectUtil.isAllNotEmpty(proSetSolvedPageParam.getSortField(), proSetSolvedPageParam.getSortOrder()) && ISortOrderEnum.isValid(proSetSolvedPageParam.getSortOrder())) {
            queryWrapper.orderBy(
                    true,
                    proSetSolvedPageParam.getSortOrder().equals(ISortOrderEnum.ASCEND.getValue()),
                    StrUtil.toUnderlineCase(proSetSolvedPageParam.getSortField()));
        }

        return this.page(CommonPageRequest.Page(
                        Optional.ofNullable(proSetSolvedPageParam.getCurrent()).orElse(1),
                        Optional.ofNullable(proSetSolvedPageParam.getSize()).orElse(20),
                null
                ),
                queryWrapper);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void add(ProSetSolvedAddParam proSetSolvedAddParam) {
        ProSetSolved bean = BeanUtil.toBean(proSetSolvedAddParam, ProSetSolved.class);
        this.save(bean);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void edit(ProSetSolvedEditParam proSetSolvedEditParam) {
        if (!this.exists(new LambdaQueryWrapper<ProSetSolved>().eq(ProSetSolved::getId, proSetSolvedEditParam.getId()))) {
            throw new BusinessException(ResultCode.PARAM_ERROR);
        }
        ProSetSolved bean = BeanUtil.toBean(proSetSolvedEditParam, ProSetSolved.class);
        BeanUtil.copyProperties(proSetSolvedEditParam, bean);
        this.updateById(bean);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void delete(List<ProSetSolvedIdParam> proSetSolvedIdParamList) {
        if (ObjectUtil.isEmpty(proSetSolvedIdParamList)) {
            throw new BusinessException(ResultCode.PARAM_ERROR);
        }
        this.removeByIds(CollStreamUtil.toList(proSetSolvedIdParamList, ProSetSolvedIdParam::getId));
    }

    @Override
    public ProSetSolved detail(ProSetSolvedIdParam proSetSolvedIdParam) {
        return this.queryEntity(proSetSolvedIdParam.getId());
    }

    @Override
    public ProSetSolved queryEntity(String id) {
        ProSetSolved proSetSolved = this.getById(id);
        if (ObjectUtil.isEmpty(proSetSolved)) {
            throw new BusinessException(ResultCode.PARAM_ERROR);
        }
        return proSetSolved;
    }
}