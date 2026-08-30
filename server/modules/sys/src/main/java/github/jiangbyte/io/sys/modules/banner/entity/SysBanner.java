package github.jiangbyte.io.sys.modules.banner.entity;

import io.swagger.v3.oas.annotations.media.Schema;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import github.jiangbyte.io.common.core.domain.BaseEntity;
import github.jiangbyte.io.common.mybatis.handler.JacksonJsonTypeHandler;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.OffsetDateTime;
import java.util.List;

/**
 * Banner 实体，对应表 sys_banner。
 *
 * Author: Charlie
 */
@Schema(description = "Banner 实体，对应表 sys_banner。")
@Data
@EqualsAndHashCode(callSuper = true)
@TableName(value = "sys_banner", autoResultMap = true)
public class SysBanner extends BaseEntity {
    @Schema(description = "标题")
    private String title;
    @Schema(description = "Banner 图片 object_name（由服务层解析访问 URL）")
    private String image;

    @TableField(exist = false)
    @Schema(description = "imageUrl")
    private String imageUrl;
    @Schema(description = "点击跳转链接地址")
    private String url;
    @Schema(description = "链接类型（字典 BANNER_LINK_TYPE）")
    private String linkType;
    @Schema(description = "摘要")
    private String summary;
    @Schema(description = "描述说明")
    private String description;
    @Schema(description = "Banner 分类（字典 BANNER_CATEGORY）")
    private String category;
    @Schema(description = "Banner 类型（字典 BANNER_TYPE）")
    private String type;
    @Schema(description = "展示位置（字典 BANNER_POSITION）")
    private String position;
    @TableField(typeHandler = JacksonJsonTypeHandler.class)
    @Schema(description = "可见账户类型：ADMIN/PORTAL（JSON 数组）")
    private List<String> targetAccountTypes;
    @Schema(description = "排序号（越小越靠前）")
    private Integer sort;
    @Schema(description = "用户交互次数统计")
    private Long interactionCount;
    @Schema(description = "Banner 状态：ENABLED/DISABLED 等")
    private String status;
    @Schema(description = "开始展示时间")
    private OffsetDateTime startAt;
    @Schema(description = "结束展示时间")
    private OffsetDateTime endAt;
}
