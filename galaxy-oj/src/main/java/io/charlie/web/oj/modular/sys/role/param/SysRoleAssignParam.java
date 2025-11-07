package io.charlie.web.oj.modular.sys.role.param;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

/**
 * @author Charlie Zhang
 * @version v1.0
 * @date 2025-09-20
 * @description 角色
 */
@Data
@Schema(name = "SysRole", description = "角色 ID参数")
public class SysRoleAssignParam implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @NotBlank(message = "用户ID不能为空")
    private String userId;

    @NotEmpty(message = "角色ID不能为空")
    private List<String> roleIds;
}