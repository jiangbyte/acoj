package github.jiangbyte.io.iam.modules.position.param;

import io.swagger.v3.oas.annotations.media.Schema;
import github.jiangbyte.io.common.core.domain.PageQuery;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 岗位分页查询入参。
 *
 * Author: Charlie
 */
@Schema(description = "岗位分页查询入参。")
@Data
@EqualsAndHashCode(callSuper = true)
public class SysPositionPageParam extends PageQuery {
    @Schema(description = "职位名称")

    private String name;
    @Schema(description = "职位类别")
    private String category;
    @Schema(description = "职位状态：ENABLED/DISABLED")
    private String status;
}
