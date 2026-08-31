package github.jiangbyte.io.oj.modules.tag.param;

/**
 * OJ 标签分页查询入参。
 *
 * Author: Charlie
 */

import github.jiangbyte.io.common.core.domain.PageQuery;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Schema(description = "OjTag 分页查询入参")
@Data
@EqualsAndHashCode(callSuper = true)
public class OjTagPageParam extends PageQuery {
    @Schema(description = "标签名称")
    private String name;
    @Schema(description = "状态：ENABLED/DISABLED")
    private String status;
}
