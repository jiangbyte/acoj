package io.charlie.web.oj.modular.data.progress.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import io.charlie.web.oj.modular.data.progress.entity.DataProgress;
import io.charlie.web.oj.modular.data.progress.param.DataProgressAddParam;
import io.charlie.web.oj.modular.data.progress.param.DataProgressEditParam;
import io.charlie.web.oj.modular.data.progress.param.DataProgressIdParam;
import io.charlie.web.oj.modular.data.progress.param.DataProgressPageParam;

import java.util.List;

/**
* @author Charlie Zhang
* @version v1.0
* @date 2025-09-20
* @description 题集进度表 服务类
*/
public interface DataProgressService extends IService<DataProgress> {
    Page<DataProgress> page(DataProgressPageParam dataProgressPageParam);

    void add(DataProgressAddParam dataProgressAddParam);

    void edit(DataProgressEditParam dataProgressEditParam);

    void delete(List<DataProgressIdParam> dataProgressIdParamList);

    DataProgress detail(DataProgressIdParam dataProgressIdParam);

}