package github.jiangbyte.io.iam.modules.client.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import github.jiangbyte.io.common.core.param.IdsParam;
import github.jiangbyte.io.iam.modules.client.entity.SysClientModule;
import github.jiangbyte.io.iam.modules.client.param.SysClientModuleAddParam;
import github.jiangbyte.io.iam.modules.client.param.SysClientModuleEditParam;
import github.jiangbyte.io.iam.modules.client.param.SysClientModulePageParam;

import java.util.List;

/**
 * 客户端模块服务接口：CRUD、分页与按账号类型选择器。
 *
 * Author: Charlie
 */
public interface ClientModuleService extends IService<SysClientModule> {

    /** 创建客户端模块。 */
    void create(SysClientModuleAddParam param);

    /** 更新客户端模块。 */
    void update(SysClientModuleEditParam param);

    /** 批量删除客户端模块。 */
    void delete(IdsParam param);

    /** 查询客户端模块详情。 */
    SysClientModule detail(String id);

    /** 分页查询客户端模块。 */
    Page<SysClientModule> page(SysClientModulePageParam param);

    /** 按账号类型返回模块选择器列表。 */
    List<SysClientModule> selector(String accountType);
}
