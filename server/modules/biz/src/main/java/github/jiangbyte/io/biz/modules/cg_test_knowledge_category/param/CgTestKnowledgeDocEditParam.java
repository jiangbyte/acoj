package github.jiangbyte.io.biz.modules.cg_test_knowledge_category.param;

/**
 * 编辑知识文档入参。
 *
 * Author: Charlie
 */

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import java.time.OffsetDateTime;
import java.util.Map;
import jakarta.validation.constraints.NotNull;

@Schema(description = "CgTestKnowledgeDoc编辑入参")
@Data
public class CgTestKnowledgeDocEditParam {

    @NotBlank
    @Size(max = 64)
    @Schema(description = "主键ID")
    private String id;
    @NotBlank
    @Schema(description = "分类ID")
    private String categoryId;
    @NotBlank
    @Schema(description = "编码")
    private String code;
    @NotBlank
    @Schema(description = "标题")
    private String title;
    @NotBlank
    @Schema(description = "类型")
    private String type;
    @NotBlank
    @Schema(description = "状态")
    private String status;
    @NotBlank
    @Schema(description = "摘要")
    private String summary;
    @NotBlank
    @Schema(description = "内容")
    private String content;
    @NotBlank
    @Schema(description = "作者")
    private String author;
    @Schema(description = "发布时间")
    private OffsetDateTime publishedAt;
    @NotNull
    @Schema(description = "浏览/查看次数")
    private Integer viewCount;
    @NotNull
    @Schema(description = "排序号（越小越靠前）")
    private Integer sort;
    @NotNull
    @Schema(description = "是否置顶：1 置顶 / 0 不置顶")
    private Boolean isTop;
    @Schema(description = "展示设置（JSON）")
    private java.util.Map<String, Object> settings;
    @Schema(description = "扩展信息（JSON）")
    private java.util.Map<String, Object> extra;
}
