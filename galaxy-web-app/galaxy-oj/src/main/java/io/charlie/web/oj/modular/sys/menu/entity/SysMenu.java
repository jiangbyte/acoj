package io.charlie.web.oj.modular.sys.menu.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import io.charlie.galaxy.pojo.CommonEntity;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.io.Serial;
import java.util.Date;
import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.dromara.core.trans.anno.Trans;
import org.dromara.core.trans.constant.TransType;

/**
* @author Charlie Zhang
* @version v1.0
* @date 2025-09-24
* @description 菜单表
*/
@EqualsAndHashCode(callSuper = true)
@Data
@TableName(value = "sys_menu", autoResultMap = true)
@Schema(name = "SysMenu", description = "菜单表")
public class SysMenu extends CommonEntity {
    @Serial
    @TableField(exist = false)
    private static final long serialVersionUID = 1L;

    @Schema(description = "菜单ID")
    private String id;

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

    @Schema(description = "是否缓存")
    @Trans(type = TransType.DICTIONARY, key = "YES_NO")
    private Boolean keepAlive;

    @Schema(description = "是否可见")
    @Trans(type = TransType.DICTIONARY, key = "YES_NO")
    private Boolean visible;

    @Schema(description = "排序")
    private Integer sort;

    @Schema(description = "是否固定")
    @Trans(type = TransType.DICTIONARY, key = "YES_NO")
    private Boolean pined;

    @Schema(description = "菜单类型：0-目录，1-菜单")
    @Trans(type = TransType.DICTIONARY, key = "MENU_TYPE")
    private Integer menuType;

    @Schema(description = "额外信息")
    @TableField(typeHandler = JacksonTypeHandler.class)
    private List<MenuExJsonItem> exJson;

    @Schema(description = "子菜单")
    @TableField(exist = false)
    private List<SysMenu> children;
}
