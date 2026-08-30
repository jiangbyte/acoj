package github.jiangbyte.io.iam.modules.account.param;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * 账号授权角色入参：账号 id + 角色 id 列表。
 *
 * Author: Charlie
 */
@Schema(description = "账号授权角色入参：账号 id + 角色 id 列表。")
@Data
public class SysAccountGrantRoleParam {

    @NotBlank
    @Schema(description = "主键ID")
    private String id;
    @Schema(description = "roleIds")
    private List<String> roleIds = new ArrayList<>();
}
