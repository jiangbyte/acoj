package github.jiangbyte.io.iam.modules.position.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import github.jiangbyte.io.common.core.param.IdsParam;
import github.jiangbyte.io.iam.modules.position.entity.SysPosition;
import github.jiangbyte.io.iam.modules.position.param.SysPositionAddParam;
import github.jiangbyte.io.iam.modules.position.param.SysPositionEditParam;
import github.jiangbyte.io.iam.modules.position.param.SysPositionPageParam;

/**
 * 岗位服务接口：CRUD 与分页。
 *
 * Author: Charlie
 */
public interface PositionService extends IService<SysPosition> {

    /** 创建岗位。 */
    void create(SysPositionAddParam param);

    /** 更新岗位。 */
    void update(SysPositionEditParam param);

    /** 批量删除岗位。 */
    void delete(IdsParam param);

    /** 查询岗位详情。 */
    SysPosition detail(String id);

    /** 分页查询岗位。 */
    Page<SysPosition> page(SysPositionPageParam param);
}
