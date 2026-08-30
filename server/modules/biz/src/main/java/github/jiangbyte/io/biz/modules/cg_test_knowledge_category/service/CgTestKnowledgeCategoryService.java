package github.jiangbyte.io.biz.modules.cg_test_knowledge_category.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import github.jiangbyte.io.common.core.param.IdsParam;
import github.jiangbyte.io.biz.modules.cg_test_knowledge_category.entity.CgTestKnowledgeCategory;
import github.jiangbyte.io.biz.modules.cg_test_knowledge_category.param.CgTestKnowledgeCategoryAddParam;
import github.jiangbyte.io.biz.modules.cg_test_knowledge_category.param.CgTestKnowledgeCategoryEditParam;
import github.jiangbyte.io.biz.modules.cg_test_knowledge_category.param.CgTestKnowledgeCategoryPageParam;
import github.jiangbyte.io.biz.modules.cg_test_knowledge_category.entity.CgTestKnowledgeDoc;
import github.jiangbyte.io.biz.modules.cg_test_knowledge_category.param.CgTestKnowledgeDocAddParam;
import github.jiangbyte.io.biz.modules.cg_test_knowledge_category.param.CgTestKnowledgeDocEditParam;
import github.jiangbyte.io.biz.modules.cg_test_knowledge_category.param.CgTestKnowledgeDocPageParam;
import cn.hutool.core.lang.tree.Tree;

import java.util.List;

/**
 * CgTestKnowledgeCategory 服务接口：CRUD与树查询与子实体维护。
 *
 * Author: Charlie
 */
public interface CgTestKnowledgeCategoryService extends IService<CgTestKnowledgeCategory> {

    /** 创建。 */
    void create(CgTestKnowledgeCategoryAddParam param);

    /** 更新。 */
    void update(CgTestKnowledgeCategoryEditParam param);

    /** 批量删除。 */
    void delete(IdsParam param);

    /** 查询详情。 */
    CgTestKnowledgeCategory detail(String id);

    /** 分页查询。 */
    Page<CgTestKnowledgeCategory> page(CgTestKnowledgeCategoryPageParam param);

    /** 树形查询。 */
    List<Tree<String>> tree(String keyword);

    /** 创建子实体。 */
    void childCreate(CgTestKnowledgeDocAddParam param);

    /** 更新子实体。 */
    void childUpdate(CgTestKnowledgeDocEditParam param);

    /** 删除子实体。 */
    void childDelete(IdsParam param);

    /** 查询子实体详情。 */
    CgTestKnowledgeDoc childDetail(String id);

    /** 分页查询子实体。 */
    Page<CgTestKnowledgeDoc> childPage(CgTestKnowledgeDocPageParam param);
}
