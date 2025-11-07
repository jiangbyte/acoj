package io.charlie.web.oj.modular.sys.config.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import io.charlie.web.oj.modular.sys.config.entity.SysConfig;
import io.charlie.web.oj.modular.sys.config.param.*;

import java.util.List;

/**
* @author Charlie Zhang
* @version v1.0
* @date 2025-09-20
* @description 系统配置表 服务类
*/
public interface SysConfigService extends IService<SysConfig> {
    Page<SysConfig> page(SysConfigPageParam sysConfigPageParam);

    List<SysConfig> listAll(SysConfigListParam sysConfigListParam);

    void add(SysConfigAddParam sysConfigAddParam);

    void edit(SysConfigEditParam sysConfigEditParam);

    void delete(List<SysConfigIdParam> sysConfigIdParamList);

    SysConfig detail(SysConfigIdParam sysConfigIdParam);

    String getValueByCode(String code);

}