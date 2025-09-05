package io.charlie.app.core.modular.sys.group.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import io.charlie.app.core.modular.sys.user.entity.SysUser;
import io.charlie.galaxy.pojo.CommonEntity;

import java.io.Serial;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.dromara.core.trans.anno.Trans;
import org.dromara.core.trans.constant.TransType;

/**
* @author Charlie Zhang
* @version v1.0
* @date 2025-07-25
* @description 用户组表
*/
@EqualsAndHashCode(callSuper = true)
@Data
@TableName("sys_group")
@Schema(name = "SysGroup", description = "用户组表")
public class SysGroup extends CommonEntity {
    @Serial
    @TableField(exist = false)
    private static final long serialVersionUID = 1L;

    @Schema(description = "用户组")
    private String id;

    @Schema(description = "父级用户组")
    @Trans(type = TransType.SIMPLE, target = SysGroup.class, fields = "name", ref = "parentIdName")
    private String parentId;

    @Schema(description = "用户组名称")
    @TableField(exist = false)
    private String parentIdName;

    @Schema(description = "名称")
    private String name;

    @Schema(description = "编码")
    private String code;

    @Schema(description = "描述")
    private String description;

    @Schema(description = "排序")
    private Integer sort;

    @Schema(description = "负责人")
    @Trans(type = TransType.SIMPLE, target = SysUser.class, fields = "nickname", ref = "adminIdName")
    private String adminId;

    @Schema(description = "负责人名称")
    @TableField(exist = false)
    private String adminIdName;

    @Schema(description = "系统组")
    @Trans(type = TransType.DICTIONARY, key = "YES_NO", ref = "groupTypeName")
    private Boolean groupType;

    @Schema(description = "系统组名称")
    @TableField(exist = false)
    private String groupTypeName;
}
