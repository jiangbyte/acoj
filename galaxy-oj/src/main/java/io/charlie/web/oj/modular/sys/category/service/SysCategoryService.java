package io.charlie.web.oj.modular.sys.category.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import io.charlie.galaxy.option.LabelOption;
import io.charlie.web.oj.modular.sys.category.entity.SysCategory;
import io.charlie.web.oj.modular.sys.category.param.*;

import java.util.List;

/**
* @author Charlie Zhang
* @version v1.0
* @date 2025-09-20
* @description 分类表 服务类
*/
public interface SysCategoryService extends IService<SysCategory> {
    Page<SysCategory> page(SysCategoryPageParam sysCategoryPageParam);

    void add(SysCategoryAddParam sysCategoryAddParam);

    void edit(SysCategoryEditParam sysCategoryEditParam);

    void delete(List<SysCategoryIdParam> sysCategoryIdParamList);

    SysCategory detail(SysCategoryIdParam sysCategoryIdParam);

    List<LabelOption<String>> options(SysCategoryOptionParam sysCategoryOptionParam);
}