package github.jiangbyte.io.biz.modules.cg_test_knowledge_category.entity;

/**
 * 知识文档实体，对应表 {@code cg_test_knowledge_doc}。
 *
 * Author: Charlie
 */

import io.swagger.v3.oas.annotations.media.Schema;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.TableField;
import github.jiangbyte.io.common.mybatis.handler.JacksonJsonTypeHandler;
import github.jiangbyte.io.common.core.domain.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import java.time.OffsetDateTime;
import java.util.Map;

@Schema(description = "代码生成测试-知识文档")
@Data
@EqualsAndHashCode(callSuper = true)
@TableName(value = "cg_test_knowledge_doc", autoResultMap = true)
public class CgTestKnowledgeDoc extends BaseEntity {
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
    @Schema(description = "摘要")
    private String summary;
    @Schema(description = "内容")
    private String content;
    @Schema(description = "作者")
    private String author;
    @Schema(description = "发布时间")
    private OffsetDateTime publishedAt;
    @Schema(description = "浏览/查看次数")
    private Integer viewCount;
    @Schema(description = "排序号（越小越靠前）")
    private Integer sort;
    @Schema(description = "是否置顶：1 置顶 / 0 不置顶")
    private Boolean isTop;
    @TableField(typeHandler = JacksonJsonTypeHandler.class)
    @Schema(description = "展示设置（JSON）")
    private java.util.Map<String, Object> settings;
    @TableField(typeHandler = JacksonJsonTypeHandler.class)
    @Schema(description = "扩展信息（JSON）")
    private java.util.Map<String, Object> extra;
}
