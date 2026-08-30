package github.jiangbyte.io.iam.modules.client.entity;

import io.swagger.v3.oas.annotations.media.Schema;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import github.jiangbyte.io.common.core.domain.BaseEntity;
import github.jiangbyte.io.common.mybatis.handler.JacksonJsonTypeHandler;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.dromara.core.trans.anno.Trans;
import org.dromara.core.trans.constant.TransType;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 客户端资源（菜单/按钮等）实体，对应表 sys_client_resource。
 *
 * Author: Charlie
 */
@Schema(description = "客户端资源（菜单/按钮等）实体，对应表 sys_client_resource。")
@Data
@EqualsAndHashCode(callSuper = true)
@TableName(value = "sys_client_resource", autoResultMap = true)
public class SysClientResource extends BaseEntity {
    @Trans(
            type = TransType.SIMPLE,
            target = SysClientResource.class,
            fields = "name",
            ref = "parentIdName")
    @Schema(description = "父级客户端资源ID")
    private String parentId;
    @Schema(description = "编码")
    private String code;
    @Schema(description = "名称")
    private String name;
    @Schema(description = "资源类型：MENU/BUTTON/API 等")
    private String resourceType;
    @Trans(
            type = TransType.SIMPLE,
            target = SysClientModule.class,
            fields = "name",
            ref = "moduleIdName")
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
    private Integer sort;
    @Schema(description = "是否可见：1 可见 / 0 隐藏")
    private Boolean isVisible;
    @Schema(description = "是否缓存路由：1 缓存 / 0 不缓存")
    private Boolean isCache;
    @Schema(description = "是否固定标签页：1 固定 / 0 不固定")
    private Boolean isAffix;
    @Schema(description = "资源状态：ENABLED/DISABLED")
    private String status;
    @Schema(description = "客户端资源描述")
    private String description;
    @Schema(description = "页面布局类型")
    private String layout;
    @TableField(typeHandler = JacksonJsonTypeHandler.class)
    @Schema(description = "扩展信息（JSON）")
    private Map<String, Object> extra;

    @TableField(exist = false)
    @Schema(description = "父级名称（展示）")
    private String parentIdName;

    @TableField(exist = false)
    @Schema(description = "模块名称（展示）")
    private String moduleIdName;

    @TableField(exist = false)
    @Schema(description = "账户类型：ADMIN（管理端）/ PORTAL（门户端）")
    private String accountType;

    @TableField(exist = false)
    @Schema(description = "子节点列表")
    private List<SysClientResource> children = new ArrayList<>();
}
