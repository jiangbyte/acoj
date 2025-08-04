package io.charlie.app.core.modular.relation.entity;

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
    private static final long serialVersionUID = 1L;

    @Schema(description = "用户ID")
    private Long userId;

    @Schema(description = "角色ID")
    private Long roleId;
}
