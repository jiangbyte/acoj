package io.charlie.app.core.modular.sys.dict.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import io.charlie.app.core.modular.sys.dict.entity.SysDict;
import io.charlie.app.core.modular.sys.dict.param.SysDictAddParam;
import io.charlie.app.core.modular.sys.dict.param.SysDictEditParam;
import io.charlie.app.core.modular.sys.dict.param.SysDictIdParam;
import io.charlie.app.core.modular.sys.dict.param.SysDictPageParam;

import java.util.List;

/**
* @author Charlie Zhang
* @version v1.0
* @date 2025-07-27
* @description 系统字典表 服务类
*/
public interface SysDictService extends IService<SysDict> {
    Page<SysDict> page(SysDictPageParam sysDictPageParam);

    void add(SysDictAddParam sysDictAddParam);

    void edit(SysDictEditParam sysDictEditParam);

    void delete(List<SysDictIdParam> sysDictIdParamList);

    SysDict detail(SysDictIdParam sysDictIdParam);

}