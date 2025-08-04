package io.charlie.app.core.modular.set.similarity.task.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import io.charlie.app.core.modular.set.similarity.task.entity.ProSetSimilarityTask;
import io.charlie.app.core.modular.set.similarity.task.param.ProSetSimilarityTaskAddParam;
import io.charlie.app.core.modular.set.similarity.task.param.ProSetSimilarityTaskEditParam;
import io.charlie.app.core.modular.set.similarity.task.param.ProSetSimilarityTaskIdParam;
import io.charlie.app.core.modular.set.similarity.task.param.ProSetSimilarityTaskPageParam;

import java.util.List;

/**
* @author Charlie Zhang
* @version v1.0
* @date 2025-07-27
* @description 题单代码相似度检测任务表 服务类
*/
public interface ProSetSimilarityTaskService extends IService<ProSetSimilarityTask> {
    Page<ProSetSimilarityTask> page(ProSetSimilarityTaskPageParam proSetSimilarityTaskPageParam);

    void add(ProSetSimilarityTaskAddParam proSetSimilarityTaskAddParam);

    void edit(ProSetSimilarityTaskEditParam proSetSimilarityTaskEditParam);

    void delete(List<ProSetSimilarityTaskIdParam> proSetSimilarityTaskIdParamList);

    ProSetSimilarityTask detail(ProSetSimilarityTaskIdParam proSetSimilarityTaskIdParam);

}