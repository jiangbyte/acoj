package io.charlie.app.core.modular.set.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import io.charlie.app.core.modular.set.entity.ProSet;
import io.charlie.app.core.modular.set.param.ProSetAddParam;
import io.charlie.app.core.modular.set.param.ProSetEditParam;
import io.charlie.app.core.modular.set.param.ProSetIdParam;
import io.charlie.app.core.modular.set.param.ProSetPageParam;

import java.util.List;

/**
* @author Charlie Zhang
* @version v1.0
* @date 2025-07-22
* @description 题集 服务类
*/
public interface ProSetService extends IService<ProSet> {
    Page<ProSet> page(ProSetPageParam proSetPageParam);

    void add(ProSetAddParam proSetAddParam);

    void edit(ProSetEditParam proSetEditParam);

    void delete(List<ProSetIdParam> proSetIdParamList);

    ProSet detail(ProSetIdParam proSetIdParam);

    ProSet queryEntity(String id);

    }