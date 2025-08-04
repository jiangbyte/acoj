package io.charlie.app.core.modular.problem.similarity.result.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import io.charlie.app.core.modular.problem.similarity.result.entity.ProSimilarityResult;
import io.charlie.app.core.modular.problem.similarity.result.param.ProSimilarityResultAddParam;
import io.charlie.app.core.modular.problem.similarity.result.param.ProSimilarityResultEditParam;
import io.charlie.app.core.modular.problem.similarity.result.param.ProSimilarityResultIdParam;
import io.charlie.app.core.modular.problem.similarity.result.param.ProSimilarityResultPageParam;

import java.util.List;

/**
* @author Charlie Zhang
* @version v1.0
* @date 2025-07-25
* @description 代码相似度检测结果详情表 服务类
*/
public interface ProSimilarityResultService extends IService<ProSimilarityResult> {
    Page<ProSimilarityResult> page(ProSimilarityResultPageParam proSimilarityResultPageParam);

    void add(ProSimilarityResultAddParam proSimilarityResultAddParam);

    void edit(ProSimilarityResultEditParam proSimilarityResultEditParam);

    void delete(List<ProSimilarityResultIdParam> proSimilarityResultIdParamList);

    ProSimilarityResult detail(ProSimilarityResultIdParam proSimilarityResultIdParam);

}