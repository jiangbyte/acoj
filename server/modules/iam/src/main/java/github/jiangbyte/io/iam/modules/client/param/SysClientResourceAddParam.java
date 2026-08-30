package github.jiangbyte.io.iam.modules.client.param;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.Map;

/**
 * 创建客户端资源入参。
 *
 * Author: Charlie
 */
@Schema(description = "创建客户端资源入参。")
@Data
public class SysClientResourceAddParam {

    @NotBlank
    @Schema(description = "编码")
    private String code;
    @NotBlank
    @Schema(description = "名称")
    private String name;
    @NotBlank
    @Schema(description = "资源类型：MENU/BUTTON/API 等")
    private String resourceType;
    @Schema(description = "父级客户端资源ID")
    private String parentId;
    @Schema(description = "所属客户端模块ID")
    private String moduleId;
    @Schema(description = "前端路由路径")
    private String path;
    @Schema(description = "前端组件路径")
    private String component;
    @Schema(description = "路由重定向地址")
    private String redirect;
    @Schema(description = "图标标识")
    private String icon;
    @Schema(description = "颜色值")
    private String color;
    @Schema(description = "外链跳转地址")
    private String href;
    @Schema(description = "排序号（越小越靠前）")
    private Integer sort = 99;
    @Schema(description = "是否可见：1 可见 / 0 隐藏")
    private Boolean isVisible = true;
    @Schema(description = "是否缓存路由：1 缓存 / 0 不缓存")
    private Boolean isCache = false;
    @Schema(description = "是否固定标签页：1 固定 / 0 不固定")
    private Boolean isAffix = false;
    @Schema(description = "资源状态：ENABLED/DISABLED")
    private String status = "ENABLED";
    @Schema(description = "客户端资源描述")
    private String description;
    @Schema(description = "页面布局类型")
    private String layout;
    @Schema(description = "扩展信息（JSON）")
    private Map<String, Object> extra = Map.of();
}
