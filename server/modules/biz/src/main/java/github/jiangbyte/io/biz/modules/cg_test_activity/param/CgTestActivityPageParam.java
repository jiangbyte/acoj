package github.jiangbyte.io.biz.modules.cg_test_activity.param;

/**
 * Activity分页查询入参。
 *
 * Author: Charlie
 */

import io.swagger.v3.oas.annotations.media.Schema;
import github.jiangbyte.io.common.core.domain.PageQuery;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Schema(description = "CgTestActivity分页查询入参")
@Data
@EqualsAndHashCode(callSuper = true)
public class CgTestActivityPageParam extends PageQuery {
    @Schema(description = "编码")
    private String code;
    @Schema(description = "名称")
    private String name;
    @Schema(description = "分类")
    private String category;
    @Schema(description = "类型")
    private String type;
    @Schema(description = "状态")
    private String status;
}
