package io.charlie.app.core.modular.problem.similarity.task.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollStreamUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.charlie.app.core.modular.problem.similarity.task.entity.ProSimilarityTask;
import io.charlie.app.core.modular.problem.similarity.task.param.ProSimilarityTaskAddParam;
import io.charlie.app.core.modular.problem.similarity.task.param.ProSimilarityTaskEditParam;
import io.charlie.app.core.modular.problem.similarity.task.param.ProSimilarityTaskIdParam;
import io.charlie.app.core.modular.problem.similarity.task.param.ProSimilarityTaskPageParam;
import io.charlie.app.core.modular.problem.similarity.task.mapper.ProSimilarityTaskMapper;
import io.charlie.app.core.modular.problem.similarity.task.service.ProSimilarityTaskService;
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
* @description 代码相似度检测任务表 服务实现类
*/
@Slf4j
@Service
@RequiredArgsConstructor
public class ProSimilarityTaskServiceImpl extends ServiceImpl<ProSimilarityTaskMapper, ProSimilarityTask> implements ProSimilarityTaskService {

    @Override
    public Page<ProSimilarityTask> page(ProSimilarityTaskPageParam proSimilarityTaskPageParam) {
        QueryWrapper<ProSimilarityTask> queryWrapper = new QueryWrapper<ProSimilarityTask>().checkSqlInjection();
        if (ObjectUtil.isAllNotEmpty(proSimilarityTaskPageParam.getSortField(), proSimilarityTaskPageParam.getSortOrder()) && ISortOrderEnum.isValid(proSimilarityTaskPageParam.getSortOrder())) {
            queryWrapper.orderBy(
                    true,
                    proSimilarityTaskPageParam.getSortOrder().equals(ISortOrderEnum.ASCEND.getValue()),
                    StrUtil.toUnderlineCase(proSimilarityTaskPageParam.getSortField()));
        }

        return this.page(CommonPageRequest.Page(
                        Optional.ofNullable(proSimilarityTaskPageParam.getCurrent()).orElse(1),
                        Optional.ofNullable(proSimilarityTaskPageParam.getSize()).orElse(20),
                null
                ),
                queryWrapper);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void add(ProSimilarityTaskAddParam proSimilarityTaskAddParam) {
        ProSimilarityTask bean = BeanUtil.toBean(proSimilarityTaskAddParam, ProSimilarityTask.class);
        this.save(bean);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void edit(ProSimilarityTaskEditParam proSimilarityTaskEditParam) {
        if (!this.exists(new LambdaQueryWrapper<ProSimilarityTask>().eq(ProSimilarityTask::getId, proSimilarityTaskEditParam.getId()))) {
            throw new BusinessException(ResultCode.PARAM_ERROR);
        }
        ProSimilarityTask bean = BeanUtil.toBean(proSimilarityTaskEditParam, ProSimilarityTask.class);
        BeanUtil.copyProperties(proSimilarityTaskEditParam, bean);
        this.updateById(bean);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void delete(List<ProSimilarityTaskIdParam> proSimilarityTaskIdParamList) {
        if (ObjectUtil.isEmpty(proSimilarityTaskIdParamList)) {
            throw new BusinessException(ResultCode.PARAM_ERROR);
        }
        this.removeByIds(CollStreamUtil.toList(proSimilarityTaskIdParamList, ProSimilarityTaskIdParam::getId));
    }

    @Override
    public ProSimilarityTask detail(ProSimilarityTaskIdParam proSimilarityTaskIdParam) {
        ProSimilarityTask proSimilarityTask = this.getById(proSimilarityTaskIdParam.getId());
        if (ObjectUtil.isEmpty(proSimilarityTask)) {
            throw new BusinessException(ResultCode.PARAM_ERROR);
        }
        return proSimilarityTask;
    }

}