package io.charlie.app.core.modular.sys.category.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import io.charlie.app.core.modular.sys.category.entity.SysCategory;
import io.charlie.app.core.modular.sys.category.param.*;
import io.charlie.galaxy.option.LabelOption;

import java.util.List;

/**
* @author Charlie Zhang
* @version v1.0
* @date 2025-07-25
* @description 分类表 服务类
*/
public interface SysCategoryService extends IService<SysCategory> {
    Page<SysCategory> page(SysCategoryPageParam proCategoryPageParam);

    void add(SysCategoryAddParam sysCategoryAddParam);

    void edit(SysCategoryEditParam sysCategoryEditParam);

    void delete(List<SysCategoryIdParam> sysCategoryIdParams);

    SysCategory detail(SysCategoryIdParam sysCategoryIdParam);

    List<LabelOption<String>> options(SysCategoryOptionParam sysCategoryOptionParam);
}