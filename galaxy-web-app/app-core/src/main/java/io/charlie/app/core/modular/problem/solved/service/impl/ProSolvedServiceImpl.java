package io.charlie.app.core.modular.problem.solved.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollStreamUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.charlie.app.core.modular.problem.solved.entity.ProSolved;
import io.charlie.app.core.modular.problem.solved.param.ProSolvedAddParam;
import io.charlie.app.core.modular.problem.solved.param.ProSolvedEditParam;
import io.charlie.app.core.modular.problem.solved.param.ProSolvedIdParam;
import io.charlie.app.core.modular.problem.solved.param.ProSolvedPageParam;
import io.charlie.app.core.modular.problem.solved.mapper.ProSolvedMapper;
import io.charlie.app.core.modular.problem.solved.service.ProSolvedService;
import io.charlie.app.core.modular.problem.submit.entity.ProSubmit;
import io.charlie.galaxy.enums.ISortOrderEnum;
import io.charlie.galaxy.exception.BusinessException;
import io.charlie.galaxy.pojo.CommonPageRequest;
import io.charlie.galaxy.result.ResultCode;
import org.springframework.stereotype.Service;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
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
public class ProSolvedServiceImpl extends ServiceImpl<ProSolvedMapper, ProSolved> implements ProSolvedService {

    @Override
    public Page<ProSolved> page(ProSolvedPageParam proSolvedPageParam) {
        QueryWrapper<ProSolved> queryWrapper = new QueryWrapper<ProSolved>().checkSqlInjection();
        if (ObjectUtil.isAllNotEmpty(proSolvedPageParam.getSortField(), proSolvedPageParam.getSortOrder()) && ISortOrderEnum.isValid(proSolvedPageParam.getSortOrder())) {
            queryWrapper.orderBy(
                    true,
                    proSolvedPageParam.getSortOrder().equals(ISortOrderEnum.ASCEND.getValue()),
                    StrUtil.toUnderlineCase(proSolvedPageParam.getSortField()));
        }

        if (ObjectUtil.isNotEmpty(proSolvedPageParam.getProblemId())) {
            queryWrapper.lambda().eq(ProSolved::getProblemId, proSolvedPageParam.getProblemId());
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
        ProSolved proSolved = this.getById(proSolvedIdParam.getId());
        if (ObjectUtil.isEmpty(proSolved)) {
            throw new BusinessException(ResultCode.PARAM_ERROR);
        }
        return proSolved;
    }

    @Override
    public String getUserSolvedProblem() {
        long count;
        try {
            String loginIdAsString = StpUtil.getLoginIdAsString();
            count = this.count(new LambdaQueryWrapper<ProSolved>().eq(ProSolved::getUserId, loginIdAsString).eq(ProSolved::getSolved, true));
        } catch (Exception ignored) {
            count = 0;
        }
        return String.valueOf(count);
    }

    @Override
    public BigDecimal getAveragePassRate() {
        // 总成功次数
        long totalSuccessCount = this.count(new LambdaQueryWrapper<ProSolved>().eq(ProSolved::getSolved, true));
        if (totalSuccessCount == 0) {
            return BigDecimal.ZERO;
        }
        // 总尝试次数
        long totalTryCount = this.count();
        if (totalTryCount == 0) {
            return BigDecimal.ZERO;
        }
        // 平均通过率
        return new BigDecimal(totalSuccessCount)
                .multiply(new BigDecimal(100))
                .divide(new BigDecimal(totalTryCount), 2, RoundingMode.DOWN);
    }

}