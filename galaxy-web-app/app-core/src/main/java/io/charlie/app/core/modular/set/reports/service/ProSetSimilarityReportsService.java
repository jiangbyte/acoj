package io.charlie.app.core.modular.set.reports.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import io.charlie.app.core.modular.set.reports.entity.ProSetSimilarityReports;
import io.charlie.app.core.modular.set.reports.param.ProSetSimilarityReportsAddParam;
import io.charlie.app.core.modular.set.reports.param.ProSetSimilarityReportsEditParam;
import io.charlie.app.core.modular.set.reports.param.ProSetSimilarityReportsIdParam;
import io.charlie.app.core.modular.set.reports.param.ProSetSimilarityReportsPageParam;

import java.util.List;

/**
* @author Charlie Zhang
* @version v1.0
* @date 2025-09-08
* @description 题库题目报告库表 服务类
*/
public interface ProSetSimilarityReportsService extends IService<ProSetSimilarityReports> {
    Page<ProSetSimilarityReports> page(ProSetSimilarityReportsPageParam proSetSimilarityReportsPageParam);

    void add(ProSetSimilarityReportsAddParam proSetSimilarityReportsAddParam);

    void edit(ProSetSimilarityReportsEditParam proSetSimilarityReportsEditParam);

    void delete(List<ProSetSimilarityReportsIdParam> proSetSimilarityReportsIdParamList);

    ProSetSimilarityReports detail(ProSetSimilarityReportsIdParam proSetSimilarityReportsIdParam);

}