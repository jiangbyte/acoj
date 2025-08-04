package io.charlie.app.core.modular.dict.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import io.charlie.app.core.modular.dict.entity.SysDictData;
import io.charlie.app.core.modular.dict.param.SysDictDataAddParam;
import io.charlie.app.core.modular.dict.param.SysDictDataEditParam;
import io.charlie.app.core.modular.dict.param.SysDictDataIdParam;
import io.charlie.app.core.modular.dict.param.SysDictDataPageParam;

import java.util.List;

/**
* @author Charlie Zhang
* @version v1.0
* @date 2025-07-25
* @description 字典数据表 服务类
*/
public interface SysDictDataService extends IService<SysDictData> {
    Page<SysDictData> page(SysDictDataPageParam sysDictDataPageParam);

    void add(SysDictDataAddParam sysDictDataAddParam);

    void edit(SysDictDataEditParam sysDictDataEditParam);

    void delete(List<SysDictDataIdParam> sysDictDataIdParamList);

    SysDictData detail(SysDictDataIdParam sysDictDataIdParam);

}