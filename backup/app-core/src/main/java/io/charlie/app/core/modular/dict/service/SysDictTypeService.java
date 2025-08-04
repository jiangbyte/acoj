package io.charlie.app.core.modular.dict.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import io.charlie.app.core.modular.dict.entity.SysDictType;
import io.charlie.app.core.modular.dict.param.SysDictTypeAddParam;
import io.charlie.app.core.modular.dict.param.SysDictTypeEditParam;
import io.charlie.app.core.modular.dict.param.SysDictTypeIdParam;
import io.charlie.app.core.modular.dict.param.SysDictTypePageParam;

import java.util.List;

/**
* @author Charlie Zhang
* @version v1.0
* @date 2025-07-25
* @description 字典类型表 服务类
*/
public interface SysDictTypeService extends IService<SysDictType> {
    Page<SysDictType> page(SysDictTypePageParam sysDictTypePageParam);

    void add(SysDictTypeAddParam sysDictTypeAddParam);

    void edit(SysDictTypeEditParam sysDictTypeEditParam);

    void delete(List<SysDictTypeIdParam> sysDictTypeIdParamList);

    SysDictType detail(SysDictTypeIdParam sysDictTypeIdParam);

}