package io.charlie.app.core.modular.sys.dict.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import io.charlie.app.core.modular.sys.dict.entity.SysDictData;
import io.charlie.app.core.modular.sys.dict.param.SysDictDataAddParam;
import io.charlie.app.core.modular.sys.dict.param.SysDictDataEditParam;
import io.charlie.app.core.modular.sys.dict.param.SysDictDataIdParam;
import io.charlie.app.core.modular.sys.dict.param.SysDictDataPageParam;
import io.charlie.galaxy.option.LabelOption;

import java.util.List;

/**
 * @author Charlie Zhang
 * @version v1.0
 * @date 2025-07-25
 * @description 字典数据表 服务类
 */
public interface SysDictDataService extends IService<SysDictData> {
    Page<SysDictData> page(SysDictDataPageParam sysDictDataPageParam);

    void add(SysDictDataAddParam sysDictDataAddParam);

    void edit(SysDictDataEditParam sysDictDataEditParam);

    void delete(List<SysDictDataIdParam> sysDictDataIdParamList);

    SysDictData detail(SysDictDataIdParam sysDictDataIdParam);

    List<SysDictData> listById(String typeId);

    List<LabelOption<String>> listByCode(String typeCode);
}