package github.jiangbyte.io.biz.modules.cg_test_knowledge_category.convert;

/**
 * 知识文档 MapStruct 转换：入参与实体映射。
 *
 * Author: Charlie
 */

import github.jiangbyte.io.biz.modules.cg_test_knowledge_category.entity.CgTestKnowledgeDoc;
import github.jiangbyte.io.biz.modules.cg_test_knowledge_category.param.CgTestKnowledgeDocAddParam;
import github.jiangbyte.io.biz.modules.cg_test_knowledge_category.param.CgTestKnowledgeDocEditParam;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface CgTestKnowledgeDocConvert {

    /** 新增入参转实体。 */
    CgTestKnowledgeDoc toEntity(CgTestKnowledgeDocAddParam param);

    /** 编辑入参更新到实体。 */
    void update(CgTestKnowledgeDocEditParam param, @MappingTarget CgTestKnowledgeDoc entity);
}
