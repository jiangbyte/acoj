package github.jiangbyte.io.profile.modules.admin.result;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 部门 ID-名称项（由 entity 查询后填充，非 easy-trans Result）。
 *
 * Author: Charlie
 */
@Schema(description = "部门 ID-名称项（由 entity 查询后填充，非 easy-trans Result）。")
@Data
public class DeptIdNameResult {
    @Schema(description = "主键ID")
    private String id;
    @Schema(description = "名称")
    private String name;
}
