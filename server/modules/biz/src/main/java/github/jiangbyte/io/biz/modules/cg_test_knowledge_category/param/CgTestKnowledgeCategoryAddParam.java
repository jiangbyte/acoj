package github.jiangbyte.io.biz.modules.cg_test_knowledge_category.param;

/**
 * 创建KnowledgeCategory入参。
 *
 * Author: Charlie
 */

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import java.util.Map;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Schema(description = "CgTestKnowledgeCategory创建入参")
@Data
public class CgTestKnowledgeCategoryAddParam {
    @NotBlank
    @Schema(description = "父级ID")
    private String parentId;
    @NotBlank
    @Schema(description = "编码")
    private String code;
    @NotBlank
    @Schema(description = "名称")
    private String name;
    @NotBlank
    @Schema(description = "状态")
    private String status;
    @NotNull
    @Schema(description = "排序号（越小越靠前）")
    private Integer sort;
    @NotNull
    @Schema(description = "是否可见：1 可见 / 0 隐藏")
    private Boolean isVisible;
    @NotBlank
    @Schema(description = "描述说明")
    private String description;
    @Schema(description = "扩展信息（JSON）")
    private java.util.Map<String, Object> extra;
}
