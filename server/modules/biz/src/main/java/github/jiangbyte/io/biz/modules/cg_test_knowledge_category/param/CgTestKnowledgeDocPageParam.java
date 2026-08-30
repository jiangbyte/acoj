package github.jiangbyte.io.biz.modules.cg_test_knowledge_category.param;

/**
 * 知识文档分页查询入参。
 *
 * Author: Charlie
 */

import io.swagger.v3.oas.annotations.media.Schema;
import github.jiangbyte.io.common.core.domain.PageQuery;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Schema(description = "CgTestKnowledgeDoc分页查询入参")
@Data
@EqualsAndHashCode(callSuper = true)
public class CgTestKnowledgeDocPageParam extends PageQuery {
    @Schema(description = "分类ID")
    private String categoryId;
    @Schema(description = "编码")
    private String code;
    @Schema(description = "标题")
    private String title;
    @Schema(description = "类型")
    private String type;
    @Schema(description = "状态")
    private String status;
}
