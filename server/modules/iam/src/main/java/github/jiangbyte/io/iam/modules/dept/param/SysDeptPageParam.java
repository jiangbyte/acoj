package github.jiangbyte.io.iam.modules.dept.param;

import io.swagger.v3.oas.annotations.media.Schema;
import github.jiangbyte.io.common.core.domain.PageQuery;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 部门分页查询入参。
 *
 * Author: Charlie
 */
@Schema(description = "部门分页查询入参。")
@Data
@EqualsAndHashCode(callSuper = true)
public class SysDeptPageParam extends PageQuery {
    @Schema(description = "部门名称")

    private String name;
    @Schema(description = "部门类别/层级类型")
    private String category;
    @Schema(description = "部门状态：ENABLED/DISABLED")
    private String status;
    @Schema(description = "父级ID")
    private String parentId;
}
