package github.jiangbyte.io.biz.modules.cg_test_catalog.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import github.jiangbyte.io.common.core.param.IdsParam;
import github.jiangbyte.io.biz.modules.cg_test_catalog.entity.CgTestCatalog;
import github.jiangbyte.io.biz.modules.cg_test_catalog.param.CgTestCatalogAddParam;
import github.jiangbyte.io.biz.modules.cg_test_catalog.param.CgTestCatalogEditParam;
import github.jiangbyte.io.biz.modules.cg_test_catalog.param.CgTestCatalogPageParam;
import cn.hutool.core.lang.tree.Tree;

import java.util.List;

/**
 * CgTestCatalog 服务接口：CRUD与树查询。
 *
 * Author: Charlie
 */
public interface CgTestCatalogService extends IService<CgTestCatalog> {

    /** 创建。 */
    void create(CgTestCatalogAddParam param);

    /** 更新。 */
    void update(CgTestCatalogEditParam param);

    /** 批量删除。 */
    void delete(IdsParam param);

    /** 查询详情。 */
    CgTestCatalog detail(String id);

    /** 分页查询。 */
    Page<CgTestCatalog> page(CgTestCatalogPageParam param);

    /** 树形查询。 */
    List<Tree<String>> tree(String keyword);
}
