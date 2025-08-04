package io.charlie.app.core.modular.set.similarity.result.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import io.charlie.app.core.modular.set.similarity.result.entity.ProSetSimilarityResult;
import io.charlie.app.core.modular.set.similarity.result.param.ProSetSimilarityResultAddParam;
import io.charlie.app.core.modular.set.similarity.result.param.ProSetSimilarityResultEditParam;
import io.charlie.app.core.modular.set.similarity.result.param.ProSetSimilarityResultIdParam;
import io.charlie.app.core.modular.set.similarity.result.param.ProSetSimilarityResultPageParam;

import java.util.List;

/**
* @author Charlie Zhang
* @version v1.0
* @date 2025-07-25
* @description 题单代码相似度检测结果详情表 服务类
*/
public interface ProSetSimilarityResultService extends IService<ProSetSimilarityResult> {
    Page<ProSetSimilarityResult> page(ProSetSimilarityResultPageParam proSetSimilarityResultPageParam);

    void add(ProSetSimilarityResultAddParam proSetSimilarityResultAddParam);

    void edit(ProSetSimilarityResultEditParam proSetSimilarityResultEditParam);

    void delete(List<ProSetSimilarityResultIdParam> proSetSimilarityResultIdParamList);

    ProSetSimilarityResult detail(ProSetSimilarityResultIdParam proSetSimilarityResultIdParam);

}