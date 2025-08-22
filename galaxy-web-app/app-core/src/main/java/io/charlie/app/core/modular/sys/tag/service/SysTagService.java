package io.charlie.app.core.modular.sys.tag.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import io.charlie.app.core.modular.sys.tag.param.*;
import io.charlie.app.core.modular.sys.tag.entity.SysTag;
import io.charlie.galaxy.option.LabelOption;

import java.util.List;

/**
* @author Charlie Zhang
* @version v1.0
* @date 2025-07-25
* @description 标签表 服务类
*/
public interface SysTagService extends IService<SysTag> {
    Page<SysTag> page(SysTagPageParam sysTagPageParam);

    void add(SysTagAddParam sysTagAddParam);

    void edit(SysTagEditParam sysTagEditParam);

    void delete(List<SysTagIdParam> sysTagIdParams);

    SysTag detail(SysTagIdParam sysTagIdParam);

    List<LabelOption<String>> options(SysTagOptionParam sysTagOptionParam);

}