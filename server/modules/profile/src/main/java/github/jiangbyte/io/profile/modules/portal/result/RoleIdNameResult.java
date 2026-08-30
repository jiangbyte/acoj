package github.jiangbyte.io.profile.modules.portal.result;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 角色 ID-名称项（由 entity 查询后填充，非 easy-trans Result）。
 *
 * Author: Charlie
 */
@Schema(description = "角色 ID-名称项（由 entity 查询后填充，非 easy-trans Result）。")
@Data
public class RoleIdNameResult {
    @Schema(description = "主键ID")
    private String id;
    @Schema(description = "名称")
    private String name;
}
