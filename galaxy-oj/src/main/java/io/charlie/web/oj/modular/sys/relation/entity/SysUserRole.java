package io.charlie.web.oj.modular.sys.relation.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
* @author Charlie Zhang
* @version v1.0
* @date 2025-07-14
* @description 用户-角色 关联表(1-N)
*/
@Data
@TableName("sys_user_role")
@Schema(name = "SysUserRole", description = "用户-角色 关联表(1-N)")
public class SysUserRole implements Serializable {
    @Serial
    @TableField(exist = false)
    private static final long serialVersionUID = 1L;

    @Schema(description = "菜单ID")
    private String id;

    @Schema(description = "用户ID")
    private String userId;

    @Schema(description = "角色ID")
    private String roleId;
}
