package io.charlie.app.core.modular.problem.similarity.task.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import io.charlie.app.core.modular.problem.similarity.task.entity.ProSimilarityTask;
import io.charlie.app.core.modular.problem.similarity.task.param.ProSimilarityTaskAddParam;
import io.charlie.app.core.modular.problem.similarity.task.param.ProSimilarityTaskEditParam;
import io.charlie.app.core.modular.problem.similarity.task.param.ProSimilarityTaskIdParam;
import io.charlie.app.core.modular.problem.similarity.task.param.ProSimilarityTaskPageParam;

import java.util.List;

/**
* @author Charlie Zhang
* @version v1.0
* @date 2025-07-27
* @description 代码相似度检测任务表 服务类
*/
public interface ProSimilarityTaskService extends IService<ProSimilarityTask> {
    Page<ProSimilarityTask> page(ProSimilarityTaskPageParam proSimilarityTaskPageParam);

    void add(ProSimilarityTaskAddParam proSimilarityTaskAddParam);

    void edit(ProSimilarityTaskEditParam proSimilarityTaskEditParam);

    void delete(List<ProSimilarityTaskIdParam> proSimilarityTaskIdParamList);

    ProSimilarityTask detail(ProSimilarityTaskIdParam proSimilarityTaskIdParam);

}