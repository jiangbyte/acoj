package io.charlie.web.oj.modular.sys.menu.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import io.charlie.web.oj.modular.sys.menu.entity.SysMenu;
import io.charlie.web.oj.modular.sys.menu.param.*;

import java.util.List;

/**
 * @author Charlie Zhang
 * @version v1.0
 * @date 2025-09-24
 * @description 菜单表 服务类
 */
public interface SysMenuService extends IService<SysMenu> {
    Page<SysMenu> page(SysMenuPageParam sysMenuPageParam);

    void add(SysMenuAddParam sysMenuAddParam);

    void edit(SysMenuEditParam sysMenuEditParam);

    void delete(List<SysMenuIdParam> sysMenuIdParamList);

    SysMenu detail(SysMenuIdParam sysMenuIdParam);

    List<SysMenu> menuTree();

    List<SysMenu> authMenu();

    List<SysMenu> authMenu1();

    void assignMenuPermission(SysMenuPermissionParam sysMenuPermissionParam);
}