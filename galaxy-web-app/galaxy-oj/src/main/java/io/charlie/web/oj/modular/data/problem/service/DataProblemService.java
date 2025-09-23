package io.charlie.web.oj.modular.data.problem.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import io.charlie.web.oj.modular.data.problem.entity.DataProblem;
import io.charlie.web.oj.modular.data.problem.entity.DataProblemCount;
import io.charlie.web.oj.modular.data.problem.param.DataProblemAddParam;
import io.charlie.web.oj.modular.data.problem.param.DataProblemEditParam;
import io.charlie.web.oj.modular.data.problem.param.DataProblemIdParam;
import io.charlie.web.oj.modular.data.problem.param.DataProblemPageParam;

import java.util.List;

/**
* @author Charlie Zhang
* @version v1.0
* @date 2025-09-20
* @description 题目表 服务类
*/
public interface DataProblemService extends IService<DataProblem> {
    Page<DataProblem> page(DataProblemPageParam dataProblemPageParam);

    void add(DataProblemAddParam dataProblemAddParam);

    void edit(DataProblemEditParam dataProblemEditParam);

    void delete(List<DataProblemIdParam> dataProblemIdParamList);

    DataProblem detail(DataProblemIdParam dataProblemIdParam);

    List<DataProblem> latestN(int n);

    DataProblemCount getProblemCount();

    List<DataProblem> getHotN(int n);
}