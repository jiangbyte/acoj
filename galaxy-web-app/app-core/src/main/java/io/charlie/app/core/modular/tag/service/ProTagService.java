package io.charlie.app.core.modular.tag.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import io.charlie.app.core.modular.category.param.ProCategoryOptionParam;
import io.charlie.app.core.modular.tag.entity.ProTag;
import io.charlie.app.core.modular.tag.param.*;
import io.charlie.galaxy.option.LabelOption;

import java.util.List;

/**
* @author Charlie Zhang
* @version v1.0
* @date 2025-07-25
* @description 标签表 服务类
*/
public interface ProTagService extends IService<ProTag> {
    Page<ProTag> page(ProTagPageParam proTagPageParam);

    void add(ProTagAddParam proTagAddParam);

    void edit(ProTagEditParam proTagEditParam);

    void delete(List<ProTagIdParam> proTagIdParamList);

    ProTag detail(ProTagIdParam proTagIdParam);

    List<LabelOption<String>> options(ProTagOptionParam proTagOptionParam);

}