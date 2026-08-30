package github.jiangbyte.io.iam.modules.resource.result;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 管理端按钮资源结果 DTO。
 *
 * Author: Charlie
 */
@Schema(description = "管理端按钮资源结果 DTO。")
@Data
public class SysResourceButtonResult {
    @Schema(description = "主键ID")

    private String id;
    @Schema(description = "父级ID")
    private String parentId;
    @Schema(description = "父级名称（展示）")
    private String parentIdName;
    @Schema(description = "编码")
    private String code;
    @Schema(description = "名称")
    private String name;
    @Schema(description = "resourceType")
    private String resourceType;
    @Schema(description = "所属模块ID")
    private String moduleId;
    @Schema(description = "模块名称（展示）")
    private String moduleIdName;
    @Schema(description = "模块所属客户端（展示）")
    private String moduleClient;
    @Schema(description = "路径")
    private String path;
    @Schema(description = "component")
    private String component;
    @Schema(description = "redirect")
    private String redirect;
    @Schema(description = "图标标识")
    private String icon;
    @Schema(description = "颜色值")
    private String color;
    @Schema(description = "href")
    private String href;
    @Schema(description = "排序号（越小越靠前）")
    private Integer sort;
    @Schema(description = "是否可见：1 可见 / 0 隐藏")
    private Boolean isVisible;
    @Schema(description = "是否缓存路由：1 缓存 / 0 不缓存")
    private Boolean isCache;
    @Schema(description = "是否固定标签页：1 固定 / 0 不固定")
    private Boolean isAffix;
    @Schema(description = "状态")
    private String status;
    @Schema(description = "描述说明")
    private String description;
    @Schema(description = "layout")
    private String layout;
    @Schema(description = "扩展信息（JSON）")
    private Map<String, Object> extra;
    @Schema(description = "创建时间")
    private OffsetDateTime createdAt;
    @Schema(description = "创建人（账户ID）")
    private String createdBy;
    @Schema(description = "更新时间")
    private OffsetDateTime updatedAt;
    @Schema(description = "更新人（账户ID）")
    private String updatedBy;
    @Schema(description = "permissionRelId")

    private String permissionRelId;
    @Schema(description = "权限键")
    private String permissionKey;
    @Schema(description = "dataScope")
    private String dataScope;
    @Schema(description = "自定义数据范围部门ID列表（JSON 数组）")
    private List<String> customScopeDeptIds = new ArrayList<>();
    @Schema(description = "permissionDescription")
    private String permissionDescription;
}
