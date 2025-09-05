package io.charlie.app.core.modular.set.sample.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import io.charlie.app.core.modular.set.sample.entity.ProSetSampleLibrary;
import io.charlie.app.core.modular.set.sample.param.ProSetSampleLibraryAddParam;
import io.charlie.app.core.modular.set.sample.param.ProSetSampleLibraryEditParam;
import io.charlie.app.core.modular.set.sample.param.ProSetSampleLibraryIdParam;
import io.charlie.app.core.modular.set.sample.param.ProSetSampleLibraryPageParam;

import java.util.List;

/**
* @author Charlie Zhang
* @version v1.0
* @date 2025-09-05
* @description 题目题集提交样本库 服务类
*/
public interface ProSetSampleLibraryService extends IService<ProSetSampleLibrary> {
    Page<ProSetSampleLibrary> page(ProSetSampleLibraryPageParam proSetSampleLibraryPageParam);

    void add(ProSetSampleLibraryAddParam proSetSampleLibraryAddParam);

    void edit(ProSetSampleLibraryEditParam proSetSampleLibraryEditParam);

    void delete(List<ProSetSampleLibraryIdParam> proSetSampleLibraryIdParamList);

    ProSetSampleLibrary detail(ProSetSampleLibraryIdParam proSetSampleLibraryIdParam);

}