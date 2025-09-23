package io.charlie.web.oj.modular.data.reports.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import io.charlie.web.oj.modular.data.reports.entity.TaskReports;
import io.charlie.web.oj.modular.data.reports.param.TaskReportsAddParam;
import io.charlie.web.oj.modular.data.reports.param.TaskReportsEditParam;
import io.charlie.web.oj.modular.data.reports.param.TaskReportsIdParam;
import io.charlie.web.oj.modular.data.reports.param.TaskReportsPageParam;

import java.util.List;

/**
* @author Charlie Zhang
* @version v1.0
* @date 2025-09-21
* @description 报告库表 服务类
*/
public interface TaskReportsService extends IService<TaskReports> {
    Page<TaskReports> page(TaskReportsPageParam taskReportsPageParam);

    void add(TaskReportsAddParam taskReportsAddParam);

    void edit(TaskReportsEditParam taskReportsEditParam);

    void delete(List<TaskReportsIdParam> taskReportsIdParamList);

    TaskReports detail(TaskReportsIdParam taskReportsIdParam);

}