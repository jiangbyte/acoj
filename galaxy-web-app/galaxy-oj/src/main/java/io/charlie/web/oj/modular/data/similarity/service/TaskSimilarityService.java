package io.charlie.web.oj.modular.data.similarity.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import io.charlie.web.oj.modular.data.similarity.entity.TaskSimilarity;
import io.charlie.web.oj.modular.data.similarity.param.TaskSimilarityAddParam;
import io.charlie.web.oj.modular.data.similarity.param.TaskSimilarityEditParam;
import io.charlie.web.oj.modular.data.similarity.param.TaskSimilarityIdParam;
import io.charlie.web.oj.modular.data.similarity.param.TaskSimilarityPageParam;

import java.util.List;

/**
* @author Charlie Zhang
* @version v1.0
* @date 2025-09-21
* @description 检测结果任务库 服务类
*/
public interface TaskSimilarityService extends IService<TaskSimilarity> {
    Page<TaskSimilarity> page(TaskSimilarityPageParam taskSimilarityPageParam);

    void add(TaskSimilarityAddParam taskSimilarityAddParam);

    void edit(TaskSimilarityEditParam taskSimilarityEditParam);

    void delete(List<TaskSimilarityIdParam> taskSimilarityIdParamList);

    TaskSimilarity detail(TaskSimilarityIdParam taskSimilarityIdParam);

}