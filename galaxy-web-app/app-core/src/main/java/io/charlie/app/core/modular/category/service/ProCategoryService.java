package io.charlie.app.core.modular.category.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import io.charlie.app.core.modular.category.entity.ProCategory;
import io.charlie.app.core.modular.category.param.*;
import io.charlie.galaxy.option.LabelOption;

import java.util.List;

/**
* @author Charlie Zhang
* @version v1.0
* @date 2025-07-25
* @description 分类表 服务类
*/
public interface ProCategoryService extends IService<ProCategory> {
    Page<ProCategory> page(ProCategoryPageParam proCategoryPageParam);

    void add(ProCategoryAddParam proCategoryAddParam);

    void edit(ProCategoryEditParam proCategoryEditParam);

    void delete(List<ProCategoryIdParam> proCategoryIdParamList);

    ProCategory detail(ProCategoryIdParam proCategoryIdParam);

    List<LabelOption<String>> options(ProCategoryOptionParam proCategoryOptionParam);
}