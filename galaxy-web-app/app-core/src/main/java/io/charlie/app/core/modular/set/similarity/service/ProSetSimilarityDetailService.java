package io.charlie.app.core.modular.set.similarity.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import io.charlie.app.core.modular.set.similarity.entity.ProSetSimilarityDetail;
import io.charlie.app.core.modular.set.similarity.param.ProSetSimilarityDetailAddParam;
import io.charlie.app.core.modular.set.similarity.param.ProSetSimilarityDetailEditParam;
import io.charlie.app.core.modular.set.similarity.param.ProSetSimilarityDetailIdParam;
import io.charlie.app.core.modular.set.similarity.param.ProSetSimilarityDetailPageParam;

import java.util.List;

/**
* @author Charlie Zhang
* @version v1.0
* @date 2025-09-05
* @description 题集题目检测结果任务库 服务类
*/
public interface ProSetSimilarityDetailService extends IService<ProSetSimilarityDetail> {
    Page<ProSetSimilarityDetail> page(ProSetSimilarityDetailPageParam proSetSimilarityDetailPageParam);

    void add(ProSetSimilarityDetailAddParam proSetSimilarityDetailAddParam);

    void edit(ProSetSimilarityDetailEditParam proSetSimilarityDetailEditParam);

    void delete(List<ProSetSimilarityDetailIdParam> proSetSimilarityDetailIdParamList);

    ProSetSimilarityDetail detail(ProSetSimilarityDetailIdParam proSetSimilarityDetailIdParam);

}