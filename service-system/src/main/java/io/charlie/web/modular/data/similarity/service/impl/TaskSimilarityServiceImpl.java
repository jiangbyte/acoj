package io.charlie.web.modular.data.similarity.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollStreamUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.dynamic.datasource.annotation.DS;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.charlie.web.modular.data.similarity.entity.TaskSimilarity;
import io.charlie.web.modular.data.similarity.param.TaskSimilarityAddParam;
import io.charlie.web.modular.data.similarity.param.TaskSimilarityEditParam;
import io.charlie.web.modular.data.similarity.param.TaskSimilarityIdParam;
import io.charlie.web.modular.data.similarity.param.TaskSimilarityPageParam;
import io.charlie.web.modular.data.similarity.param.*;
import io.charlie.web.modular.data.similarity.mapper.TaskSimilarityMapper;
import io.charlie.web.modular.data.similarity.service.TaskSimilarityService;
import io.charlie.cores.enums.ISortOrderEnum;
import io.charlie.cores.exception.BusinessException;
import io.charlie.cores.pojo.CommonPageRequest;
import io.charlie.cores.result.ResultCode;
import org.springframework.stereotype.Service;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.*;

/**
 * @author Charlie Zhang
 * @version v1.0
 * @date 2025-06-23
 * @description 检测结果任务库 服务实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TaskSimilarityServiceImpl extends ServiceImpl<TaskSimilarityMapper, TaskSimilarity> implements TaskSimilarityService {

    @Override
    @DS("slave")
    public Page<TaskSimilarity> page(TaskSimilarityPageParam taskSimilarityPageParam) {
        // 1. 按 task/problem/submit/user 过滤，支撑跨工作流追溯
        // 2. 相似度闭区间过滤；只填一端时另一端不限制，下限大于上限直接拒绝
        // 3. 先按排序字段排完全部命中行，再分页，避免只重排当前页
        QueryWrapper<TaskSimilarity> queryWrapper = new QueryWrapper<TaskSimilarity>().checkSqlInjection();
        if (ObjectUtil.isNotEmpty(taskSimilarityPageParam.getTaskId())) {
            queryWrapper.lambda().eq(TaskSimilarity::getTaskId, taskSimilarityPageParam.getTaskId());
        }
        if (StrUtil.isNotBlank(taskSimilarityPageParam.getProblemId())) {
            queryWrapper.lambda().eq(TaskSimilarity::getProblemId, taskSimilarityPageParam.getProblemId());
        }
        if (StrUtil.isNotBlank(taskSimilarityPageParam.getSubmitId())) {
            queryWrapper.lambda().and(w -> w.eq(TaskSimilarity::getSubmitId, taskSimilarityPageParam.getSubmitId())
                    .or()
                    .eq(TaskSimilarity::getOriginId, taskSimilarityPageParam.getSubmitId()));
        }
        if (StrUtil.isNotBlank(taskSimilarityPageParam.getUserId())) {
            queryWrapper.lambda().and(w -> w.eq(TaskSimilarity::getSubmitUser, taskSimilarityPageParam.getUserId())
                    .or()
                    .eq(TaskSimilarity::getOriginUser, taskSimilarityPageParam.getUserId()));
        }
        if (StrUtil.isNotBlank(taskSimilarityPageParam.getSubmitUser())) {
            queryWrapper.lambda().eq(TaskSimilarity::getSubmitUser, taskSimilarityPageParam.getSubmitUser());
        }
        if (StrUtil.isNotBlank(taskSimilarityPageParam.getOriginUser())) {
            queryWrapper.lambda().eq(TaskSimilarity::getOriginUser, taskSimilarityPageParam.getOriginUser());
        }
        BigDecimal minSimilarity = taskSimilarityPageParam.getMinSimilarity();
        BigDecimal maxSimilarity = taskSimilarityPageParam.getMaxSimilarity();
        if (minSimilarity != null && maxSimilarity != null && minSimilarity.compareTo(maxSimilarity) > 0) {
            throw new BusinessException("minSimilarity cannot be greater than maxSimilarity");
        }
        if (minSimilarity != null) {
            queryWrapper.lambda().ge(TaskSimilarity::getSimilarity, minSimilarity);
        }
        if (maxSimilarity != null) {
            queryWrapper.lambda().le(TaskSimilarity::getSimilarity, maxSimilarity);
        }

        if (ObjectUtil.isAllNotEmpty(taskSimilarityPageParam.getSortField(), taskSimilarityPageParam.getSortOrder()) && ISortOrderEnum.isValid(taskSimilarityPageParam.getSortOrder())) {
            queryWrapper.orderBy(
                    true,
                    taskSimilarityPageParam.getSortOrder().equals(ISortOrderEnum.ASCEND.getValue()),
                    StrUtil.toUnderlineCase(taskSimilarityPageParam.getSortField()));
        } else {
            queryWrapper.lambda().orderByDesc(TaskSimilarity::getSimilarity);
        }

        return this.page(CommonPageRequest.Page(
                        Optional.ofNullable(taskSimilarityPageParam.getCurrent()).orElse(1),
                        Optional.ofNullable(taskSimilarityPageParam.getSize()).orElse(20),
                        null
                ),
                queryWrapper);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void add(TaskSimilarityAddParam taskSimilarityAddParam) {
        TaskSimilarity bean = BeanUtil.toBean(taskSimilarityAddParam, TaskSimilarity.class);
        this.save(bean);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void edit(TaskSimilarityEditParam taskSimilarityEditParam) {
        if (!this.exists(new LambdaQueryWrapper<TaskSimilarity>().eq(TaskSimilarity::getId, taskSimilarityEditParam.getId()))) {
            throw new BusinessException(ResultCode.PARAM_ERROR);
        }
        TaskSimilarity bean = BeanUtil.toBean(taskSimilarityEditParam, TaskSimilarity.class);
        BeanUtil.copyProperties(taskSimilarityEditParam, bean);
        this.updateById(bean);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void delete(List<TaskSimilarityIdParam> taskSimilarityIdParamList) {
        if (ObjectUtil.isEmpty(taskSimilarityIdParamList)) {
            throw new BusinessException(ResultCode.PARAM_ERROR);
        }
        this.removeByIds(CollStreamUtil.toList(taskSimilarityIdParamList, TaskSimilarityIdParam::getId));
    }

    @Override
    @DS("slave")
    public TaskSimilarity detail(TaskSimilarityIdParam taskSimilarityIdParam) {
        TaskSimilarity taskSimilarity = this.getById(taskSimilarityIdParam.getId());
        if (ObjectUtil.isEmpty(taskSimilarity)) {
            throw new BusinessException(ResultCode.PARAM_ERROR);
        }
        return taskSimilarity;
    }

}