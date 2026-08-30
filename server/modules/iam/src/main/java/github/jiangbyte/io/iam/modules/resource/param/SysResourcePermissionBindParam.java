package github.jiangbyte.io.iam.modules.resource.param;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * 管理端资源绑定权限入参。
 *
 * Author: Charlie
 */
@Schema(description = "管理端资源绑定权限入参。")
@Data
public class SysResourcePermissionBindParam {

    @NotBlank
    @Schema(description = "resourceId")
    private String resourceId;
    @NotBlank
    @Schema(description = "权限键")
    private String permissionKey;
    @Schema(description = "账户类型：ADMIN（管理端）/ PORTAL（门户端）")
    private String accountType = "ADMIN";
    @Schema(description = "dataScope")
    private String dataScope = "ALL";
    @Schema(description = "自定义数据范围部门ID列表（JSON 数组）")
    private List<String> customScopeDeptIds = new ArrayList<>();
    @Schema(description = "排序号（越小越靠前）")
    private Integer sort = 99;
    @Schema(description = "描述说明")
    private String description;
}
