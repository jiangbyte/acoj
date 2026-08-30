package github.jiangbyte.io.biz.modules.cg_test_activity.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import github.jiangbyte.io.common.core.param.IdsParam;
import github.jiangbyte.io.biz.modules.cg_test_activity.entity.CgTestActivity;
import github.jiangbyte.io.biz.modules.cg_test_activity.param.CgTestActivityAddParam;
import github.jiangbyte.io.biz.modules.cg_test_activity.param.CgTestActivityEditParam;
import github.jiangbyte.io.biz.modules.cg_test_activity.param.CgTestActivityPageParam;

import java.util.List;

/**
 * CgTestActivity 服务接口：CRUD。
 *
 * Author: Charlie
 */
public interface CgTestActivityService extends IService<CgTestActivity> {

    /** 创建。 */
    void create(CgTestActivityAddParam param);

    /** 更新。 */
    void update(CgTestActivityEditParam param);

    /** 批量删除。 */
    void delete(IdsParam param);

    /** 查询详情。 */
    CgTestActivity detail(String id);

    /** 分页查询。 */
    Page<CgTestActivity> page(CgTestActivityPageParam param);
}
