package github.jiangbyte.io.biz.modules.cg_test_order.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import github.jiangbyte.io.common.core.param.IdsParam;
import github.jiangbyte.io.biz.modules.cg_test_order.entity.CgTestOrder;
import github.jiangbyte.io.biz.modules.cg_test_order.param.CgTestOrderAddParam;
import github.jiangbyte.io.biz.modules.cg_test_order.param.CgTestOrderEditParam;
import github.jiangbyte.io.biz.modules.cg_test_order.param.CgTestOrderPageParam;
import github.jiangbyte.io.biz.modules.cg_test_order.entity.CgTestOrderItem;
import github.jiangbyte.io.biz.modules.cg_test_order.param.CgTestOrderItemAddParam;
import github.jiangbyte.io.biz.modules.cg_test_order.param.CgTestOrderItemEditParam;
import github.jiangbyte.io.biz.modules.cg_test_order.param.CgTestOrderItemPageParam;

import java.util.List;

/**
 * CgTestOrder 服务接口：CRUD与子实体维护。
 *
 * Author: Charlie
 */
public interface CgTestOrderService extends IService<CgTestOrder> {

    /** 创建。 */
    void create(CgTestOrderAddParam param);

    /** 更新。 */
    void update(CgTestOrderEditParam param);

    /** 批量删除。 */
    void delete(IdsParam param);

    /** 查询详情。 */
    CgTestOrder detail(String id);

    /** 分页查询。 */
    Page<CgTestOrder> page(CgTestOrderPageParam param);

    /** 创建子实体。 */
    void childCreate(CgTestOrderItemAddParam param);

    /** 更新子实体。 */
    void childUpdate(CgTestOrderItemEditParam param);

    /** 删除子实体。 */
    void childDelete(IdsParam param);

    /** 查询子实体详情。 */
    CgTestOrderItem childDetail(String id);

    /** 分页查询子实体。 */
    Page<CgTestOrderItem> childPage(CgTestOrderItemPageParam param);
}
