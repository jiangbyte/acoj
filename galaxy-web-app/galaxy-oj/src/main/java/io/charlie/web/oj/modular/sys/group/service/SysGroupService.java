package io.charlie.web.oj.modular.sys.group.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import io.charlie.galaxy.option.LabelOption;
import io.charlie.web.oj.modular.sys.group.entity.SysGroup;
import io.charlie.web.oj.modular.sys.group.param.*;

import java.util.List;

/**
* @author Charlie Zhang
* @version v1.0
* @date 2025-09-20
* @description 用户组表 服务类
*/
public interface SysGroupService extends IService<SysGroup> {
    Page<SysGroup> page(SysGroupPageParam sysGroupPageParam);

    void add(SysGroupAddParam sysGroupAddParam);

    void edit(SysGroupEditParam sysGroupEditParam);

    void delete(List<SysGroupIdParam> sysGroupIdParamList);

    SysGroup detail(SysGroupIdParam sysGroupIdParam);

    List<LabelOption<String>> options(SysGroupOptionParam sysGroupOptionParam);

}