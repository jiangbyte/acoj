package io.charlie.web.oj.modular.data.reports.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollStreamUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.charlie.web.oj.modular.data.reports.entity.TaskReports;
import io.charlie.web.oj.modular.data.reports.param.TaskReportsAddParam;
import io.charlie.web.oj.modular.data.reports.param.TaskReportsEditParam;
import io.charlie.web.oj.modular.data.reports.param.TaskReportsIdParam;
import io.charlie.web.oj.modular.data.reports.param.TaskReportsPageParam;
import io.charlie.web.oj.modular.data.reports.mapper.TaskReportsMapper;
import io.charlie.web.oj.modular.data.reports.service.TaskReportsService;
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
* @description 报告库表 服务实现类
*/
@Slf4j
@Service
@RequiredArgsConstructor
public class TaskReportsServiceImpl extends ServiceImpl<TaskReportsMapper, TaskReports> implements TaskReportsService {

    @Override
    public Page<TaskReports> page(TaskReportsPageParam taskReportsPageParam) {
        QueryWrapper<TaskReports> queryWrapper = new QueryWrapper<TaskReports>().checkSqlInjection();
        // 降序
        queryWrapper.lambda().orderByDesc(TaskReports::getCreateTime);
        if (ObjectUtil.isAllNotEmpty(taskReportsPageParam.getSortField(), taskReportsPageParam.getSortOrder()) && ISortOrderEnum.isValid(taskReportsPageParam.getSortOrder())) {
            queryWrapper.orderBy(
                    true,
                    taskReportsPageParam.getSortOrder().equals(ISortOrderEnum.ASCEND.getValue()),
                    StrUtil.toUnderlineCase(taskReportsPageParam.getSortField()));
        }

        return this.page(CommonPageRequest.Page(
                        Optional.ofNullable(taskReportsPageParam.getCurrent()).orElse(1),
                        Optional.ofNullable(taskReportsPageParam.getSize()).orElse(20),
                null
                ),
                queryWrapper);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void add(TaskReportsAddParam taskReportsAddParam) {
        TaskReports bean = BeanUtil.toBean(taskReportsAddParam, TaskReports.class);
        this.save(bean);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void edit(TaskReportsEditParam taskReportsEditParam) {
        if (!this.exists(new LambdaQueryWrapper<TaskReports>().eq(TaskReports::getId, taskReportsEditParam.getId()))) {
            throw new BusinessException(ResultCode.PARAM_ERROR);
        }
        TaskReports bean = BeanUtil.toBean(taskReportsEditParam, TaskReports.class);
        BeanUtil.copyProperties(taskReportsEditParam, bean);
        this.updateById(bean);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void delete(List<TaskReportsIdParam> taskReportsIdParamList) {
        if (ObjectUtil.isEmpty(taskReportsIdParamList)) {
            throw new BusinessException(ResultCode.PARAM_ERROR);
        }
        this.removeByIds(CollStreamUtil.toList(taskReportsIdParamList, TaskReportsIdParam::getId));
    }

    @Override
    public TaskReports detail(TaskReportsIdParam taskReportsIdParam) {
        TaskReports taskReports = this.getById(taskReportsIdParam.getId());
        if (ObjectUtil.isEmpty(taskReports)) {
            throw new BusinessException(ResultCode.PARAM_ERROR);
        }
        return taskReports;
    }

}