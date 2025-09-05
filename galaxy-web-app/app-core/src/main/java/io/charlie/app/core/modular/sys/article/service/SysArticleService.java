package io.charlie.app.core.modular.sys.article.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import io.charlie.app.core.modular.sys.article.entity.SysArticle;
import io.charlie.app.core.modular.sys.article.param.*;
import io.charlie.galaxy.option.LabelOption;

import java.util.List;

/**
* @author Charlie Zhang
* @version v1.0
* @date 2025-07-25
* @description 系统文章表 服务类
*/
public interface SysArticleService extends IService<SysArticle> {
    Page<SysArticle> page(SysArticlePageParam sysArticlePageParam);

    void add(SysArticleAddParam sysArticleAddParam);

    void edit(SysArticleEditParam sysArticleEditParam);

    void delete(List<SysArticleIdParam> sysArticleIdParamList);

    SysArticle detail(SysArticleIdParam sysArticleIdParam);

    List<LabelOption<String>> options(SysArticleOptionParam sysArticleOptionParam);
}