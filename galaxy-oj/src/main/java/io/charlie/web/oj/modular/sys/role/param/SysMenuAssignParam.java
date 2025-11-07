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
* @date 2025-09-24
* @description 菜单
*/
@Data
@Schema(name = "SysMenu", description = "菜单 ID参数")
public class SysMenuAssignParam implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @NotBlank(message = "id不能为空")
    private String roleId;

    private List<String> menuIds;
}