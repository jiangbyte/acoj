package github.jiangbyte.io.sys.modules.job.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import github.jiangbyte.io.common.core.param.IdsParam;
import github.jiangbyte.io.sys.modules.job.entity.SysJob;
import github.jiangbyte.io.sys.modules.job.param.SysJobAddParam;
import github.jiangbyte.io.sys.modules.job.param.SysJobEditParam;
import github.jiangbyte.io.sys.modules.job.param.SysJobEnabledParam;
import github.jiangbyte.io.sys.modules.job.param.SysJobPageParam;

/**
 * 任务服务接口：CRUD、启停与立即执行。
 *
 * Author: Charlie
 */
public interface JobService extends IService<SysJob> {

    /** 创建。 */
    void create(SysJobAddParam param);

    /** 更新。 */
    void update(SysJobEditParam param);

    /** 批量删除。 */
    void delete(IdsParam param);

    /** 查询详情。 */
    SysJob detail(String id);

    /** 分页查询。 */
    Page<SysJob> page(SysJobPageParam param);

    /** 启停。 */
    void updateEnabled(SysJobEnabledParam param);

    /** 立即执行（异步，结果写入执行日志）。 */
    void runNow(String id);
}
