package github.jiangbyte.io.iam.modules.resource.result;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 资源可绑定权限选项。
 *
 * Author: Charlie
 */
@Schema(description = "资源可绑定权限选项。")
@Data
public class SysResourcePermissionOptionResult {
    @Schema(description = "主键ID")

    private String id;
    @Schema(description = "权限键")
    private String permissionKey;
    @Schema(description = "标题")
    private String title;
    @Schema(description = "dataScope")
    private String dataScope;
}
