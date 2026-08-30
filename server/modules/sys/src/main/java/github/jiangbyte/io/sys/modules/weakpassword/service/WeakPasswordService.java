package github.jiangbyte.io.sys.modules.weakpassword.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import github.jiangbyte.io.common.core.param.IdsParam;
import github.jiangbyte.io.sys.modules.weakpassword.entity.SysWeakPassword;
import github.jiangbyte.io.sys.modules.weakpassword.param.SysWeakPasswordAddParam;
import github.jiangbyte.io.sys.modules.weakpassword.param.SysWeakPasswordEditParam;
import github.jiangbyte.io.sys.modules.weakpassword.param.SysWeakPasswordPageParam;

import java.util.List;

/**
 * 弱密码服务接口：CRUD 与是否弱密码校验。
 *
 * Author: Charlie
 */
public interface WeakPasswordService extends IService<SysWeakPassword> {

    /** 创建。 */
    void create(SysWeakPasswordAddParam param);

    /** 更新。 */
    void update(SysWeakPasswordEditParam param);

    /** 批量删除。 */
    void delete(IdsParam param);

    /** 查询详情。 */
    SysWeakPassword detail(String id);

    /** 分页查询。 */
    Page<SysWeakPassword> page(SysWeakPasswordPageParam param);

    /** 列表查询。 */
    List<SysWeakPassword> list(String password, String keyword);
}
