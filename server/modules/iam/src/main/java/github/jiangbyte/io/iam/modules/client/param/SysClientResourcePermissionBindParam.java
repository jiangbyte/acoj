package github.jiangbyte.io.iam.modules.client.param;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * 客户端资源绑定权限入参（权限键、数据范围等）。
 *
 * Author: Charlie
 */
@Schema(description = "客户端资源绑定权限入参（权限键、数据范围等）。")
@Data
public class SysClientResourcePermissionBindParam {

    @NotBlank
    @Schema(description = "resourceId")
    private String resourceId;
    @NotBlank
    @Schema(description = "权限键")
    private String permissionKey;
    @Schema(description = "账户类型：ADMIN（管理端）/ PORTAL（门户端）")
    private String accountType = "ADMIN";
    @Schema(description = "dataScope")
    private String dataScope = "SELF";
    @Schema(description = "自定义数据范围部门ID列表（JSON 数组）")
    private List<String> customScopeDeptIds = new ArrayList<>();
    @Schema(description = "排序号（越小越靠前）")
    private Integer sort = 99;
    @Schema(description = "描述说明")
    private String description;
}
