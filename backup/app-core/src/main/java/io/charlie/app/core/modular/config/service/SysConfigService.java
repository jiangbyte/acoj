package io.charlie.app.core.modular.config.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import io.charlie.app.core.modular.config.entity.SysConfig;
import io.charlie.app.core.modular.config.param.SysConfigAddParam;
import io.charlie.app.core.modular.config.param.SysConfigEditParam;
import io.charlie.app.core.modular.config.param.SysConfigIdParam;
import io.charlie.app.core.modular.config.param.SysConfigPageParam;

import java.util.List;

/**
* @author Charlie Zhang
* @version v1.0
* @date 2025-07-25
* @description 系统配置表 服务类
*/
public interface SysConfigService extends IService<SysConfig> {
    Page<SysConfig> page(SysConfigPageParam sysConfigPageParam);

    void add(SysConfigAddParam sysConfigAddParam);

    void edit(SysConfigEditParam sysConfigEditParam);

    void delete(List<SysConfigIdParam> sysConfigIdParamList);

    SysConfig detail(SysConfigIdParam sysConfigIdParam);

}