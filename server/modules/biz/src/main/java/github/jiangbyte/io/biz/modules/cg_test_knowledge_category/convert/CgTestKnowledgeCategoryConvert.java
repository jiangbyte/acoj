package github.jiangbyte.io.biz.modules.cg_test_knowledge_category.convert;

/**
 * KnowledgeCategory MapStruct 转换：入参与实体映射。
 *
 * Author: Charlie
 */

import github.jiangbyte.io.biz.modules.cg_test_knowledge_category.entity.CgTestKnowledgeCategory;
import github.jiangbyte.io.biz.modules.cg_test_knowledge_category.param.CgTestKnowledgeCategoryAddParam;
import github.jiangbyte.io.biz.modules.cg_test_knowledge_category.param.CgTestKnowledgeCategoryEditParam;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface CgTestKnowledgeCategoryConvert {

    /** 新增入参转实体。 */
    CgTestKnowledgeCategory toEntity(CgTestKnowledgeCategoryAddParam param);

    /** 编辑入参更新到实体。 */
    void update(CgTestKnowledgeCategoryEditParam param, @MappingTarget CgTestKnowledgeCategory entity);
}
