package io.charlie.web.oj.modular.sys.log.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import io.charlie.web.oj.modular.sys.log.entity.SysLog;
import io.charlie.web.oj.modular.sys.log.param.SysLogAddParam;
import io.charlie.web.oj.modular.sys.log.param.SysLogEditParam;
import io.charlie.web.oj.modular.sys.log.param.SysLogIdParam;
import io.charlie.web.oj.modular.sys.log.param.SysLogPageParam;

import java.util.List;

/**
* @author Charlie Zhang
* @version v1.0
* @date 2025-10-17
* @description 系统活动/日志记录表 服务类
*/
public interface SysLogService extends IService<SysLog> {
    Page<SysLog> page(SysLogPageParam sysLogPageParam);

    void add(SysLogAddParam sysLogAddParam);

    void edit(SysLogEditParam sysLogEditParam);

    void delete(List<SysLogIdParam> sysLogIdParamList);

    SysLog detail(SysLogIdParam sysLogIdParam);

    List<SysLog> recent(int count);

}