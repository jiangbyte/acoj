package io.charlie.problem.service.base.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollStreamUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.charlie.app.core.modular.set.similarity.task.entity.ProSetSimilarityTask;
import io.charlie.app.core.modular.set.similarity.task.param.ProSetSimilarityTaskAddParam;
import io.charlie.app.core.modular.set.similarity.task.param.ProSetSimilarityTaskEditParam;
import io.charlie.app.core.modular.set.similarity.task.param.ProSetSimilarityTaskIdParam;
import io.charlie.app.core.modular.set.similarity.task.param.ProSetSimilarityTaskPageParam;
import io.charlie.app.core.modular.set.similarity.task.mapper.ProSetSimilarityTaskMapper;
import io.charlie.app.core.modular.set.similarity.task.service.ProSetSimilarityTaskService;
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
* @description 题单代码相似度检测任务表 服务实现类
*/
@Slf4j
@Service
@RequiredArgsConstructor
public class ProSetSimilarityTaskServiceImpl extends ServiceImpl<ProSetSimilarityTaskMapper, ProSetSimilarityTask> implements ProSetSimilarityTaskService {

    @Override
    public Page<ProSetSimilarityTask> page(ProSetSimilarityTaskPageParam proSetSimilarityTaskPageParam) {
        QueryWrapper<ProSetSimilarityTask> queryWrapper = new QueryWrapper<ProSetSimilarityTask>().checkSqlInjection();
        if (ObjectUtil.isAllNotEmpty(proSetSimilarityTaskPageParam.getSortField(), proSetSimilarityTaskPageParam.getSortOrder()) && ISortOrderEnum.isValid(proSetSimilarityTaskPageParam.getSortOrder())) {
            queryWrapper.orderBy(
                    true,
                    proSetSimilarityTaskPageParam.getSortOrder().equals(ISortOrderEnum.ASCEND.getValue()),
                    StrUtil.toUnderlineCase(proSetSimilarityTaskPageParam.getSortField()));
        }

        return this.page(CommonPageRequest.Page(
                        Optional.ofNullable(proSetSimilarityTaskPageParam.getCurrent()).orElse(1),
                        Optional.ofNullable(proSetSimilarityTaskPageParam.getSize()).orElse(20),
                null
                ),
                queryWrapper);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void add(ProSetSimilarityTaskAddParam proSetSimilarityTaskAddParam) {
        ProSetSimilarityTask bean = BeanUtil.toBean(proSetSimilarityTaskAddParam, ProSetSimilarityTask.class);
        this.save(bean);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void edit(ProSetSimilarityTaskEditParam proSetSimilarityTaskEditParam) {
        if (!this.exists(new LambdaQueryWrapper<ProSetSimilarityTask>().eq(ProSetSimilarityTask::getId, proSetSimilarityTaskEditParam.getId()))) {
            throw new BusinessException(ResultCode.PARAM_ERROR);
        }
        ProSetSimilarityTask bean = BeanUtil.toBean(proSetSimilarityTaskEditParam, ProSetSimilarityTask.class);
        BeanUtil.copyProperties(proSetSimilarityTaskEditParam, bean);
        this.updateById(bean);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void delete(List<ProSetSimilarityTaskIdParam> proSetSimilarityTaskIdParamList) {
        if (ObjectUtil.isEmpty(proSetSimilarityTaskIdParamList)) {
            throw new BusinessException(ResultCode.PARAM_ERROR);
        }
        this.removeByIds(CollStreamUtil.toList(proSetSimilarityTaskIdParamList, ProSetSimilarityTaskIdParam::getId));
    }

    @Override
    public ProSetSimilarityTask detail(ProSetSimilarityTaskIdParam proSetSimilarityTaskIdParam) {
        ProSetSimilarityTask proSetSimilarityTask = this.getById(proSetSimilarityTaskIdParam.getId());
        if (ObjectUtil.isEmpty(proSetSimilarityTask)) {
            throw new BusinessException(ResultCode.PARAM_ERROR);
        }
        return proSetSimilarityTask;
    }

}