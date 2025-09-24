package io.charlie.web.oj.modular.data.set.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import io.charlie.web.oj.modular.data.problem.entity.DataProblem;
import io.charlie.web.oj.modular.data.set.entity.DataSet;
import io.charlie.web.oj.modular.data.set.param.*;

import java.util.List;

/**
* @author Charlie Zhang
* @version v1.0
* @date 2025-09-20
* @description 题集 服务类
*/
public interface DataSetService extends IService<DataSet> {
    Page<DataSet> page(DataSetPageParam dataSetPageParam);

    void add(DataSetAddParam dataSetAddParam);

    void edit(DataSetEditParam dataSetEditParam);

    void delete(List<DataSetIdParam> dataSetIdParamList);

    DataSet detail(DataSetIdParam dataSetIdParam);

    List<DataSet> latestN(int n);

    List<DataProblem> getSetProblem(DataSetProblemParam dataSetProblemParam);

    DataProblem getSetProblemDetail(DataSetProblemDetailParam dataSetProblemDetailParam);

    List<DataSet> getHotN(int n);
}