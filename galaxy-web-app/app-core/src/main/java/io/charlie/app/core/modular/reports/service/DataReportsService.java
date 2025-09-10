package io.charlie.app.core.modular.reports.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import io.charlie.app.core.modular.reports.entity.DataReports;
import io.charlie.app.core.modular.reports.param.DataReportsAddParam;
import io.charlie.app.core.modular.reports.param.DataReportsEditParam;
import io.charlie.app.core.modular.reports.param.DataReportsIdParam;
import io.charlie.app.core.modular.reports.param.DataReportsPageParam;

import java.util.List;

/**
* @author Charlie Zhang
* @version v1.0
* @date 2025-09-09
* @description 报告库表 服务类
*/
public interface DataReportsService extends IService<DataReports> {
    Page<DataReports> page(DataReportsPageParam dataReportsPageParam);

    void add(DataReportsAddParam dataReportsAddParam);

    void edit(DataReportsEditParam dataReportsEditParam);

    void delete(List<DataReportsIdParam> dataReportsIdParamList);

    DataReports detail(DataReportsIdParam dataReportsIdParam);

}