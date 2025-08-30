package io.charlie.app.core.modular.set.progress.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import io.charlie.app.core.modular.set.progress.entity.ProSetProgress;
import io.charlie.app.core.modular.set.progress.entity.ProSetProgressData;
import io.charlie.app.core.modular.set.progress.param.*;

import java.util.List;

/**
* @author Charlie Zhang
* @version v1.0
* @date 2025-07-25
* @description 题集进度表 服务类
*/
public interface ProSetProgressService extends IService<ProSetProgress> {
    Page<ProSetProgress> page(ProSetProgressPageParam proSetProgressPageParam);

    void add(ProSetProgressAddParam proSetProgressAddParam);

    void edit(ProSetProgressEditParam proSetProgressEditParam);

    void delete(List<ProSetProgressIdParam> proSetProgressIdParamList);

    ProSetProgress detail(ProSetProgressIdParam proSetProgressIdParam);

    Page<ProSetProgressData> progressDataPage(ProSetProgressDataPageParam proSetProgressDataPageParam);
}