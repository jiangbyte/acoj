package io.charlie.web.oj.modular.sys.menu.param;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import io.charlie.galaxy.pojo.CommonEntity;
import java.io.Serializable;
import java.time.LocalDateTime;

import io.charlie.web.oj.modular.sys.menu.entity.MenuExJsonItem;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import java.io.Serial;
import java.util.List;

/**
* @author Charlie Zhang
* @version v1.0
* @date 2025-09-24
* @description 菜单 增加参数
*/
@Data
@Schema(name = "SysMenu", description = "菜单 增加参数")
public class SysMenuAddParam implements Serializable {
    @Serial
    @TableField(exist = false)
    private static final long serialVersionUID = 1L;

    @Schema(description = "父菜单ID")
    private String pid;

    @Schema(description = "菜单名称（英文标识）")
    private String name;

    @Schema(description = "路由路径")
    private String path;

    @Schema(description = "组件路径")
    private String componentPath;

    @Schema(description = "菜单标题")
    private String title;

    @Schema(description = "图标")
    private String icon;

    @Schema(description = "头部参数")
    private String parameters;

    @Schema(description = "是否缓存")
    private Boolean keepAlive;

    @Schema(description = "是否可见")
    private Boolean visible;

    @Schema(description = "排序")
    private Integer sort;

    @Schema(description = "是否固定")
    private Boolean pined;

    @Schema(description = "菜单类型：0-目录，1-菜单")
    private Integer menuType;

    @Schema(description = "额外信息")
    private List<MenuExJsonItem> exJson;

}