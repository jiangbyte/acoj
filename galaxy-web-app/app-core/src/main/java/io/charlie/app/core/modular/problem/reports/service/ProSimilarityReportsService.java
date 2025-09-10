package io.charlie.app.core.modular.problem.reports.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import io.charlie.app.core.modular.problem.reports.entity.ProSimilarityReports;
import io.charlie.app.core.modular.problem.reports.param.*;
import io.charlie.app.core.modular.similarity.param.ProblemReportConfigParam;

import java.util.List;

/**
* @author Charlie Zhang
* @version v1.0
* @date 2025-09-08
* @description 题目报告库表 服务类
*/
public interface ProSimilarityReportsService extends IService<ProSimilarityReports> {
    Page<ProSimilarityReports> page(ProSimilarityReportsPageParam proSimilarityReportsPageParam);

    void add(ProSimilarityReportsAddParam proSimilarityReportsAddParam);

    void edit(ProSimilarityReportsEditParam proSimilarityReportsEditParam);

    void delete(List<ProSimilarityReportsIdParam> proSimilarityReportsIdParamList);

    ProSimilarityReports detail(ProSimilarityReportsIdParam proSimilarityReportsIdParam);

}