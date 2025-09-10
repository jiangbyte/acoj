package io.charlie.app.core.modular.problem.similarity.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import io.charlie.app.core.modular.problem.similarity.entity.ProSimilarityDetail;
import io.charlie.app.core.modular.problem.similarity.param.*;

import java.util.List;

/**
* @author Charlie Zhang
* @version v1.0
* @date 2025-09-05
* @description 题目检测结果任务库 服务类
*/
public interface ProSimilarityDetailService extends IService<ProSimilarityDetail> {
    Page<ProSimilarityDetail> page(ProSimilarityDetailPageParam proSimilarityDetailPageParam);

    void add(ProSimilarityDetailAddParam proSimilarityDetailAddParam);

    void edit(ProSimilarityDetailEditParam proSimilarityDetailEditParam);

    void delete(List<ProSimilarityDetailIdParam> proSimilarityDetailIdParamList);

    ProSimilarityDetail detail(ProSimilarityDetailIdParam proSimilarityDetailIdParam);


}