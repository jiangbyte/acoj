package github.jiangbyte.io.iam.modules.client.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import github.jiangbyte.io.common.core.param.IdsParam;
import github.jiangbyte.io.iam.modules.client.entity.SysClientResource;
import github.jiangbyte.io.iam.modules.client.param.SysClientResourceAddParam;
import github.jiangbyte.io.iam.modules.client.param.SysClientResourceEditParam;
import github.jiangbyte.io.iam.modules.client.param.SysClientResourcePageParam;
import github.jiangbyte.io.iam.modules.client.param.SysClientResourcePermissionBindParam;
import github.jiangbyte.io.iam.modules.client.param.SysClientResourceTreeParam;
import github.jiangbyte.io.iam.modules.resource.result.SysResourceGrantModuleOptionResult;
import cn.hutool.core.lang.tree.Tree;

import java.util.List;

/**
 * 客户端资源服务接口：CRUD、树、权限绑定与授权模块选项。
 *
 * Author: Charlie
 */
public interface ClientResourceService extends IService<SysClientResource> {

    /** 创建客户端资源。 */
    void create(SysClientResourceAddParam param);

    /** 更新客户端资源。 */
    void update(SysClientResourceEditParam param);

    /** 批量删除客户端资源。 */
    void delete(IdsParam param);

    /** 查询客户端资源详情。 */
    SysClientResource detail(String id);

    /** 分页查询客户端资源。 */
    Page<SysClientResource> page(SysClientResourcePageParam param);

    /** 构建客户端资源树。 */
    List<Tree<String>> tree(SysClientResourceTreeParam param);

    /** 绑定客户端资源与权限关系。 */
    void bindPermission(SysClientResourcePermissionBindParam param);

    /** 列出可授权的客户端模块选项。 */
    List<SysResourceGrantModuleOptionResult> listGrantModules(String accountType);
}
