package io.charlie.app.core.modular.sys.article.param;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import io.charlie.galaxy.pojo.CommonEntity;
import java.io.Serializable;
import java.time.LocalDateTime;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import java.io.Serial;

/**
* @author Charlie Zhang
* @version v1.0
* @date 2025-07-27
* @description 系统文章 编辑参数
*/
@Data
@Schema(name = "SysArticle", description = "系统文章 编辑参数")
public class SysArticleEditParam implements Serializable {
    @Serial
    @TableField(exist = false)
    private static final long serialVersionUID = 1L;

    @Schema(description = "主键")
    private String id;

    @Schema(description = "标题")
    private String title;

    @Schema(description = "子标题")
    private String subtitle;

    @Schema(description = "封面")
    private String cover;

    @Schema(description = "作者")
    private String author;

    @Schema(description = "摘要")
    private String summary;

    @Schema(description = "排序")
    private Integer sort;

    @Schema(description = "链接")
    private String toUrl;

    @Schema(description = "父级")
    private String parentId;

    @Schema(description = "类型")
    private String type;

    @Schema(description = "分类")
    private String category;

    @Schema(description = "内容")
    private String content;

}