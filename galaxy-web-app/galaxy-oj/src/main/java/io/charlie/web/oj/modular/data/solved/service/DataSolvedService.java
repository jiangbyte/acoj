package io.charlie.web.oj.modular.data.solved.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import io.charlie.web.oj.modular.data.solved.entity.DataSolved;
import io.charlie.web.oj.modular.data.solved.entity.ProblemOverallStats;
import io.charlie.web.oj.modular.data.solved.entity.ProblemStatistics;
import io.charlie.web.oj.modular.data.solved.param.DataSolvedAddParam;
import io.charlie.web.oj.modular.data.solved.param.DataSolvedEditParam;
import io.charlie.web.oj.modular.data.solved.param.DataSolvedIdParam;
import io.charlie.web.oj.modular.data.solved.param.DataSolvedPageParam;

import java.util.List;
import java.util.Map;

/**
* @author Charlie Zhang
* @version v1.0
* @date 2025-09-20
* @description 用户解决表 服务类
*/
public interface DataSolvedService extends IService<DataSolved> {
    Page<DataSolved> page(DataSolvedPageParam dataSolvedPageParam);

    void add(DataSolvedAddParam dataSolvedAddParam);

    void edit(DataSolvedEditParam dataSolvedEditParam);

    void delete(List<DataSolvedIdParam> dataSolvedIdParamList);

    DataSolved detail(DataSolvedIdParam dataSolvedIdParam);

    ProblemOverallStats getProblemOverallStats();

    Map<String, ProblemStatistics> getBatchProblemStatistics(List<String> problemIds);

    Map<String, ProblemStatistics> getBatchSetProblemStatistics(String setId, List<String> problemIds);


}