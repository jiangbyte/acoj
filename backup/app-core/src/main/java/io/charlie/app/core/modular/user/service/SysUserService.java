package io.charlie.app.core.modular.user.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import io.charlie.app.core.modular.user.entity.SysUser;
import io.charlie.app.core.modular.user.param.SysUserAddParam;
import io.charlie.app.core.modular.user.param.SysUserEditParam;
import io.charlie.app.core.modular.user.param.SysUserIdParam;
import io.charlie.app.core.modular.user.param.SysUserPageParam;

import java.util.List;

/**
* @author Charlie Zhang
* @version v1.0
* @date 2025-07-22
* @description 用户表 服务类
*/
public interface SysUserService extends IService<SysUser> {
    Page<SysUser> page(SysUserPageParam sysUserPageParam);

    void add(SysUserAddParam sysUserAddParam);

    void edit(SysUserEditParam sysUserEditParam);

    void delete(List<SysUserIdParam> sysUserIdParamList);

    SysUser detail(SysUserIdParam sysUserIdParam);

    SysUser queryEntity(String id);

    }