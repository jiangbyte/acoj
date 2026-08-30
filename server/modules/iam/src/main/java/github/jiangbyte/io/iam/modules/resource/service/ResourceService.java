package github.jiangbyte.io.iam.modules.resource.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import github.jiangbyte.io.common.core.param.IdsParam;
import github.jiangbyte.io.iam.modules.resource.entity.SysResource;
import github.jiangbyte.io.iam.modules.resource.entity.SysResourceModule;
import github.jiangbyte.io.iam.modules.resource.param.SysResourceAddParam;
import github.jiangbyte.io.iam.modules.resource.param.SysResourceButtonAddParam;
import github.jiangbyte.io.iam.modules.resource.param.SysResourceButtonEditParam;
import github.jiangbyte.io.iam.modules.resource.param.SysResourceButtonPageParam;
import github.jiangbyte.io.iam.modules.resource.param.SysResourceEditParam;
import github.jiangbyte.io.iam.modules.resource.param.SysResourceModuleAddParam;
import github.jiangbyte.io.iam.modules.resource.param.SysResourceModuleEditParam;
import github.jiangbyte.io.iam.modules.resource.param.SysResourcePageParam;
import github.jiangbyte.io.iam.modules.resource.param.SysResourcePermissionBindParam;
import github.jiangbyte.io.iam.modules.resource.param.SysResourceTreeParam;
import github.jiangbyte.io.iam.modules.resource.result.SysResourceButtonResult;
import github.jiangbyte.io.iam.modules.resource.result.SysResourceGrantModuleOptionResult;
import cn.hutool.core.lang.tree.Tree;

import java.util.List;

/**
 * 管理端资源服务接口：菜单/按钮/模块 CRUD、树、当前菜单与权限绑定。
 *
 * Author: Charlie
 */
public interface ResourceService extends IService<SysResource> {

    /** 创建管理端资源。 */
    void create(SysResourceAddParam param);

    /** 更新管理端资源。 */
    void update(SysResourceEditParam param);

    /** 批量删除管理端资源。 */
    void delete(IdsParam param);

    /** 查询管理端资源详情。 */
    SysResource detail(String id);

    /** 分页查询管理端资源。 */
    Page<SysResource> page(SysResourcePageParam param);

    /** 构建管理端资源树。 */
    List<Tree<String>> tree(SysResourceTreeParam param);

    /** 当前登录账号可见的管理端菜单。 */
    List<SysResource> currentMenus();

    /** 列出门户公开资源。 */
    List<SysResource> listPublicPortalResources();

    /** 绑定管理端资源与权限。 */
    void bindPermission(SysResourcePermissionBindParam param);

    /** 创建按钮资源。 */
    void createButton(SysResourceButtonAddParam param);

    /** 更新按钮资源。 */
    void updateButton(SysResourceButtonEditParam param);

    /** 批量删除按钮资源。 */
    void deleteButtons(IdsParam param);

    /** 分页查询按钮资源。 */
    Page<SysResourceButtonResult> pageButtons(SysResourceButtonPageParam param);

    /** 创建资源模块。 */
    void createModule(SysResourceModuleAddParam param);

    /** 更新资源模块。 */
    void updateModule(SysResourceModuleEditParam param);

    /** 批量删除资源模块。 */
    void deleteModules(IdsParam param);

    /** 资源模块详情。 */
    SysResourceModule moduleDetail(String id);

    /** 分页查询资源模块。 */
    Page<SysResourceModule> pageModules(SysResourcePageParam param);

    /** 资源模块选择器列表。 */
    List<SysResourceModule> moduleSelector();

    /** 列出可授权的管理端模块选项。 */
    List<SysResourceGrantModuleOptionResult> listGrantModules(String moduleClient);

}
