package io.charlie.app.core.modular.problem.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollStreamUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import io.charlie.app.core.modular.problem.entity.solved.ProSolved;
import io.charlie.app.core.modular.problem.mapper.ProSolvedMapper;
import io.charlie.app.core.modular.problem.param.solved.ProSolvedAddParam;
import io.charlie.app.core.modular.problem.param.solved.ProSolvedEditParam;
import io.charlie.app.core.modular.problem.param.solved.ProSolvedIdParam;
import io.charlie.app.core.modular.problem.param.solved.ProSolvedPageParam;
import io.charlie.app.core.modular.problem.service.ProSolvedService;
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
* @description 用户解决表 服务实现类
*/
@Slf4j
@Service
@RequiredArgsConstructor
public class ProSolvedServiceImpl extends ServiceImpl<ProSolvedMapper, ProSolved> implements ProSolvedService {

    @Override
    public Page<ProSolved> page(ProSolvedPageParam proSolvedPageParam) {
        QueryWrapper<ProSolved> queryWrapper = new QueryWrapper<ProSolved>().checkSqlInjection();
        // 关键字
//        if (ObjectUtil.isNotEmpty(proSolvedPageParam.getKeyword())) {
//            queryWrapper.lambda().like(ProSolved::getTitle, proSolvedPageParam.getKeyword());
//        }
        if (ObjectUtil.isAllNotEmpty(proSolvedPageParam.getSortField(), proSolvedPageParam.getSortOrder()) && ISortOrderEnum.isValid(proSolvedPageParam.getSortOrder())) {
            queryWrapper.orderBy(
                    true,
                    proSolvedPageParam.getSortOrder().equals(ISortOrderEnum.ASCEND.getValue()),
                    StrUtil.toUnderlineCase(proSolvedPageParam.getSortField()));
        }

        return this.page(CommonPageRequest.Page(
                        Optional.ofNullable(proSolvedPageParam.getCurrent()).orElse(1),
                        Optional.ofNullable(proSolvedPageParam.getSize()).orElse(20),
                null
                ),
                queryWrapper);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void add(ProSolvedAddParam proSolvedAddParam) {
        ProSolved bean = BeanUtil.toBean(proSolvedAddParam, ProSolved.class);
        this.save(bean);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void edit(ProSolvedEditParam proSolvedEditParam) {
        if (!this.exists(new LambdaQueryWrapper<ProSolved>().eq(ProSolved::getId, proSolvedEditParam.getId()))) {
            throw new BusinessException(ResultCode.PARAM_ERROR);
        }
        ProSolved bean = BeanUtil.toBean(proSolvedEditParam, ProSolved.class);
        BeanUtil.copyProperties(proSolvedEditParam, bean);
        this.updateById(bean);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void delete(List<ProSolvedIdParam> proSolvedIdParamList) {
        if (ObjectUtil.isEmpty(proSolvedIdParamList)) {
            throw new BusinessException(ResultCode.PARAM_ERROR);
        }
        this.removeByIds(CollStreamUtil.toList(proSolvedIdParamList, ProSolvedIdParam::getId));
    }

    @Override
    public ProSolved detail(ProSolvedIdParam proSolvedIdParam) {
        return this.queryEntity(proSolvedIdParam.getId());
    }

    @Override
    public ProSolved queryEntity(String id) {
        ProSolved proSolved = this.getById(id);
        if (ObjectUtil.isEmpty(proSolved)) {
            throw new BusinessException(ResultCode.PARAM_ERROR);
        }
        return proSolved;
    }
}