package github.jiangbyte.io.sys.modules.dict.param;

import io.swagger.v3.oas.annotations.media.Schema;
import github.jiangbyte.io.common.core.domain.PageQuery;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 数据字典分页查询入参。
 *
 * Author: Charlie
 */
@Schema(description = "数据字典分页查询入参。")
@Data
@EqualsAndHashCode(callSuper = true)
public class SysDictPageParam extends PageQuery {
    @Schema(description = "字典项编码（同父级下唯一）")

    private String code;
    @Schema(description = "字典分类：SYSTEM（系统）/ BUSINESS（业务）")
    private String category;
    @Schema(description = "父级字典项ID")
    private String parentId;
    @Schema(description = "字典项状态：ENABLED/DISABLED")
    private String status;
}
