package io.charlie.app.core.modular.set.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import io.charlie.app.core.modular.set.entity.progress.ProSetProgress;
import io.charlie.app.core.modular.set.param.progress.ProSetProgressAddParam;
import io.charlie.app.core.modular.set.param.progress.ProSetProgressEditParam;
import io.charlie.app.core.modular.set.param.progress.ProSetProgressIdParam;
import io.charlie.app.core.modular.set.param.progress.ProSetProgressPageParam;

import java.util.List;

/**
* @author Charlie Zhang
* @version v1.0
* @date 2025-07-22
* @description 题集进度表 服务类
*/
public interface ProSetProgressService extends IService<ProSetProgress> {
    Page<ProSetProgress> page(ProSetProgressPageParam proSetProgressPageParam);

    void add(ProSetProgressAddParam proSetProgressAddParam);

    void edit(ProSetProgressEditParam proSetProgressEditParam);

    void delete(List<ProSetProgressIdParam> proSetProgressIdParamList);

    ProSetProgress detail(ProSetProgressIdParam proSetProgressIdParam);

    ProSetProgress queryEntity(String id);

    }