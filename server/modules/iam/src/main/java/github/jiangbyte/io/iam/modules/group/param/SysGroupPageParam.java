package github.jiangbyte.io.iam.modules.group.param;

import io.swagger.v3.oas.annotations.media.Schema;
import github.jiangbyte.io.common.core.domain.PageQuery;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 用户组分页查询入参。
 *
 * Author: Charlie
 */
@Schema(description = "用户组分页查询入参。")
@Data
@EqualsAndHashCode(callSuper = true)
public class SysGroupPageParam extends PageQuery {
    @Schema(description = "用户组名称")

    private String name;
    @Schema(description = "用户组状态：ENABLED/DISABLED")
    private String status;
}
