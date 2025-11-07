package io.charlie.web.oj.modular.data.similarity.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollStreamUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.dynamic.datasource.annotation.DS;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.charlie.web.oj.modular.data.similarity.entity.TaskSimilarity;
import io.charlie.web.oj.modular.data.similarity.param.*;
import io.charlie.web.oj.modular.data.similarity.mapper.TaskSimilarityMapper;
import io.charlie.web.oj.modular.data.similarity.service.TaskSimilarityService;
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
 * @description 检测结果任务库 服务实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TaskSimilarityServiceImpl extends ServiceImpl<TaskSimilarityMapper, TaskSimilarity> implements TaskSimilarityService {

    @Override
    @DS("slave")
    public Page<TaskSimilarity> page(TaskSimilarityPageParam taskSimilarityPageParam) {
        QueryWrapper<TaskSimilarity> queryWrapper = new QueryWrapper<TaskSimilarity>().checkSqlInjection();
        if (ObjectUtil.isNotEmpty(taskSimilarityPageParam.getTaskId())) {
            queryWrapper.lambda().eq(TaskSimilarity::getTaskId, taskSimilarityPageParam.getTaskId());
        }

        if (ObjectUtil.isAllNotEmpty(taskSimilarityPageParam.getSortField(), taskSimilarityPageParam.getSortOrder()) && ISortOrderEnum.isValid(taskSimilarityPageParam.getSortOrder())) {
            queryWrapper.orderBy(
                    true,
                    taskSimilarityPageParam.getSortOrder().equals(ISortOrderEnum.ASCEND.getValue()),
                    StrUtil.toUnderlineCase(taskSimilarityPageParam.getSortField()));
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