package github.jiangbyte.io.iam.modules.resource.entity;

import io.swagger.v3.oas.annotations.media.Schema;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import github.jiangbyte.io.common.core.domain.BaseEntity;
import github.jiangbyte.io.common.mybatis.handler.JacksonJsonTypeHandler;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.dromara.core.trans.anno.Trans;
import org.dromara.core.trans.constant.TransType;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 管理端资源（菜单/按钮等）实体，对应表 sys_resource。
 *
 * Author: Charlie
 */
@Schema(description = "管理端资源（菜单/按钮等）实体，对应表 sys_resource。")
@Data
@EqualsAndHashCode(callSuper = true)
@TableName(value = "sys_resource", autoResultMap = true)
public class SysResource extends BaseEntity {
    @Trans(
            type = TransType.SIMPLE,
            target = SysResource.class,
            fields = "name",
            ref = "parentIdName")
    @Schema(description = "父级资源ID（菜单树）")
    private String parentId;
    @Schema(description = "编码")
    private String code;
    @Schema(description = "名称")
    private String name;
    @Schema(description = "资源类型：MENU/BUTTON/API 等")
    private String resourceType;
    @Trans(
            type = TransType.SIMPLE,
            target = SysResourceModule.class,
            fields = {"name", "client"},
            refs = {"moduleIdName", "moduleClient"})
    @Schema(description = "所属资源模块ID")
    private String moduleId;
    @Schema(description = "路径")
    private String path;
    @Schema(description = "前端路由组件路径")
    private String component;
    @Schema(description = "路由重定向地址")
    private String redirect;
    @Schema(description = "图标标识")
    private String icon;
    @Schema(description = "颜色值")
    private String color;
    @Schema(description = "外链地址")
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
    @Schema(description = "资源描述说明")
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
    @Schema(description = "模块所属客户端（展示）")
    private String moduleClient;

    @Schema(description = "仅 tree 接口使用；current 等扁平列表不输出空 children。")
    /**
     * 仅 tree 接口使用；current 等扁平列表不输出空 children。
     */
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    @TableField(exist = false)
    private List<SysResource> children = new ArrayList<>();
}
