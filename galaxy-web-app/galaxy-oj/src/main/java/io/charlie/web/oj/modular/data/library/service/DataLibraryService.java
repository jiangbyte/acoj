package io.charlie.web.oj.modular.data.library.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import io.charlie.web.oj.modular.data.library.entity.DataLibrary;
import io.charlie.web.oj.modular.data.library.param.DataLibraryAddParam;
import io.charlie.web.oj.modular.data.library.param.DataLibraryEditParam;
import io.charlie.web.oj.modular.data.library.param.DataLibraryIdParam;
import io.charlie.web.oj.modular.data.library.param.DataLibraryPageParam;
import io.charlie.web.oj.modular.data.submit.entity.DataSubmit;

import java.util.List;

/**
* @author Charlie Zhang
* @version v1.0
* @date 2025-09-20
* @description 提交样本库 服务类
*/
public interface DataLibraryService extends IService<DataLibrary> {
    Page<DataLibrary> page(DataLibraryPageParam dataLibraryPageParam);

    void add(DataLibraryAddParam dataLibraryAddParam);

    void edit(DataLibraryEditParam dataLibraryEditParam);

    void delete(List<DataLibraryIdParam> dataLibraryIdParamList);

    DataLibrary detail(DataLibraryIdParam dataLibraryIdParam);

    void addLibrary(DataSubmit submit);

}