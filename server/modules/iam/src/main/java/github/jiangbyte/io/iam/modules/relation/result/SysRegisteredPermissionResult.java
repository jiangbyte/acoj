package github.jiangbyte.io.iam.modules.relation.result;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 已注册权限结果：权限键、名称、模块/资源编码与动作。
 *
 * Author: Charlie
 */
@Schema(description = "已注册权限结果：权限键、名称、模块/资源编码与动作。")
public record SysRegisteredPermissionResult(
        @Schema(description = "权限键")
        String permissionKey,
        @Schema(description = "名称")
        String name,
        @Schema(description = "模块编码")
        String moduleCode,
        @Schema(description = "资源编码")
        String resourceCode,
        @Schema(description = "动作")
        String action
) {
}
