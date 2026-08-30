package github.jiangbyte.io.iam.modules.role.param;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * 角色成员授权入参（账号 id 列表）。
 *
 * Author: Charlie
 */
@Schema(description = "角色成员授权入参（账号 id 列表）。")
@Data
public class SysRoleGrantUserParam {

    @NotBlank
    @Schema(description = "主键ID")
    private String id;
    @Schema(description = "accountIds")
    private List<String> accountIds = new ArrayList<>();
}
