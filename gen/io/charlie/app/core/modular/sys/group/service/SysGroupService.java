package io.charlie.app.core.modular.sys.group.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import io.charlie.app.core.modular.sys.group.entity.SysGroup;
import io.charlie.app.core.modular.sys.group.param.SysGroupAddParam;
import io.charlie.app.core.modular.sys.group.param.SysGroupEditParam;
import io.charlie.app.core.modular.sys.group.param.SysGroupIdParam;
import io.charlie.app.core.modular.sys.group.param.SysGroupPageParam;

import java.util.List;

/**
* @author Charlie Zhang
* @version v1.0
* @date 2025-07-27
* @description 用户组表 服务类
*/
public interface SysGroupService extends IService<SysGroup> {
    Page<SysGroup> page(SysGroupPageParam sysGroupPageParam);

    void add(SysGroupAddParam sysGroupAddParam);

    void edit(SysGroupEditParam sysGroupEditParam);

    void delete(List<SysGroupIdParam> sysGroupIdParamList);

    SysGroup detail(SysGroupIdParam sysGroupIdParam);

}