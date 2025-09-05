package io.charlie.app.core.modular.problem.sample.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import io.charlie.app.core.modular.problem.sample.entity.ProSampleLibrary;
import io.charlie.app.core.modular.problem.sample.param.ProSampleLibraryAddParam;
import io.charlie.app.core.modular.problem.sample.param.ProSampleLibraryEditParam;
import io.charlie.app.core.modular.problem.sample.param.ProSampleLibraryIdParam;
import io.charlie.app.core.modular.problem.sample.param.ProSampleLibraryPageParam;

import java.util.List;

/**
* @author Charlie Zhang
* @version v1.0
* @date 2025-09-05
* @description 题目提交样本库 服务类
*/
public interface ProSampleLibraryService extends IService<ProSampleLibrary> {
    Page<ProSampleLibrary> page(ProSampleLibraryPageParam proSampleLibraryPageParam);

    void add(ProSampleLibraryAddParam proSampleLibraryAddParam);

    void edit(ProSampleLibraryEditParam proSampleLibraryEditParam);

    void delete(List<ProSampleLibraryIdParam> proSampleLibraryIdParamList);

    ProSampleLibrary detail(ProSampleLibraryIdParam proSampleLibraryIdParam);

}